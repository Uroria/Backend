package com.uroria.backend.server.modules.player;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.uroria.backend.pluginapi.BackendRegistry;
import com.uroria.backend.pluginapi.events.player.PlayerRegisterEvent;
import com.uroria.backend.pluginapi.events.player.PlayerUpdateEvent;
import com.uroria.backend.common.BackendPlayer;
import com.uroria.backend.pluginapi.modules.PlayerManager;
import com.uroria.backend.server.Uroria;
import com.uroria.backend.server.events.BackendEventManager;
import com.uroria.backend.server.modules.AbstractManager;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.apache.pulsar.client.api.PulsarClient;
import org.bson.Document;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public final class BackendPlayerManager extends AbstractManager implements PlayerManager {
    private final PulsarClient pulsarClient;
    private final MongoCollection<Document> players;
    private final RedisCommands<String, String> cachedPlayers;
    private final BackendEventManager eventManager;
    private BackendPlayerUUIDResponse uuidResponse;
    private BackendPlayerNameResponse nameResponse;
    private BackendPlayerUpdate playerUpdate;

    public BackendPlayerManager(Logger logger, PulsarClient pulsarClient, MongoDatabase database, StatefulRedisConnection<String, String> cache) {
        super(logger, "PlayerModule");
        this.pulsarClient = pulsarClient;
        this.players = database.getCollection("players", Document.class);
        this.cachedPlayers = cache.sync();
        this.eventManager = BackendRegistry.get(BackendEventManager.class).orElseThrow(() -> new NullPointerException("EventManager not initialized"));
    }

    @Override
    public void enable() {
        try {
            this.uuidResponse = new BackendPlayerUUIDResponse(this.pulsarClient, this);
            this.nameResponse = new BackendPlayerNameResponse(this.pulsarClient, this);
            this.playerUpdate = new BackendPlayerUpdate(this.pulsarClient, this);
        } catch (Exception exception) {
            this.logger.error("Cannot initialize handlers", exception);
        }
    }

    @Override
    public void disable() {
        try {
            if (this.uuidResponse != null) this.uuidResponse.close();
            if (this.nameResponse != null) this.nameResponse.close();
            if (this.playerUpdate != null) this.playerUpdate.close();
        } catch (Exception exception) {
            this.logger.error("Cannot close handlers", exception);
        }
    }

    @Override
    public Optional<BackendPlayer> getPlayer(UUID uuid) {
        try {
            BackendPlayer cachedPlayer = getCachedPlayer(uuid);
            if (cachedPlayer != null) return Optional.of(cachedPlayer);
            Document savedDocument = this.players.find(Filters.eq("uuid", uuid.toString())).first();
            if (savedDocument == null) {
                BackendPlayer player = new BackendPlayer(uuid, null);
                String json = Uroria.getGson().toJson(player);
                Document document = Document.parse(json);
                if (this.players.insertOne(document).wasAcknowledged()) {
                    PlayerRegisterEvent playerRegisterEvent = new PlayerRegisterEvent(player);
                    this.eventManager.callEventAsync(playerRegisterEvent);
                    this.logger.info("Registered new player " + player.getUUID());
                    return Optional.of(player);
                }
                return Optional.empty();
            }
            return Optional.of(fromJson(savedDocument.toJson()));
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            Uroria.captureException(exception);
            return Optional.empty();
        }
    }

    @Override
    public Optional<BackendPlayer> getPlayer(String currentName) {
        currentName = currentName.toLowerCase();
        try {
            BackendPlayer cachedPlayer = getCachedPlayer(currentName);
            if (cachedPlayer != null) return Optional.of(cachedPlayer);
            Document savedDocument = this.players.find(Filters.eq("currentName", currentName)).first();
            if (savedDocument == null) return Optional.empty();
            return Optional.of(fromJson(savedDocument.toJson()));
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            Uroria.captureException(exception);
            return Optional.empty();
        }
    }

    @Override
    public void updatePlayer(BackendPlayer player) {
        updateDatabase(player);
        this.playerUpdate.update(player);
    }

    void updateLocal(BackendPlayer player) {
        updateDatabase(player);
    }

    private void updateDatabase(BackendPlayer player) {
        try {
            this.cachedPlayers.del("player:" + player.getUUID());
            String json = Uroria.getGson().toJson(player);
            fromJson(json);
            Document document = Document.parse(json);
            if (this.players.replaceOne(Filters.eq("uuid", player.getUUID().toString()), document).wasAcknowledged()) {
                PlayerUpdateEvent playerUpdateEvent = new PlayerUpdateEvent(player);
                this.eventManager.callEventAsync(playerUpdateEvent);
                this.logger.debug("Updated player " + player.getCurrentName().orElse(player.getUUID().toString()));
                return;
            }
            this.logger.warn("Could not update player " + player.getCurrentName().orElse(player.getUUID().toString()));
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            Uroria.captureException(exception);
        }
    }

    private BackendPlayer fromJson(String json) {
        BackendPlayer player = Uroria.getGson().fromJson(json, BackendPlayer.class);
        String key = "player:" + player.getUUID();
        this.cachedPlayers.set(key, json, SetArgs.Builder.ex(Duration.ofHours(4)));
        player.getCurrentName().ifPresent(name -> {
            this.cachedPlayers.set("player:" + name, key, SetArgs.Builder.ex(Duration.ofHours(24)));
        });
        return player;
    }

    private BackendPlayer getCachedPlayer(UUID uuid) {
        String cachedObject = this.cachedPlayers.get("player:" + uuid);
        if (cachedObject == null) return null;
        return Uroria.getGson().fromJson(cachedObject, BackendPlayer.class);
    }

    private BackendPlayer getCachedPlayer(String name) {
        String cachedObjectKey = this.cachedPlayers.get("player:" + name);
        if (cachedObjectKey == null) return null;
        String cachedObject = this.cachedPlayers.get("player:" + cachedObjectKey);
        if (cachedObject == null) return null;
        return Uroria.getGson().fromJson(cachedObject, BackendPlayer.class);
    }
}
