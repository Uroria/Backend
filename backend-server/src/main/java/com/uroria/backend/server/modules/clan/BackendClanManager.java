package com.uroria.backend.server.modules.clan;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.uroria.backend.clan.BackendClan;
import com.uroria.backend.clan.ClanManager;
import com.uroria.backend.pluginapi.BackendRegistry;
import com.uroria.backend.pluginapi.events.clan.ClanCreateEvent;
import com.uroria.backend.pluginapi.events.clan.ClanDeleteEvent;
import com.uroria.backend.pluginapi.events.clan.ClanUpdateEvent;
import com.uroria.backend.server.Uroria;
import com.uroria.backend.server.events.BackendEventManager;
import com.uroria.backend.server.modules.AbstractManager;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public final class BackendClanManager extends AbstractManager implements ClanManager {
    private final PulsarClient pulsarClient;
    private final MongoCollection<Document> clans;
    private final RedisCommands<String, String> cachedClans;
    private final BackendEventManager eventManager;
    private BackendClanTagResponse tagResponse;
    private BackendClanOperatorResponse operatorResponse;
    private BackendClanUpdate clanUpdate;

    public BackendClanManager(Logger logger, PulsarClient pulsarClient, MongoDatabase database, StatefulRedisConnection<String, String> cache) {
        super(logger, "ClanModule");
        this.pulsarClient = pulsarClient;
        this.clans = database.getCollection("clans", Document.class);
        this.cachedClans = cache.sync();
        this.eventManager = BackendRegistry.get(BackendEventManager.class).orElseThrow(() -> new NullPointerException("EventManager not initialized"));
    }

    @Override
    public void enable() {
        try {
            this.tagResponse = new BackendClanTagResponse(this.pulsarClient, this);
            this.operatorResponse = new BackendClanOperatorResponse(this.pulsarClient, this);
            this.clanUpdate = new BackendClanUpdate(this.pulsarClient, this);
        } catch (Exception exception) {
            this.logger.error("Cannot initialize handlers", exception);
        }
    }

    @Override
    public void disable() {
        try {
            if (this.tagResponse != null) this.tagResponse.close();
            if (this.operatorResponse != null) this.operatorResponse.close();
            if (this.clanUpdate != null) this.clanUpdate.close();
        } catch (Exception exception) {
            this.logger.error("Cannot close handlers", exception);
        }
    }

    @Override
    public Optional<BackendClan> getClan(@NonNull String tag, int timeout) {
        return getClan(tag);
    }

    @Override
    public Optional<BackendClan> getClan(@NonNull UUID operator, int timeout) {
        return getClan(operator);
    }

    @Override
    public Optional<BackendClan> getClan(@NotNull String tag) {
        try {
            BackendClan cachedClan = getCachedClan(tag);
            if (cachedClan != null) return Optional.of(cachedClan);
            Document savedDocument = this.clans.find(Filters.eq("tag", tag)).first();
            if (savedDocument == null) {
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
    public Optional<BackendClan> getClan(@NotNull UUID operator) {
        try {
            BackendClan cachedClan = getCachedClan(operator);
            if (cachedClan != null) return Optional.of(cachedClan);
            Document savedDocument = this.clans.find(Filters.eq("operator", operator.toString())).first();
            if (savedDocument == null) {
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
    public BackendClan updateClan(@NotNull BackendClan clan) {
        updateDatabase(clan);
        this.clanUpdate.update(clan);
        return clan;
    }

    void updateLocal(BackendClan clan) {
        updateDatabase(clan);
    }

    private void updateDatabase(BackendClan clan) {
        try {
            this.cachedClans.del("clan:" + clan.getTag());
            this.cachedClans.del("clan:" + clan.getOperator());
            String json = Uroria.getGson().toJson(clan);
            if (clan.isDeleted()) {
                if (this.clans.deleteOne(Filters.eq("name", clan.getName())).wasAcknowledged()) {
                    ClanDeleteEvent clanDeleteEvent = new ClanDeleteEvent(clan);
                    this.eventManager.callEventAsync(clanDeleteEvent);
                    this.logger.debug("Deleted clan " + clan.getName() + " " + clan.getTag() + " " + clan.getOperator());
                    return;
                }
                this.logger.warn("Could not delete clan " + clan.getName());
                return;
            }
            Document newDocument = Document.parse(json);
            Document document = this.clans.find(Filters.eq("name", clan.getName())).first();
            fromJson(json);
            if (document == null) {
                if (this.clans.insertOne(newDocument).wasAcknowledged()) {
                    ClanCreateEvent clanCreateEvent = new ClanCreateEvent(clan);
                    this.eventManager.callEventAsync(clanCreateEvent);
                    this.logger.debug("Registered clan " + clan.getName() + " " + clan.getTag() + " " + clan.getOperator());
                    return;
                }
                this.logger.warn("Could not register clan " + clan.getName());
                return;
            }
            if (this.clans.replaceOne(Filters.eq("name", clan.getName()), newDocument).wasAcknowledged()) {
                ClanUpdateEvent clanUpdateEvent = new ClanUpdateEvent(clan);
                this.eventManager.callEventAsync(clanUpdateEvent);
                return;
            }
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            Uroria.captureException(exception);
        }
    }

    private BackendClan fromJson(String json) {
        BackendClan clan = Uroria.getGson().fromJson(json, BackendClan.class);
        this.cachedClans.set("clan:" + clan.getTag(), json, SetArgs.Builder.ex(Duration.ofHours(48)));
        this.cachedClans.set("clan:" + clan.getOperator(), json, SetArgs.Builder.ex(Duration.ofHours(48)));
        return clan;
    }

    private BackendClan getCachedClan(UUID operator) {
        String cachedObject = this.cachedClans.get("clan:" + operator);
        if (cachedObject == null) return null;
        return Uroria.getGson().fromJson(cachedObject, BackendClan.class);
    }

    private BackendClan getCachedClan(String tag) {
        String cachedObject = this.cachedClans.get("clan:" + tag);
        if (cachedObject == null) return null;
        return Uroria.getGson().fromJson(cachedObject, BackendClan.class);
    }
}
