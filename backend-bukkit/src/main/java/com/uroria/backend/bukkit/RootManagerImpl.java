package com.uroria.backend.bukkit;

import com.uroria.backend.impl.management.AbstractRootManager;
import com.uroria.backend.impl.management.BackendRootBackendRequest;
import com.uroria.backend.impl.management.BackendRootStopUpdate;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.bukkit.Bukkit;
import org.slf4j.Logger;

import java.util.Random;

public final class RootManagerImpl extends AbstractRootManager {
    private BackendRootBackendRequest request;
    private BackendRootStopUpdate stop;
    public RootManagerImpl(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient, logger);
    }

    @Override
    protected void start(String identifier) throws PulsarClientException {
        this.request = new BackendRootBackendRequest(this.pulsarClient, identifier, 5000);
        this.stop = new BackendRootStopUpdate(this.pulsarClient, identifier, this::checkStopEverything);
    }

    @Override
    protected void shutdown() throws PulsarClientException {
        this.request.close();
        this.stop.close();
    }

    @Override
    protected void checkStopEverything() {
        this.logger.warn("ACCEPTED ROOT LEVEL SHUTDOWN! EVERYTHING GETS CLOSED!");
        Bukkit.shutdown();
    }

    @Override
    public void stopEverything() {
        this.stop.update(true);
    }

    @Override
    public boolean isBackendOnline() {
        int random = new Random().nextInt();
        return this.request.request(random, 5000).orElse(false);
    }
}
