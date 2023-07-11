package com.uroria.backend.velocity;

import com.uroria.backend.common.settings.BackendSettings;
import com.uroria.backend.common.settings.SettingsRequest;
import com.uroria.backend.scheduler.BackendScheduler;
import com.uroria.backend.settings.BackendSettingsGameRequest;
import com.uroria.backend.settings.BackendSettingsIDRequest;
import com.uroria.backend.settings.BackendSettingsTagRequest;
import com.uroria.backend.settings.BackendSettingsUpdate;
import com.uroria.backend.settings.AbstractSettingsManager;
import com.uroria.backend.velocity.events.SettingsDeleteEvent;
import com.uroria.backend.velocity.events.SettingsUpdateEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class SettingsManagerImpl extends AbstractSettingsManager {
    private final int keepAlive = BackendVelocityPlugin.getConfig().getOrSetDefault("cacheKeepAliveInMinutes.settings", 20);
    private final ProxyServer proxyServer;
    private BackendSettingsTagRequest tagRequest;
    private BackendSettingsIDRequest idRequest;
    private BackendSettingsGameRequest gameRequest;
    private BackendSettingsUpdate update;

    SettingsManagerImpl(PulsarClient pulsarClient, Logger logger, ProxyServer proxyServer) {
        super(pulsarClient, logger);
        this.proxyServer = proxyServer;
    }

    @Override
    protected void start(String identifier) {
        runCacheChecker();
        try {
            this.tagRequest = new BackendSettingsTagRequest(this.pulsarClient, identifier);
            this.idRequest = new BackendSettingsIDRequest(this.pulsarClient, identifier);
            this.gameRequest = new BackendSettingsGameRequest(this.pulsarClient, identifier);
            this.update = new BackendSettingsUpdate(this.pulsarClient, identifier, this::checkSettings);
        } catch (Exception exception) {
            this.logger.error("Cannot initialize handlers", exception);
            BackendAPIImpl.captureException(exception);
        }
    }

    @Override
    protected void shutdown() {
        try {
            if (this.tagRequest != null) this.tagRequest.close();
            if (this.idRequest != null) this.idRequest.close();
            if (this.gameRequest != null) this.gameRequest.close();
            if (this.update != null) this.update.close();
        } catch (Exception exception) {
            this.logger.error("Cannot close handlers", exception);
            BackendAPIImpl.captureException(exception);
        }
    }

    @Override
    protected void checkSettings(BackendSettings settings) {
        if (this.settings.stream().noneMatch(settings1 -> settings1.equals(settings))) return;

        for (BackendSettings savedSettings : this.settings) {
            if (!savedSettings.equals(settings)) continue;
            savedSettings.modify(settings);

            logger.info("Updating settings " + settings.getTag());

            this.proxyServer.getEventManager().fireAndForget(new SettingsUpdateEvent(savedSettings));

            if (savedSettings.isDeleted()) {
                this.proxyServer.getEventManager().fireAndForget(new SettingsDeleteEvent(savedSettings));
                this.settings.remove(savedSettings);
            }
            return;
        }

        logger.info("Adding settings " + settings.getTag());
        this.settings.add(settings);
        this.proxyServer.getEventManager().fireAndForget(new SettingsUpdateEvent(settings));
    }

    @Override
    public Collection<BackendSettings> getSettings(@NonNull UUID uuid, int gameId) {
        SettingsRequest request = new SettingsRequest(uuid, gameId);
        Collection<BackendSettings> settingsCollection = this.gameRequest.request(request, 3000).orElse(new ArrayList<>());
        settingsCollection.removeIf(BackendSettings::isDeleted);
        return settingsCollection;
    }

    @Override
    public Optional<BackendSettings> getSettings(@NonNull UUID uuid, int gameId, int id) {
        for (BackendSettings settings : this.settings) {
            if (!settings.getUUID().equals(uuid)) continue;
            if (settings.getGameID() != gameId) continue;
            if (settings.getID() != id) continue;
            return Optional.of(settings);
        }

        SettingsRequest request = new SettingsRequest(uuid, gameId, id);
        Optional<BackendSettings> response = this.idRequest.request(request, 3000);
        if (response.isPresent()) {
            if (response.get().isDeleted()) return Optional.empty();
            settings.add(response.get());
        }
        return response;
    }

    @Override
    public Optional<BackendSettings> getSettings(@NotNull String tag) {
        for (BackendSettings settings : this.settings) {
            if (!settings.getTag().equals(tag)) continue;
            return Optional.of(settings);
        }

        Optional<BackendSettings> request = this.tagRequest.request(new SettingsRequest(tag), 3000);
        if (request.isPresent()) {
            if (request.get().isDeleted()) return Optional.empty();
            settings.add(request.get());
        }
        return request;
    }

    @Override
    public BackendSettings updateSettings(@NonNull BackendSettings settings) {
        try {
            checkSettings(settings);
            this.update.update(settings);
        } catch (Exception exception) {
            this.logger.error("Cannot update settings", exception);
            BackendAPIImpl.captureException(exception);
        }
        return settings;
    }

    private void runCacheChecker() {
        BackendScheduler.runTaskLater(() -> {
            Collection<UUID> markedForRemoval = new ArrayList<>();
            for (BackendSettings settings : this.settings) {
                if (this.proxyServer.getPlayer(settings.getUUID()).isEmpty()) markedForRemoval.add(settings.getUUID());
            }
            return markedForRemoval;
        }, keepAlive, TimeUnit.MINUTES).run(markedForRemoval -> {
            for (UUID uuid : markedForRemoval) {
                this.settings.removeIf(settings -> settings.getUUID().equals(uuid));
            }
            int size = markedForRemoval.size();
            if (size > 0) this.logger.info(size + " settings removed from cache");
            runCacheChecker();
        }, throwable -> {
            this.logger.error("Unhandled exception", throwable);
            BackendAPIImpl.captureException(throwable);
            runCacheChecker();
        });
    }
}
