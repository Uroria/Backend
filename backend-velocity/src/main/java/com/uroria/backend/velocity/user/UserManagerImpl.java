package com.uroria.backend.velocity.user;

import com.uroria.backend.impl.scheduler.BackendScheduler;
import com.uroria.backend.impl.user.AbstractUserManager;
import com.uroria.backend.impl.user.UserNameRequestChannel;
import com.uroria.backend.impl.user.UserUUIDRequestChannel;
import com.uroria.backend.impl.user.UserUpdateChannel;
import com.uroria.backend.user.User;
import com.uroria.backend.user.UserManager;
import com.uroria.backend.velocity.BackendVelocityPlugin;
import com.velocitypowered.api.proxy.ProxyServer;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class UserManagerImpl extends AbstractUserManager implements UserManager {

    private final ProxyServer proxyServer;
    private UserUUIDRequestChannel uuidRequest;
    private UserNameRequestChannel nameRequest;
    private UserUpdateChannel update;

    public UserManagerImpl(PulsarClient pulsarClient, Logger logger, ProxyServer proxyServer) {
        super(pulsarClient, logger);
        this.proxyServer = proxyServer;
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
    protected void checkUser(@NonNull User user) {
        if (this.users.stream().noneMatch(user::equals)) return;

        if (user.isDeleted()) {
            this.users.remove(user);
            this.proxyServer.getEventManager().fireAndForget(new UserUpdateEvent(user));
            return;
        }

        for (User cachedUser : this.users) {
            if (!cachedUser.equals(user)) continue;
            cachedUser.modify(user);

            logger.info("Updated " + user);

            this.proxyServer.getEventManager().fireAndForget(new UserUpdateEvent(cachedUser));
            return;
        }

        logger.info("Adding " + user);
        this.users.add(user);
        this.proxyServer.getEventManager().fireAndForget(new UserUpdateEvent(user));
    }

    @Override
    public Optional<User> getUser(UUID uuid, int timeout) {
        for (User user : this.users) {
            if (user.getUUID().equals(uuid)) return Optional.of(user);
        }

        if (BackendVelocityPlugin.isOffline()) {
            User user = new User(uuid);
            users.add(user);
            return Optional.of(user);
        }

        Optional<User> request = uuidRequest.request(uuid, timeout);
        request.ifPresent(this.users::add);
        return request;
    }

    @Override
    public Optional<User> getUser(String name, int timeout) {
        for (User user : this.users) {
            String username = user.getUsername();
            if (username == null) continue;
            if (username.equalsIgnoreCase(name)) return Optional.of(user);
        }

        if (BackendVelocityPlugin.isOffline()) return Optional.empty();

        Optional<User> request = nameRequest.request(name.toLowerCase(), timeout);
        request.ifPresent(user -> {
            if (user.getUsername() == null) return;
            this.users.add(user);
        });
        return request;
    }

    @Override
    public void updateUser(@NonNull User user) {
        try {
            checkUser(user);
            if (BackendVelocityPlugin.isOffline()) return;
            this.update.update(user);
        } catch (Exception exception) {
            this.logger.error("Cannot update " + user);
        }
    }

    private void runCacheChecker() {
        BackendScheduler.runTaskLater(() -> {
            ObjectArraySet<UUID> markedForRemoval = new ObjectArraySet<>();
            for (User user : this.users) {
                if (this.proxyServer.getPlayer(user.getUUID()).isEmpty()) markedForRemoval.add(user.getUUID());
            }
            return markedForRemoval;
        }, 10, TimeUnit.MINUTES).run(markedForRemoval -> {
            for (UUID uuid : markedForRemoval) {
                this.users.removeIf(user -> user.getUUID().equals(uuid));
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
