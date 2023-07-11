package com.uroria.backend.server.modules.stats;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.uroria.backend.common.stats.StatsManager;
import com.uroria.backend.pluginapi.BackendRegistry;
import com.uroria.backend.pluginapi.events.stats.StatRegisterEvent;
import com.uroria.backend.pluginapi.events.stats.StatUpdateEvent;
import com.uroria.backend.common.stats.BackendStat;
import com.uroria.backend.server.Uroria;
import com.uroria.backend.server.events.BackendEventManager;
import com.uroria.backend.server.modules.AbstractManager;
import org.apache.pulsar.client.api.PulsarClient;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public final class BackendStatsManager extends AbstractManager implements StatsManager {
    private final PulsarClient pulsarClient;
    private final MongoCollection<Document> stats;
    private final BackendEventManager eventManager;

    private BackendStatsResponse request;
    private BackendStatsUpdate update;

    public BackendStatsManager(Logger logger, PulsarClient pulsarClient, MongoDatabase database) {
        super(logger, "StatsModule");
        this.pulsarClient = pulsarClient;
        this.stats = database.getCollection("stats", Document.class);
        this.eventManager = BackendRegistry.get(BackendEventManager.class).orElseThrow(() -> new NullPointerException("EventManager not initialized"));
    }

    @Override
    public void enable() {
        try {
            this.request = new BackendStatsResponse(this.pulsarClient, this);
            this.update = new BackendStatsUpdate(this.pulsarClient, this);
        } catch (Exception exception) {
            this.logger.error("Cannot initialize handlers", exception);
        }
    }

    @Override
    public void disable() {
        try {
            if (this.request != null) this.request.close();
            if (this.update != null) this.update.close();
        } catch (Exception exception) {
            this.logger.error("Cannot close handlers", exception);
        }
    }

    @Override
    public Collection<BackendStat> getStats(@NotNull UUID holder, int gameId) {
        try {
            return parseStats(this.stats.find(Filters.and(
                    Filters.eq("uuid", holder.toString()),
                    Filters.eq("gameId", gameId)
            )).cursor());
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            Uroria.captureException(exception);
            return List.of();
        }
    }

    @Override
    public Collection<BackendStat> getStatsWithScoreGreaterThanValue(@NotNull UUID holder, int gameId, @NotNull String scoreKey, long value) {
        try {
            return parseStats(this.stats.find(Filters.and(
                Filters.eq("uuid", holder.toString()),
                    Filters.eq("gameId", gameId),
                    Filters.gt("scores." + scoreKey, value)
            )).cursor());
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            Uroria.captureException(exception);
            return List.of();
        }
    }

    @Override
    public Collection<BackendStat> getStatsWithScoreLowerThanValue(@NotNull UUID holder, int gameId, @NotNull String scoreKey, long value) {
        try {
            return parseStats(this.stats.find(Filters.and(
                    Filters.eq("uuid", holder.toString()),
                    Filters.eq("gameId", gameId),
                    Filters.lt("scores." + scoreKey, value)
            )).cursor());
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            Uroria.captureException(exception);
            return List.of();
        }
    }

    @Override
    public Collection<BackendStat> getStatsWithScore(@NotNull UUID holder, int gameId, @NotNull String scoreKey, long value) {
        try {
            return parseStats(this.stats.find(Filters.and(
                    Filters.eq("uuid", holder.toString()),
                    Filters.eq("gameId", gameId),
                    Filters.eq("scores." + scoreKey, value)
            )).cursor());
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            Uroria.captureException(exception);
            return List.of();
        }
    }

    @Override
    public Collection<BackendStat> getStatsInTimeRangeOf(@NotNull UUID holder, int gameId, long startMs, long endMs) {
        try {
            return parseStats(this.stats.find(Filters.and(
                    Filters.eq("uuid", holder.toString()),
                    Filters.eq("gameId", gameId),
                    Filters.gt("time", startMs),
                    Filters.lt("time", endMs)
            )).cursor());
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            Uroria.captureException(exception);
            return List.of();
        }
    }

    @Override
    public Collection<BackendStat> getStats(int gameId) {
        try {
            return parseStats(this.stats.find(Filters.and(
                    Filters.eq("gameId", gameId)
            )).cursor());
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            Uroria.captureException(exception);
            return List.of();
        }
    }

    @Override
    public Collection<BackendStat> getStatsWithScoreGreaterThanValue(int gameId, @NotNull String scoreKey, long value) {
        try {
            return parseStats(this.stats.find(Filters.and(
                    Filters.eq("gameId", gameId),
                    Filters.gt("scores." + scoreKey, value)
            )).cursor());
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            Uroria.captureException(exception);
            return List.of();
        }
    }

    @Override
    public Collection<BackendStat> getStatsWithScoreLowerThanValue(int gameId, @NotNull String scoreKey, long value) {
        try {
            return parseStats(this.stats.find(Filters.and(
                    Filters.eq("gameId", gameId),
                    Filters.lt("scores." + scoreKey, value)
            )).cursor());
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            Uroria.captureException(exception);
            return List.of();
        }
    }

    @Override
    public Collection<BackendStat> getStatsWithScore(int gameId, @NotNull String scoreKey, long value) {
        try {
            return parseStats(this.stats.find(Filters.and(
                    Filters.eq("gameId", gameId),
                    Filters.eq("scores." + scoreKey, value)
            )).cursor());
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            Uroria.captureException(exception);
            return List.of();
        }
    }

    @Override
    public Collection<BackendStat> getStatsInTimeRangeOf(int gameId, long startMs, long endMs) {
        try {
            return parseStats(this.stats.find(Filters.and(
                    Filters.eq("gameId", gameId),
                    Filters.gt("time", startMs),
                    Filters.lt("time", endMs)
            )).cursor());
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            Uroria.captureException(exception);
            return List.of();
        }
    }

    private Collection<BackendStat> parseStats(MongoCursor<Document> documents) {
        Collection<BackendStat> stats = new ArrayList<>();

        while (documents.hasNext()) {
            Document document = documents.next();
            BackendStat stat = Uroria.getGson().fromJson(document.toJson(), BackendStat.class);
            stats.add(stat);
        }

        documents.close();

        return stats;
    }

    @Override
    public BackendStat updateStat(@NotNull BackendStat stat) {
        updateLocal(stat);
        this.update.update(stat);
        return stat;
    }

    void updateLocal(BackendStat stat) {
        try {
            String json = Uroria.getGson().toJson(stat);
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
            if (this.stats.replaceOne(Filters.and(Filters.eq("uuid", stat.getUUID().toString()), Filters.eq("time", stat.getTime()), Filters.eq("gameId", stat.getGameId())), newDocument).wasAcknowledged()) {
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
}
