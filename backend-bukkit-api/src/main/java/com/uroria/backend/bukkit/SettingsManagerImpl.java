package com.uroria.backend.bukkit;

import com.uroria.backend.bukkit.events.SettingsDeleteEvent;
import com.uroria.backend.bukkit.events.SettingsUpdateEvent;
import com.uroria.backend.common.BackendSettings;
import com.uroria.backend.common.helpers.SettingsRequest;
import com.uroria.backend.scheduler.BackendScheduler;
import com.uroria.backend.settings.BackendSettingsGameRequest;
import com.uroria.backend.settings.BackendSettingsIDRequest;
import com.uroria.backend.settings.BackendSettingsTagRequest;
import com.uroria.backend.settings.BackendSettingsUpdate;
import com.uroria.backend.settings.SettingsManager;
import org.apache.pulsar.client.api.PulsarClient;
import org.bukkit.Bukkit;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class SettingsManagerImpl extends SettingsManager {
    private final int keepAlive = BackendBukkitPlugin.config().getOrSetDefault("cacheKeepAliveInMinutes.settings", 20);
    private BackendSettingsTagRequest tagRequest;
    private BackendSettingsIDRequest idRequest;
    private BackendSettingsGameRequest gameRequest;
    private BackendSettingsUpdate update;

    SettingsManagerImpl(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient, logger);
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
            BackendAPI.captureException(exception);
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
            BackendAPI.captureException(exception);
        }
    }

    @Override
    protected void checkSettings(BackendSettings settings) {
        if (this.settings.stream().noneMatch(settings1 -> settings1.equals(settings))) return;

        for (BackendSettings savedSettings : this.settings) {
            if (!savedSettings.equals(settings)) continue;
            savedSettings.modify(settings);

            logger.info("Updating settings " + settings.getTag());

            CompletableFuture.runAsync(() -> Bukkit.getPluginManager().callEvent(new SettingsUpdateEvent(savedSettings)));

            if (savedSettings.isDeleted()) {
                CompletableFuture.runAsync(() -> Bukkit.getPluginManager().callEvent(new SettingsDeleteEvent(savedSettings)));
                this.settings.remove(savedSettings);
            }
            return;
        }

        logger.info("Adding settings " + settings.getTag());
        this.settings.add(settings);
        CompletableFuture.runAsync(() -> Bukkit.getPluginManager().callEvent(new SettingsUpdateEvent(settings)));
    }

    @Override
    public Collection<BackendSettings> getSettings(UUID uuid, int gameId) {
        if (uuid == null) throw new IllegalArgumentException("UUID cannot be null");
        SettingsRequest request = new SettingsRequest(uuid, gameId);
        Collection<BackendSettings> settingsCollection = this.gameRequest.request(request).orElse(new ArrayList<>());
        settingsCollection.removeIf(BackendSettings::isDeleted);
        return settingsCollection;
    }

    @Override
    public Optional<BackendSettings> getSettings(UUID uuid, int gameId, int id) {
        if (uuid == null) throw new IllegalArgumentException("UUID cannot be null");

        for (BackendSettings settings : this.settings) {
            if (!settings.getUUID().equals(uuid)) continue;
            if (settings.getGameID() != gameId) continue;
            if (settings.getID() != id) continue;
            return Optional.of(settings);
        }

        SettingsRequest request = new SettingsRequest(uuid, gameId, id);
        Optional<BackendSettings> response = this.idRequest.request(request);
        if (response.isPresent()) {
            if (response.get().isDeleted()) return Optional.empty();
            settings.add(response.get());
        }
        return response;
    }

    @Override
    public Optional<BackendSettings> getSettings(String tag) {
        if (tag == null) throw new IllegalArgumentException("Tag cannot be null");

        for (BackendSettings settings : this.settings) {
            if (!settings.getTag().equals(tag)) continue;
            return Optional.of(settings);
        }

        Optional<BackendSettings> request = this.tagRequest.request(new SettingsRequest(tag));
        if (request.isPresent()) {
            if (request.get().isDeleted()) return Optional.empty();
            settings.add(request.get());
        }
        return request;
    }

    @Override
    public void updateSettings(BackendSettings settings) {
        if (settings == null) throw new IllegalArgumentException("Settings cannot be null");
        try {
            checkSettings(settings);
            this.update.update(settings);
        } catch (Exception exception) {
            this.logger.error("Cannot update settings", exception);
            BackendAPI.captureException(exception);
        }
    }

    private void runCacheChecker() {
        BackendScheduler.runTaskLater(() -> {
            Collection<UUID> markedForRemoval = new ArrayList<>();
            for (BackendSettings settings : this.settings) {
                if (Bukkit.getPlayer(settings.getUUID()) == null) markedForRemoval.add(settings.getUUID());
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
            BackendAPI.captureException(throwable);
            runCacheChecker();
        });
    }
}
