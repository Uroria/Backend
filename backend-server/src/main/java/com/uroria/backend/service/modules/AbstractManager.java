package com.uroria.backend.service.modules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.uroria.backend.service.BackendServer;
import com.uroria.backend.service.gson.Object2BooleanMapTypeAdapterFactory;
import com.uroria.backend.service.gson.Object2IntMapTypeAdapterFactory;
import com.uroria.backend.service.gson.Object2ObjectMapTypeAdapterFactory;
import com.uroria.backend.service.gson.ObjectListTypeAdapterFactory;
import com.uroria.backend.service.gson.TransientExclusionStrategy;
import io.lettuce.core.SetArgs;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;

import java.time.Duration;

public abstract class AbstractManager {
    protected final Logger logger;
    protected final PulsarClient pulsarClient;
    protected final Gson gson;
    private final String moduleName;

    public AbstractManager(PulsarClient pulsarClient, String moduleName) {
        this.pulsarClient = pulsarClient;
        this.logger = BackendServer.getLogger();
        this.gson = buildGson();
        this.moduleName = moduleName;
    }

    private Gson buildGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.disableHtmlEscaping();
        builder.registerTypeAdapterFactory(new Object2ObjectMapTypeAdapterFactory());
        builder.registerTypeAdapterFactory(new Object2BooleanMapTypeAdapterFactory());
        builder.registerTypeAdapterFactory(new Object2IntMapTypeAdapterFactory());

        builder.registerTypeAdapterFactory(new ObjectListTypeAdapterFactory());

        TransientExclusionStrategy exclusionStrategy = new TransientExclusionStrategy();
        builder.addSerializationExclusionStrategy(exclusionStrategy);
        builder.addDeserializationExclusionStrategy(exclusionStrategy);
        return builder.create();
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
            this.logger.error("Shutting down " + moduleName + "...");
        }
    }

    public final String getModuleName() {
        return this.moduleName;
    }
}
