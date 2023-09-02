package com.uroria.backend.service.modules;

import com.google.gson.Gson;
import com.uroria.backend.service.BackendServer;
import com.uroria.base.gson.GsonFactory;
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
}
