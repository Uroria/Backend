package com.uroria.backend.service.modules;

import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;
import com.uroria.backend.impl.pulsar.PulsarObjectResponses;
import com.uroria.backend.service.BackendServer;
import com.uroria.base.gson.GsonFactory;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.UUID;

public abstract class AbstractManager extends PulsarObjectResponses {
    protected final Logger logger;
    protected final PulsarClient pulsarClient;
    protected final RedisCommands<String, String> redis;
    protected final MongoCollection<Document> mongo;
    protected final Gson gson;
    private final String moduleName;

    public AbstractManager(PulsarClient pulsarClient, @Nullable CryptoKeyReader keyReader, String requestTopic, String updateTopic, StatefulRedisConnection<String, String> redis, MongoCollection<Document> mongo, String moduleName) {
        super(pulsarClient, keyReader, moduleName, requestTopic, updateTopic);
        this.pulsarClient = pulsarClient;
        this.redis = redis.sync();
        this.mongo = mongo;
        this.logger = BackendServer.getLogger();
        this.gson = GsonFactory.create();
        this.moduleName = moduleName;
    }

    protected abstract void enable() throws PulsarClientException;

    protected abstract void disable() throws PulsarClientException;

    public final SetArgs lifespan(Duration duration) {
        return SetArgs.Builder.ex(duration);
    }

    public final void start() {
        this.logger.info("Starting " + moduleName + "...");
        try {
            this.enable();
        } catch (Exception exception) {
            this.logger.error("Cannot enable " + moduleName, exception);
        }
    }

    public final void shutdown() {
        this.logger.info("Shutting down " + moduleName + "...");
        try {
            this.disable();
        } catch (Exception exception) {
            this.logger.error("Cannot shutdown " + moduleName, exception);
        }
    }

    public final String getModuleName() {
        return this.moduleName;
    }

    protected final UUID toUUID(Object object) {
        try {
            return (UUID) object;
        } catch (Exception exception) {
            return null;
        }
    }
}
