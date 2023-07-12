package com.uroria.backend.server.modules.management;

import com.uroria.backend.management.RootManager;
import com.uroria.backend.server.modules.AbstractManager;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

public final class BackendRootManager extends AbstractManager implements RootManager {
    private final PulsarClient pulsarClient;
    private BackendRootBackendResponse response;
    private BackendRootStopUpdate stop;

    public BackendRootManager(PulsarClient pulsarClient, Logger logger) {
        super(logger, "BackendModule");
        this.pulsarClient = pulsarClient;
    }

    @Override
    public void enable() {
        try {
            this.response = new BackendRootBackendResponse(this.pulsarClient);
            this.stop = new BackendRootStopUpdate(this.pulsarClient);
        } catch (Exception exception) {
            this.logger.error("Cannot initialize handlers", exception);
        }
    }

    @Override
    public void disable() {
        try {
            this.response.close();
            this.stop.close();
        } catch (Exception exception) {
            this.logger.error("Cannot close handlers", exception);
        }
    }

    @Override
    public void stopEverything() {
        this.stop.update(true);
    }

    @Override
    public boolean isBackendOnline() {
        return true;
    }
}
