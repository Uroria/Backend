package com.uroria.backend.server.modules.stats;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.uroria.backend.pluginapi.BackendRegistry;
import com.uroria.backend.pluginapi.events.stats.StatRegisterEvent;
import com.uroria.backend.pluginapi.events.stats.StatUpdateEvent;
import com.uroria.backend.pluginapi.modules.StatsManager;
import com.uroria.backend.common.BackendStat;
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

public final class BackendStatsManager implements StatsManager {
    private final Logger logger;
    private final PulsarClient pulsarClient;
    private final MongoCollection<Document> stats;
    private final RedisCommands<String, String> cachedStats;
    private final BackendEventManager eventManager;

    public BackendStatsManager(Logger logger, PulsarClient pulsarClient, MongoDatabase database, StatefulRedisConnection<String, String> cache) {
        this.logger = logger;
        this.pulsarClient = pulsarClient;
        this.stats = database.getCollection("stats", Document.class);
        this.cachedStats = cache.sync();
        this.eventManager = BackendRegistry.get(BackendEventManager.class).orElseThrow(() -> new NullPointerException("EventManager not initialized"));
    }

    public void start() {
        try {
            this.logger.debug("Running non active stat system");
        } catch (Exception exception) {
            this.logger.error("Cannot initialize handlers", exception);
        }
    }

    public void shutdown() {
        try {
            this.logger.debug("Stopping non active stat system");
        } catch (Exception exception) {
            this.logger.error("Cannot close handlers", exception);
        }
    }

    @Override
    public Optional<BackendStat> getStat(UUID holder) {
        try {
            BackendStat cachedStat = getCachedStat(holder);
            if (cachedStat != null) return Optional.of(cachedStat);
            Document savedDocument = this.stats.find(Filters.eq("uuid", holder.toString())).first();
            if (savedDocument == null) return Optional.empty();
            return Optional.of(fromJson(savedDocument.toJson()));
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            Uroria.captureException(exception);
            return Optional.empty();
        }
    }

    @Override
    public void updateStat(BackendStat stat) {
        try {
            this.cachedStats.del("stats:" + stat.getUUID());
            String json = Uroria.getGson().toJson(stat);
            fromJson(json);
            Document newDocument = Document.parse(json);
            Document document = this.stats.find(Filters.eq("uuid", stat)).first();
            if (document == null) {
                if (this.stats.insertOne(newDocument).wasAcknowledged()) {
                    StatRegisterEvent statRegisterEvent = new StatRegisterEvent(stat);
                    this.eventManager.callEvent(statRegisterEvent);
                    this.logger.debug("Registered stat " + stat.getUUID() + " for " + stat.getGameId());
                } else this.logger.warn("Could not register stat");
                return;
            }
            if (this.stats.replaceOne(Filters.eq("uuid", stat.getUUID().toString()), newDocument).wasAcknowledged()) {
                StatUpdateEvent statUpdateEvent = new StatUpdateEvent(stat);
                this.eventManager.callEvent(statUpdateEvent);
                this.logger.debug("Updated stat " + stat.getUUID() + " for " + stat.getGameId());
                return;
            }
            this.logger.warn("Could not update stat " + stat.getUUID());
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            Uroria.captureException(exception);
        }
    }

    private BackendStat fromJson(String json) {
        BackendStat stat = Uroria.getGson().fromJson(json, BackendStat.class);
        String key = "stats:" + stat.getUUID();
        this.cachedStats.set(key, json, SetArgs.Builder.ex(Duration.ofHours(1)));
        return stat;
    }

    private BackendStat getCachedStat(UUID holder) {
        String cachedObject = this.cachedStats.get("stats:" + holder);
        if (cachedObject == null) return null;
        return Uroria.getGson().fromJson(cachedObject, BackendStat.class);
    }
}
