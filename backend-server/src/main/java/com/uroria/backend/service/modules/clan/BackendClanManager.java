package com.uroria.backend.service.modules.clan;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
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

public class BackendClanManager extends AbstractManager implements ClanManager {
    private final MongoCollection<Document> clans;
    private final RedisCommands<String, String> cache;
    private ClanUpdate update;
    private ClanTagResponse tagResponse;
    private ClanOperatorResponse operatorResponse;

    public BackendClanManager(PulsarClient pulsarClient, MongoDatabase database, StatefulRedisConnection<String, String> cache) {
        super(pulsarClient, "ClanModule");
        this.clans = database.getCollection("clans", Document.class);
        this.cache = cache.sync();
    }

    @Override
    protected void enable() throws PulsarClientException {
        this.update = new ClanUpdate(this.pulsarClient, this);
        this.tagResponse = new ClanTagResponse(this.pulsarClient, this);
        this.operatorResponse = new ClanOperatorResponse(this.pulsarClient, this);
    }

    @Override
    protected void disable() throws PulsarClientException {
        if (this.update != null) this.update.close();
        if (this.tagResponse != null) this.tagResponse.close();
        if (this.operatorResponse != null) this.operatorResponse.close();
    }

    @Override
    public Optional<ClanOld> getClan(String tag, int timeout) {
        try {
            ClanOld cachedClan = getCachedClan(tag);
            if (cachedClan != null) return Optional.of(cachedClan);
            Document savedDocument = this.clans.find(Filters.eq("tag", tag)).first();
            if (savedDocument == null) return Optional.empty();
            return Optional.of(fromJson(savedDocument.toJson()));
        } catch (Exception exception) {
            logger.error("Unhandled exception", exception);
            return Optional.empty();
        }
    }

    @Override
    public Optional<ClanOld> getClan(UUID operator, int timeout) {
        try {
            ClanOld cachedClan = getCachedClan(operator);
            if (cachedClan != null) return Optional.of(cachedClan);
            Document savedDocument = this.clans.find(Filters.eq("operator", operator.toString())).first();
            if (savedDocument == null) return Optional.empty();
            return Optional.of(fromJson(savedDocument.toJson()));
        } catch (Exception exception) {
            logger.error("Unhandled exception", exception);
            return Optional.empty();
        }
    }

    @Override
    public void updateClan(@NonNull ClanOld clan) {
        updateDatabase(clan);
    }

    void updateDatabase(@NonNull ClanOld clan) {
        try {
            this.cache.del("clan:tag:" + clan.getTag());
            this.cache.del("clan:operator:" + clan.getOperator());
            if (clan.isDeleted()) {
                if (this.clans.deleteOne(Filters.eq("name", clan.getName())).wasAcknowledged()) {
                    logger.debug("Deleted " + clan);
                    return;
                }
                logger.warn("Unable to delete " + clan);
                return;
            }
            String json = gson.toJson(clan);
            fromJson(json);
            Document newDocument = Document.parse(json);
            Document document = this.clans.find(Filters.eq("name", clan.getName())).first();
            if (document == null) {
                if (this.clans.insertOne(newDocument).wasAcknowledged()) {
                    this.logger.debug("Inserted clan " + clan);
                    return;
                }
                this.logger.warn("Unable to insert clan " + clan);
                return;
            }
            if (this.clans.replaceOne(Filters.eq("name", clan.getName()), newDocument).wasAcknowledged()) {
                logger.debug("Replaced " + clan);
                return;
            }
            this.logger.warn("Unable to replace " + clan);
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
        }
    }

    private ClanOld fromJson(String json) {
        ClanOld clan = gson.fromJson(json, ClanOld.class);
        String key = "clan:tag:" + clan.getTag();
        this.cache.set(key, json, lifespan(Duration.ofHours(48)));
        this.cache.set("clan:operator:" + clan.getOperator(), key, lifespan(Duration.ofHours(24)));
        return clan;
    }

    private ClanOld getCachedClan(UUID operator) {
        String cachedObjectKey = this.cache.get("clan:operator:" + operator);
        if (cachedObjectKey == null) return null;
        String cachedObject = this.cache.get(cachedObjectKey);
        if (cachedObject == null) return null;
        return gson.fromJson(cachedObject, ClanOld.class);
    }

    private ClanOld getCachedClan(String tag) {
        String cachedObject = this.cache.get("clan:tag:" + tag);
        if (cachedObject == null) return null;
        return gson.fromJson(cachedObject, ClanOld.class);
    }
}
