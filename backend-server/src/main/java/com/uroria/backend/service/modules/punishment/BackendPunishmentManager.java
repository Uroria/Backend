package com.uroria.backend.service.modules.punishment;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.uroria.backend.punishment.Punished;
import com.uroria.backend.service.modules.AbstractManager;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.bson.Document;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public final class BackendPunishmentManager extends AbstractManager implements PunishmentManager {
    private final MongoCollection<Document> punisheds;
    private final RedisCommands<String, String> cache;
    private PunishedUpdate update;
    private PunishedResponse response;

    public BackendPunishmentManager(PulsarClient pulsarClient, MongoDatabase database, StatefulRedisConnection<String, String> cache) {
        super(pulsarClient, "PunishmentModule");
        this.punisheds = database.getCollection("punisheds", Document.class);
        this.cache = cache.sync();
    }

    @Override
    public void enable() throws PulsarClientException {
        this.update = new PunishedUpdate(this.pulsarClient, this);
        this.response = new PunishedResponse(this.pulsarClient, this);
    }

    @Override
    public void disable() throws PulsarClientException {
        if (this.update != null) this.update.close();
        if (this.response != null) this.response.close();
    }

    @Override
    public Optional<Punished> getPunished(UUID uuid, int timeout) {
        if (uuid == null) return Optional.empty();
        try {
            Punished cachedPunished = getCachedPunished(uuid);
            if (cachedPunished != null) return Optional.of(cachedPunished);
            Document savedDocument = this.punisheds.find(Filters.eq("uuid", uuid.toString())).first();
            if (savedDocument == null) return Optional.empty();
            return Optional.of(fromJson(savedDocument.toJson()));
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            return Optional.empty();
        }
    }

    @Override
    public void updatePunished(@NonNull Punished punished) {
        updateDatabase(punished);
        this.update.update(punished);
    }

    void updateDatabase(@NonNull Punished punished) {
        try {
            this.cache.del("punished:" + punished.getUUID());
            String json = gson.toJson(punished);
            fromJson(json);
            Document newDocument = Document.parse(json);
            Document document = this.punisheds.find(Filters.eq("uuid", punished.getUUID().toString())).first();
            if (document == null) {
                if (this.punisheds.insertOne(newDocument).wasAcknowledged()) {
                    this.logger.debug("Inserted " + punished);
                    return;
                }
                this.logger.warn("Unable to insert " + punished);
                return;
            }
            if (this.punisheds.replaceOne(Filters.eq("uuid", punished.getUUID().toString()), newDocument).wasAcknowledged()) {
                this.logger.debug("Replaced " + punished);
                return;
            }
            this.logger.warn("Unable to replace " + punished);
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
        }
    }

    private Punished fromJson(String json) {
        Punished punished = gson.fromJson(json, Punished.class);
        String key = "punished:" + punished.getUUID();
        this.cache.set(key, json, lifespan(Duration.ofHours(4)));
        return punished;
    }

    private Punished getCachedPunished(UUID uuid) {
        String cachedObject = this.cache.get("punished:" + uuid);
        if (cachedObject == null) return null;
        return gson.fromJson(cachedObject, Punished.class);
    }
}
