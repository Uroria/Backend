package com.uroria.backend.server.modules.player;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.uroria.backend.api.BackendRegistry;
import com.uroria.backend.api.events.player.PlayerRegisterEvent;
import com.uroria.backend.api.events.player.PlayerUpdateEvent;
import com.uroria.backend.common.BackendPlayer;
import com.uroria.backend.api.modules.PlayerManager;
import com.uroria.backend.server.Uroria;
import com.uroria.backend.server.events.BackendEventManager;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.apache.pulsar.client.api.PulsarClient;
import org.bson.Document;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class BackendPlayerManager implements PlayerManager {
    private final Logger logger;
    private final PulsarClient pulsarClient;
    private final MongoCollection<Document> players;
    private final RedisCommands<String, String> cachedPlayers;
    private final BackendEventManager eventManager;
    private BackendPlayerRequest requestReceiver;
    private BackendPlayerResponse responseSender;
    private BackendPlayerUpdate update;

    public BackendPlayerManager(Logger logger, PulsarClient pulsarClient, MongoDatabase database, StatefulRedisConnection<String, String> cache) {
        this.logger = logger;
        this.pulsarClient = pulsarClient;
        this.players = database.getCollection("players", Document.class);
        this.cachedPlayers = cache.sync();
        this.eventManager = BackendRegistry.get(BackendEventManager.class).orElseThrow(() -> new NullPointerException("EventManager not initialized"));
    }

    public void start() {
        try {
            this.requestReceiver = new BackendPlayerRequest(this.pulsarClient, this.logger, this);
            this.responseSender = new BackendPlayerResponse(this.pulsarClient);
            this.update = new BackendPlayerUpdate(this.logger, this, this.pulsarClient);
        } catch (Exception exception) {
            this.logger.error("Cannot initialize handlers", exception);
        }
    }

    public void shutdown() {
        try {
            if (this.requestReceiver != null) this.requestReceiver.close();
            if (this.responseSender != null) this.responseSender.close();
            if (this.update != null) this.update.close();
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
                    CompletableFuture.runAsync(() -> {
                        PlayerRegisterEvent playerRegisterEvent = new PlayerRegisterEvent(player);
                        this.eventManager.callEvent(playerRegisterEvent);
                    });
                    this.logger.info("Registered new player " + player.getUUID());
                    return Optional.of(player);
                }
                else return Optional.empty();
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
        try {
            this.cachedPlayers.del("player:" + player.getUUID());
            String json = Uroria.getGson().toJson(player);
            fromJson(json);
            Document document = Document.parse(json);
            if (this.players.replaceOne(Filters.eq("uuid", player.getUUID().toString()), document).wasAcknowledged()) {
                PlayerUpdateEvent playerUpdateEvent = new PlayerUpdateEvent(player);
                this.eventManager.callEvent(playerUpdateEvent);
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

    BackendPlayerResponse getResponseSender() {
        return responseSender;
    }
}
