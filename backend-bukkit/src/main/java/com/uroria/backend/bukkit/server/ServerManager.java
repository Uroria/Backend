package com.uroria.backend.bukkit.server;

import com.uroria.backend.Backend;
import com.uroria.backend.bukkit.BackendBukkitPlugin;
import com.uroria.backend.impl.server.KeepAlive;
import com.uroria.backend.server.Server;
import com.uroria.backend.server.ServerStatus;
import com.uroria.backend.server.ServerType;
import com.uroria.backend.wrapper.BackendWrapper;
import com.uroria.backend.wrapper.configuration.ServerConfiguration;
import com.uroria.base.event.EventManager;
import com.uroria.base.utils.ThreadUtils;
import lombok.Getter;
import org.apache.pulsar.client.api.PulsarClientException;
import org.bukkit.Bukkit;
import org.slf4j.Logger;

import java.io.Serializable;
import java.util.Properties;

public final class ServerManager {
    private final Logger logger;
    private final EventManager eventManager;
    final int localServerId;
    private KeepAlive keepAlive;
    @Getter Server server;

    public ServerManager(Logger logger, EventManager eventManager) {
        this.logger = logger;
        this.eventManager = eventManager;
        Properties properties = System.getProperties();
        if (BackendBukkitPlugin.isOffline()) {
            this.localServerId = -2;
            return;
        }
        String stringId = properties.getProperty("server.id");
        if (stringId == null) {
            this.localServerId = -1;
            return;
        }
        this.localServerId = Integer.parseInt(stringId);
    }

    public void start(String identifier) {
        if (this.localServerId == -1) return;
        if (this.localServerId == -2) {
            this.server = new Server("Offline", -1, ServerType.OTHER);
            this.server.setStatus(ServerStatus.READY);
            return;
        }
        this.server = Backend.getAPI().getServerManager().getCloudServer(this.localServerId, 5000).orElse(null);
        if (this.server == null) {
            logger.error("Cannot find server that fits for local id " + this.localServerId);
            ThreadUtils.sleep(5000);
            Bukkit.shutdown();
            return;
        }

        switch (server.getStatus()) {
            case STOPPED, CLOSED -> {
                logger.warn("Server already closed. Stopping it.");
                Bukkit.shutdown();
                return;
            }
        }

        this.eventManager.subscribe(new ServerListener(logger, this));

        server.setStatus(ServerStatus.READY);

        try {
            ServerConfiguration.getProperties().forEach((key, value) -> {
                server.setProperty(key, (Serializable) value);
            });
            server.update();

            this.keepAlive = new KeepAlive(BackendWrapper.getAPI().getPulsarClient(), identifier, server.getIdentifier());
        } catch (Exception exception) {
            this.logger.error("Cannot update this server ", exception);
            Bukkit.shutdown();
        }
    }

    public void shutdown() throws PulsarClientException {
        Bukkit.getOnlinePlayers().forEach(player -> player.kickPlayer("Shutdown"));
        this.keepAlive.close();
    }
}
