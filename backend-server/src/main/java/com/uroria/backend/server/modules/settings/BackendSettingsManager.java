package com.uroria.backend.server.modules.settings;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.uroria.backend.settings.BackendSettings;
import com.uroria.backend.settings.SettingsManager;
import com.uroria.backend.pluginapi.BackendRegistry;
import com.uroria.backend.pluginapi.events.settings.SettingsDeleteEvent;
import com.uroria.backend.pluginapi.events.settings.SettingsUpdateEvent;
import com.uroria.backend.server.Uroria;
import com.uroria.backend.server.events.BackendEventManager;
import com.uroria.backend.server.modules.AbstractManager;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.apache.pulsar.client.api.PulsarClient;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public final class BackendSettingsManager extends AbstractManager implements SettingsManager {
    private final PulsarClient pulsarClient;
    private final MongoCollection<Document> settings;
    private final RedisCommands<String, String> cachedSettings;
    private final BackendEventManager eventManager;

    private BackendSettingsUpdate update;
    private BackendSettingsGameResponse gameResponse;
    private BackendSettingsIDResponse idResponse;
    private BackendSettingsTagResponse tagResponse;

    public BackendSettingsManager(Logger logger, PulsarClient pulsarClient, MongoDatabase database, StatefulRedisConnection<String, String> cache) {
        super(logger, "SettingsModule");
        this.pulsarClient = pulsarClient;
        this.settings = database.getCollection("settings", Document.class);
        this.cachedSettings = cache.sync();
        this.eventManager = BackendRegistry.get(BackendEventManager.class).orElseThrow(() -> new NullPointerException("EventManager not initialized"));
    }

    @Override
    public void enable() {
        try {
            this.update = new BackendSettingsUpdate(this.pulsarClient, this);
            this.gameResponse = new BackendSettingsGameResponse(this.pulsarClient, this);
            this.idResponse = new BackendSettingsIDResponse(this.pulsarClient, this);
            this.tagResponse = new BackendSettingsTagResponse(this.pulsarClient, this);
        } catch (Exception exception) {
            this.logger.error("Cannot initialize handlers", exception);
            Uroria.captureException(exception);
        }
    }

    @Override
    public void disable() {
        try {
            if (this.update != null) this.update.close();
            if (this.gameResponse != null) this.gameResponse.close();
            if (this.idResponse != null) this.idResponse.close();
            if (this.tagResponse != null) this.tagResponse.close();
        } catch (Exception exception) {
            this.logger.error("Cannot close handlers", exception);
            Uroria.captureException(exception);
        }
    }

    @Override
    public Collection<BackendSettings> getSettings(@NotNull UUID uuid, int gameId) {
        try {
            return parseSettings(this.settings.find(Filters.and(
                    Filters.eq("uuid", uuid.toString()),
                    Filters.eq("gameId", gameId)
            )).cursor());
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            Uroria.captureException(exception);
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<BackendSettings> getSettings(@NotNull UUID uuid, int gameId, int id) {
        try {
            BackendSettings cachedSettings = getCachedSettings(uuid, gameId, id);
            if (cachedSettings != null) return Optional.of(cachedSettings);
            Document savedDocument = this.settings.find(Filters.and(
                    Filters.eq("uuid", uuid.toString()),
                    Filters.eq("gameId", gameId),
                    Filters.eq("id", id)
            )).first();
            if (savedDocument == null) return Optional.empty();
            return Optional.of(fromJson(savedDocument.toJson()));
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            Uroria.captureException(exception);
            return Optional.empty();
        }
    }

    @Override
    public Optional<BackendSettings> getSettings(@NotNull String tag) {
        try {
            BackendSettings cachedSettings = getCachedSettings(tag);
            if (cachedSettings != null) return Optional.of(cachedSettings);
            Document savedDocument = this.settings.find(Filters.eq("tag", tag)).first();
            if (savedDocument == null) return Optional.empty();
            return Optional.of(fromJson(savedDocument.toJson()));
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            Uroria.captureException(exception);
            return Optional.empty();
        }
    }

    @Override
    public BackendSettings updateSettings(@NotNull BackendSettings settings) {
        updateDatabase(settings);
        this.update.update(settings);
        return settings;
    }

    void updateLocal(BackendSettings settings) {
        updateDatabase(settings);
    }

    private void updateDatabase(BackendSettings settings) {
        try {
            this.cachedSettings.del("settings:" + settings.getUUID() + ":" + settings.getGameID() + ":" + settings.getID());
            this.cachedSettings.del("settings:" + settings.getUUID() + ":" + settings.getGameID() + ":null");
            this.cachedSettings.del("settings:" + settings.getTag());
            String json = Uroria.getGson().toJson(settings);
            if (settings.isDeleted()) {
                if (this.settings.deleteOne(Filters.and(
                        Filters.eq("uuid", settings.getUUID().toString()),
                        Filters.eq("id", settings.getID()),
                        Filters.eq("gameId", settings.getGameID())
                )).wasAcknowledged()) {
                    SettingsDeleteEvent settingsDeleteEvent = new SettingsDeleteEvent(settings);
                    this.eventManager.callEventAsync(settingsDeleteEvent);
                    this.logger.debug("Deleted settings " + settings.getTag());
                    return;
                }
                this.logger.warn("Could not delete settings " + settings.getTag());
                return;
            }
            fromJson(json);
            Document document = Document.parse(json);
            if (this.settings.replaceOne(Filters.and(
                    Filters.eq("uuid", settings.getUUID().toString()),
                    Filters.eq("id", settings.getID()),
                    Filters.eq("gameId", settings.getGameID())
            ), document).wasAcknowledged()) {
                SettingsUpdateEvent settingsUpdateEvent = new SettingsUpdateEvent(settings);
                this.eventManager.callEventAsync(settingsUpdateEvent);
                this.logger.debug("Updated settings " + settings.getTag());
                return;
            }
            this.logger.warn("Could not update settings " + settings.getTag());
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            Uroria.captureException(exception);
        }
    }

    private BackendSettings fromJson(String json) {
        BackendSettings settings = Uroria.getGson().fromJson(json, BackendSettings.class);
        String key = "settings:" + settings.getUUID() + ":" + settings.getGameID() + ":" + settings.getID();
        this.cachedSettings.set(key, json, SetArgs.Builder.ex(Duration.ofHours(2)));
        this.cachedSettings.set("settings:" + settings.getUUID() + ":" + settings.getGameID() + ":null", key, SetArgs.Builder.ex(Duration.ofHours(8)));
        this.cachedSettings.set("settings:" + settings.getTag(), key, SetArgs.Builder.ex(Duration.ofHours(8)));
        return settings;
    }

    private BackendSettings getCachedSettings(UUID uuid, int gameId) {
        String cachedObjectKey = this.cachedSettings.get("settings:" + uuid + ":" + gameId + ":null");
        if (cachedObjectKey == null) return null;
        String cachedObject = this.cachedSettings.get(cachedObjectKey);
        if (cachedObject == null) return null;
        return Uroria.getGson().fromJson(cachedObject, BackendSettings.class);
    }

    private BackendSettings getCachedSettings(UUID uuid, int gameId, int id) {
        String cachedObject = this.cachedSettings.get("settings:" + uuid + ":" + gameId + ":" + id);
        if (cachedObject == null) return null;
        return Uroria.getGson().fromJson(cachedObject, BackendSettings.class);
    }

    private BackendSettings getCachedSettings(String tag) {
        String cachedObjectKey = this.cachedSettings.get("settings:" + tag);
        if (cachedObjectKey == null) return null;
        String cachedObject = this.cachedSettings.get(cachedObjectKey);
        if (cachedObject == null) return null;
        return Uroria.getGson().fromJson(cachedObject, BackendSettings.class);
    }

    private Collection<BackendSettings> parseSettings(MongoCursor<Document> documents) {
        Collection<BackendSettings> settings = new ArrayList<>();

        while (documents.hasNext()) {
            Document document = documents.next();
            BackendSettings settings1 = Uroria.getGson().fromJson(document.toJson(), BackendSettings.class);
            settings.add(settings1);
        }
        documents.close();

        return settings;
    }
}
