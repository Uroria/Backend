package com.uroria.backend.service.modules.user;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.uroria.backend.impl.pulsar.PulsarResponseChannel;
import com.uroria.backend.service.modules.AbstractManager;
import com.uroria.base.io.InsaneByteArrayInputStream;
import com.uroria.base.io.InsaneByteArrayOutputStream;
import io.lettuce.core.api.StatefulRedisConnection;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

public final class UserManagerImpl extends AbstractManager {

    private final PulsarResponseChannel response;

    public UserManagerImpl(PulsarClient pulsarClient, @Nullable CryptoKeyReader keyReader, StatefulRedisConnection<String, String> redis, MongoCollection<Document> mongo) {
        super(pulsarClient, keyReader, "user/request", "user/update", redis, mongo, "UserModule");
        this.response = new PulsarResponseChannel(pulsarClient, keyReader, getModuleName(), "users/request") {
            @Override
            public void onRequest(InsaneByteArrayInputStream input, InsaneByteArrayOutputStream output) {
                try {
                    switch (input.readShort()) {
                        case 0 -> {
                            String uuidString = input.readUTF();
                            output.writeUTF(uuidString);
                        }
                        case 1 -> {
                            String username = input.readUTF();
                            UUID uuid = getUserUUID(username);
                            if (uuid == null) {
                                output.writeBoolean(false);
                                return;
                            }
                            output.writeBoolean(true);
                            output.writeUTF(uuid.toString());
                        }
                    }
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            }
        };
    }

    @Override
    protected void enable() throws PulsarClientException {

    }

    @Override
    protected void disable() throws PulsarClientException {
        this.response.close();
    }

    private UUID getUserUUID(String username) {
        try {
            Document document = this.mongo.find(Filters.eq("username", username)).first();
            if (document == null) return null;
            return UUID.fromString(document.getString("uuid"));
        } catch (Exception exception) {
            return null;
        }
    }

    @Override
    public JsonElement request(String key) {
        try {
            String cachedString = this.redis.get(key);
            if (cachedString != null) {
                JsonElement element = JsonParser.parseString(cachedString);
                if (element != null) return element;
            }
            String[] split = key.split("\\.");
            String sub = String.join(".", Arrays.copyOfRange(split, 2, split.length));
            UUID uuid;
            try {
                uuid = UUID.fromString(split[1]);
            } catch (Exception exception) {
                return JsonNull.INSTANCE;
            }
            Document document = this.mongo.find(Filters.eq("uuid", uuid.toString())).first();
            if (document == null) return JsonNull.INSTANCE;
            String documentJsonString = document.toJson();
            JsonObject user = JsonParser.parseString(documentJsonString).getAsJsonObject();
            this.redis.set("user:" + uuid, user.toString(), lifespan(Duration.ofDays(1)));
            return user.get(sub);
        } catch (Exception exception) {
            logger.error("Cannot response user", exception);
            return JsonNull.INSTANCE;
        }
    }

    @Override
    public void checkObject(String key, JsonElement value) {
        try {
            JsonObject object;
            if (this.redis.get(key) != null) {
                this.redis.set(key, value.toString(), lifespan(Duration.ofDays(2)));
            }
            String[] split = key.split("\\.");
            String sub = String.join(".", Arrays.copyOfRange(split, 2, split.length));
            UUID uuid;
            try {
                uuid = UUID.fromString(split[1]);
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
            Document document = this.mongo.find(Filters.eq("uuid", uuid.toString())).first();
            if (document == null) {
                object = new JsonObject();
                object.add(sub, value);
                if (this.mongo.insertOne(Document.parse(object.toString())).wasAcknowledged()) {
                    this.logger.info("Inserted User " + uuid);
                    return;
                }
                this.logger.warn("Unable to insert User " + uuid);
                return;
            }
            object = JsonParser.parseString(document.toJson()).getAsJsonObject();
            object.remove(sub);
            object.add(sub, value);

            if (this.mongo.replaceOne(Filters.eq("uuid", uuid.toString()), Document.parse(object.toString())).wasAcknowledged()) {
                this.logger.info("Replaced User " + uuid);
                return;
            }
            this.logger.warn("Unable to replace User " + uuid);
        } catch (Exception exception) {
            logger.error("Unhandled exception while trying to update User", exception);
        }
    }
}
