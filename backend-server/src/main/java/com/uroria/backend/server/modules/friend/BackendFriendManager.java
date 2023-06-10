package com.uroria.backend.server.modules.friend;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.uroria.backend.common.BackendFriend;
import com.uroria.backend.pluginapi.BackendRegistry;
import com.uroria.backend.pluginapi.events.friend.FriendRegisterEvent;
import com.uroria.backend.pluginapi.events.friend.FriendUpdateEvent;
import com.uroria.backend.pluginapi.modules.FriendManager;
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

public final class BackendFriendManager implements FriendManager {
    private final Logger logger;
    private final PulsarClient pulsarClient;
    private final MongoCollection<Document> friends;
    private final RedisCommands<String, String> cachedFriends;
    private final BackendEventManager eventManager;
    private BackendFriendResponse friendResponse;
    private BackendFriendUpdate friendUpdate;

    public BackendFriendManager(Logger logger, PulsarClient pulsarClient, MongoDatabase database, StatefulRedisConnection<String, String> cache) {
        this.logger = logger;
        this.pulsarClient = pulsarClient;
        this.friends = database.getCollection("friends", Document.class);
        this.cachedFriends = cache.sync();
        this.eventManager = BackendRegistry.get(BackendEventManager.class).orElseThrow(() -> new NullPointerException("EventManager not initialized"));
    }

    public void start() {
        try {
            this.friendResponse = new BackendFriendResponse(this.pulsarClient, this);
            this.friendUpdate = new BackendFriendUpdate(this.pulsarClient, this);
        } catch (Exception exception) {
            this.logger.error("Cannot initialize handlers", exception);
        }
    }

    public void shutdown() {
        try {
            if (this.friendResponse != null) this.friendResponse.close();
            if (this.friendUpdate != null) this.friendUpdate.close();
        } catch (Exception exception) {
            this.logger.error("Cannot close handlers", exception);
        }
    }

    @Override
    public Optional<BackendFriend> getFriend(UUID uuid) {
        try {
            BackendFriend cachedFriend = getCachedFriend(uuid);
            if (cachedFriend != null) return Optional.of(cachedFriend);
            Document savedDocument = this.friends.find(Filters.eq("holder", uuid.toString())).first();
            if (savedDocument == null) {
                BackendFriend friend = new BackendFriend(uuid);
                String json = Uroria.getGson().toJson(friend);
                Document document = Document.parse(json);
                if (this.friends.insertOne(document).wasAcknowledged()) {
                    FriendRegisterEvent friendRegisterEvent = new FriendRegisterEvent(friend);
                    this.eventManager.callEventAsync(friendRegisterEvent);
                    this.logger.info("Registered new friend " + friend.getHolder());
                    return Optional.of(friend);
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
    public void updateFriend(BackendFriend friend) {
        updateDatabase(friend);
        this.friendUpdate.update(friend);
    }

    void updateLocal(BackendFriend friend) {
        updateDatabase(friend);
    }

    private void updateDatabase(BackendFriend friend) {
        try {
            this.cachedFriends.del("friend:" + friend.getHolder());
            String json = Uroria.getGson().toJson(friend);
            fromJson(json);
            Document document = Document.parse(json);
            if (this.friends.replaceOne(Filters.eq("holder", friend.getHolder().toString()), document).wasAcknowledged()) {
                FriendUpdateEvent friendUpdateEvent = new FriendUpdateEvent(friend);
                this.eventManager.callEventAsync(friendUpdateEvent);
                this.logger.debug("Updated friend " + friend.getHolder());
                return;
            }
            this.logger.warn("Could not update friend " + friend.getHolder());
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            Uroria.captureException(exception);
        }
    }

    private BackendFriend fromJson(String json) {
        BackendFriend friend = Uroria.getGson().fromJson(json, BackendFriend.class);
        this.cachedFriends.set("friend:" + friend.getHolder(), json, SetArgs.Builder.ex(Duration.ofHours(24)));
        return friend;
    }

    private BackendFriend getCachedFriend(UUID uuid) {
        String cachedObject = this.cachedFriends.get("friend:" + uuid);
        if (cachedObject == null) return null;
        return Uroria.getGson().fromJson(cachedObject, BackendFriend.class);
    }
}
