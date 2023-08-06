package com.uroria.backend.service.modules.user;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.uroria.backend.service.modules.AbstractManager;
import com.uroria.backend.user.User;
import com.uroria.backend.user.UserManager;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.bson.Document;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public final class BackendUserManager extends AbstractManager implements UserManager {
    private final MongoCollection<Document> users;
    private final RedisCommands<String, String> cache;
    private UserUpdate update;
    private UserUUIDResponse uuidResponse;
    private UserNameResponse nameResponse;

    public BackendUserManager(PulsarClient pulsarClient, MongoDatabase database, StatefulRedisConnection<String, String> cache) {
        super(pulsarClient, "UserModule");
        this.users = database.getCollection("users", Document.class);
        this.cache = cache.sync();
    }

    @Override
    public void enable() throws PulsarClientException {
        this.update = new UserUpdate(this.pulsarClient, this);
        this.uuidResponse = new UserUUIDResponse(this.pulsarClient, this);
        this.nameResponse = new UserNameResponse(this.pulsarClient, this);
    }

    @Override
    public void disable() throws PulsarClientException {
        if (this.update != null) this.update.close();
        if (this.uuidResponse != null) this.uuidResponse.close();
        if (this.nameResponse != null) this.nameResponse.close();
    }

    @Override
    public Optional<User> getUser(UUID uuid, int timeout) {
        if (uuid == null) return Optional.empty();
        try {
            User cachedUser = getCachedUser(uuid);
            if (cachedUser != null) return Optional.of(cachedUser);
            Document savedDocument = this.users.find(Filters.eq("uuid", uuid.toString())).first();
            if (savedDocument == null) {
                User user = new User(uuid);
                String json = gson.toJson(user);
                Document document = Document.parse(json);
                if (this.users.insertOne(document).wasAcknowledged()) {
                    this.logger.debug("Inserted " + user);
                    return Optional.of(user);
                }
                return Optional.empty();
            }
            return Optional.of(fromJson(savedDocument.toJson()));
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> getUser(String name, int timeout) {
        if (name == null) return Optional.empty();
        name = name.toLowerCase();
        try {
            User cachedUser = getCachedUser(name);
            if (cachedUser != null) return Optional.of(cachedUser);
            Document savedDocument = this.users.find(Filters.eq("name", name)).first();
            if (savedDocument == null) return Optional.empty();
            return Optional.of(fromJson(savedDocument.toJson()));
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            return Optional.empty();
        }
    }

    @Override
    public void updateUser(@NonNull User user) {
        updateDatabase(user);
        this.update.update(user);
    }

    void updateDatabase(@NonNull User user) {
        try {
            this.cache.del("user:" + user.getUUID());
            String json = gson.toJson(user);
            fromJson(json);
            Document document = Document.parse(json);
            if (this.users.replaceOne(Filters.eq("uuid", user.getUUID().toString()), document).wasAcknowledged()) {
                this.logger.debug("Replaced " + user);
                return;
            }
            this.logger.warn("Unable to replace " + user);
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
        }
    }

    private User fromJson(String json) {
        User user = gson.fromJson(json, User.class);
        String key = "user:" + user.getUUID();
        this.cache.set(key, json, lifespan(Duration.ofHours(4)));
        String username = user.getUsername();
        if (username != null) this.cache.set("user:" + username, key, lifespan(Duration.ofHours(24)));
        return user;
    }

    private User getCachedUser(UUID uuid) {
        String cachedObject = this.cache.get("user:" + uuid);
        if (cachedObject == null) return null;
        return gson.fromJson(cachedObject, User.class);
    }

    private User getCachedUser(String name) {
        String cachedObjectKey = this.cache.get("user:" + name);
        if (cachedObjectKey == null) return null;
        String cachedObject = this.cache.get(cachedObjectKey);
        if (cachedObject == null) return null;
        return gson.fromJson(cachedObject, User.class);
    }
}
