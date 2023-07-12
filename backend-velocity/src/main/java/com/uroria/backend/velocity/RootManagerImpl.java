package com.uroria.backend.velocity;

import com.uroria.backend.impl.management.AbstractRootManager;
import com.uroria.backend.impl.management.BackendRootBackendRequest;
import com.uroria.backend.impl.management.BackendRootStopUpdate;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;

import java.util.Random;

public final class RootManagerImpl extends AbstractRootManager {
    private final ProxyServer proxyServer;
    private BackendRootBackendRequest request;
    private BackendRootStopUpdate stop;
    public RootManagerImpl(PulsarClient pulsarClient, Logger logger, ProxyServer proxyServer) {
        super(pulsarClient, logger);
        this.proxyServer = proxyServer;
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
        this.proxyServer.shutdown(Component.text("Internal Backend reboot. Please try again later.", NamedTextColor.RED));
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
