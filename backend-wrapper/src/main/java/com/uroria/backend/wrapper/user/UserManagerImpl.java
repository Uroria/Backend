package com.uroria.backend.wrapper.user;

import com.uroria.backend.impl.scheduler.BackendScheduler;
import com.uroria.backend.impl.user.AbstractUserManager;
import com.uroria.backend.impl.user.UserNameRequestChannel;
import com.uroria.backend.impl.user.UserUUIDRequestChannel;
import com.uroria.backend.impl.user.UserUpdateChannel;
import com.uroria.base.event.EventManager;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public final class UserManagerImpl extends AbstractUserManager implements UserManager {
    private final Function<UUID, Boolean> onlinePlayerCheck;
    private final boolean offline;
    private final EventManager eventManager;
    private UserUUIDRequestChannel uuidRequest;
    private UserNameRequestChannel nameRequest;
    private UserUpdateChannel update;

    public UserManagerImpl(PulsarClient pulsarClient, Logger logger, Function<UUID, Boolean> onlinePlayerCheck, boolean offline, EventManager eventManager) {
        super(pulsarClient, logger);
        this.onlinePlayerCheck = onlinePlayerCheck;
        this.offline = offline;
        this.eventManager = eventManager;
    }

    @Override
    public void start(String identifier) throws PulsarClientException {
        this.uuidRequest = new UserUUIDRequestChannel(this.pulsarClient, identifier);
        this.nameRequest = new UserNameRequestChannel(this.pulsarClient, identifier);
        this.update = new UserUpdateChannel(this.pulsarClient, identifier, this::checkUser);
        runCacheChecker();
    }

    @Override
    public void shutdown() throws PulsarClientException {
        if (this.uuidRequest != null) this.uuidRequest.close();
        if (this.nameRequest != null) this.nameRequest.close();
        if (this.update != null) this.update.close();
    }

    @Override
    protected void checkUser(@NonNull UserOld user) {
        if (this.users.stream().noneMatch(user::equals)) return;

        if (user.isDeleted()) {
            this.users.remove(user);
            this.eventManager.callAndForget(new UserUpdateEvent(user));
            return;
        }

        for (UserOld cachedUser : this.users) {
            if (!cachedUser.equals(user)) continue;
            cachedUser.modify(user);

            logger.info("Updated " + user);

            this.eventManager.callAndForget(new UserUpdateEvent(cachedUser));
            return;
        }

        logger.info("Adding " + user);
        this.users.add(user);
        this.eventManager.callAndForget(new UserUpdateEvent(user));
    }

    @Override
    public Optional<UserOld> getUser(UUID uuid, int timeout) {
        for (UserOld user : this.users) {
            if (user.getUniqueId().equals(uuid)) return Optional.of(user);
        }

        if (this.offline) {
            UserOld user = new UserOld(uuid);
            users.add(user);
            return Optional.of(user);
        }

        Optional<UserOld> request = uuidRequest.request(uuid, timeout);
        request.ifPresent(this.users::add);
        return request;
    }

    @Override
    public Optional<UserOld> getUser(String name, int timeout) {
        for (UserOld user : this.users) {
            String username = user.getUsername();
            if (username.equalsIgnoreCase(name)) return Optional.of(user);
        }

        if (this.offline) return Optional.empty();

        Optional<UserOld> request = nameRequest.request(name.toLowerCase(), timeout);
        request.ifPresent(this.users::add);
        return request;
    }

    @Override
    public void updateUser(@NonNull UserOld user) {
        if (this.users.stream().noneMatch(user::equals)) this.users.add(user);
        try {
            checkUser(user);
            if (this.offline) return;
            this.update.update(user);
        } catch (Exception exception) {
            this.logger.error("Cannot update " + user);
        }
    }

    private void runCacheChecker() {
        BackendScheduler.runTaskLater(() -> {
            ObjectArraySet<UUID> markedForRemoval = new ObjectArraySet<>();
            for (UserOld user : this.users) {
                if (!this.onlinePlayerCheck.apply(user.getUniqueId())) markedForRemoval.add(user.getUniqueId());
            }
            return markedForRemoval;
        }, 10, TimeUnit.MINUTES).run(markedForRemoval -> {
            for (UUID uuid : markedForRemoval) {
                this.users.removeIf(user -> user.getUniqueId().equals(uuid));
            }
            int size = markedForRemoval.size();
            if (size > 0) this.logger.info(size + " Users flushed from cache");
            runCacheChecker();
        }, throwable -> {
            this.logger.error("Unhandled exception in cache checker", throwable);
            runCacheChecker();
        });
    }
}
