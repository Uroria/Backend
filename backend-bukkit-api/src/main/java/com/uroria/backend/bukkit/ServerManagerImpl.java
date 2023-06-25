package com.uroria.backend.bukkit;

import com.uroria.backend.bukkit.events.ServerStartEvent;
import com.uroria.backend.bukkit.events.ServerUpdateEvent;
import com.uroria.backend.common.BackendServer;
import com.uroria.backend.common.helpers.ServerStatus;
import com.uroria.backend.common.helpers.ServerType;
import com.uroria.backend.server.BackendAllServersRequest;
import com.uroria.backend.server.BackendServerKeepAlive;
import com.uroria.backend.server.BackendServerRequest;
import com.uroria.backend.server.BackendServerStart;
import com.uroria.backend.server.BackendServerUpdate;
import de.leonhard.storage.Json;
import org.apache.pulsar.client.api.PulsarClient;
import org.bukkit.Bukkit;
import org.slf4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public final class ServerManagerImpl extends BukkitServerManager {
    private final int localServerId;
    private BackendServerRequest request;
    private BackendServerUpdate update;
    private BackendServerStart start;
    private BackendAllServersRequest requestAll;
    private BackendServerKeepAlive keepAlive;

    public ServerManagerImpl(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient, logger);
        Properties properties = System.getProperties();
        String stringId = properties.getProperty("server.id");
        if (stringId == null) {
            this.localServerId = -1;
            return;
        }
        this.localServerId = Integer.parseInt(stringId);
    }

    @Override
    protected void start(String identifier) {
        try {
            this.request = new BackendServerRequest(this.pulsarClient, identifier);
            this.update = new BackendServerUpdate(this.pulsarClient, identifier, this::checkServer);
            this.start = new BackendServerStart(this.pulsarClient, identifier);
            this.requestAll = new BackendAllServersRequest(this.pulsarClient, identifier);
        } catch (Exception exception) {
            this.logger.error("Cannot initialize handlers", exception);
            BackendAPI.captureException(exception);
        }
        if (this.localServerId == -1) return;
        Json config = BackendBukkitPlugin.config();

        Map<String, Serializable> properties = new HashMap<>();

        for (String key : config.getSection("properties").singleLayerKeySet()) {
            try {
                String prefix = "properties." + key;
                Serializable value = (Serializable) config.get(prefix);
                if (key == null || value == null) continue;
                properties.put(key, value);
            } catch (Exception exception) {
                logger.warn("Cannot set property " + key, exception);
            }
        }

        HashMap<String, Boolean> creatorPermissions = new HashMap<>();
        HashMap<String, Boolean> crewPermissions = new HashMap<>();

        for (String node : config.getSection("creatorPermissions").singleLayerKeySet()) {
            String prefix = "creatorPermissions." + node;
            boolean value = config.getBoolean(prefix);
            creatorPermissions.put(node, value);
        }

        for (String node : config.getSection("crewPermissions").singleLayerKeySet()) {
            String prefix = "crewPermissions." + node;
            boolean value = config.getBoolean(prefix);
            crewPermissions.put(node, value);
        }

        try {
            BackendServer server = getThisServer();
            server.setStatus(ServerStatus.READY);
            properties.forEach(server::setProperty);
            if (server.getType() == ServerType.EVENT) {
                server.setProperty("creatorPermissions", creatorPermissions);
                server.setProperty("crewPermissions", crewPermissions);
            }
            updateServer(server);
            this.keepAlive = new BackendServerKeepAlive(pulsarClient, identifier, server.getIdentifier());
            this.keepAlive.start();
        } catch (Exception exception) {
            this.logger.error("Cannot start server", exception);
            Bukkit.shutdown();
        }
    }

    @Override
    protected void shutdown() {
        try {
            if (this.request != null) this.request.close();
            if (this.update != null) this.update.close();
            if (this.start != null) this.start.close();
            if (this.requestAll != null) this.requestAll.close();
            if (this.keepAlive != null) this.keepAlive.close();
        } catch (Exception exception) {
            this.logger.error("Cannot close handlers", exception);
            BackendAPI.captureException(exception);
        }
    }

    @Override
    public BackendServer getThisServer() {
        return getServer(this.localServerId, 5000).orElseThrow(() -> new RuntimeException("Own server not initialized yet"));
    }

    @Override
    protected void checkServer(BackendServer server) {
        if (this.servers.stream().noneMatch(server::equals)) {
            if (server.getStatus() == ServerStatus.CLOSED || server.getStatus() == ServerStatus.STOPPED) return;
        }

        for (BackendServer savedServer : this.servers) {
            if (!savedServer.equals(server)) continue;
            savedServer.modify(server);

            logger.info("Updating server " + savedServer.getDisplayName());

            CompletableFuture.runAsync(() -> {
                Bukkit.getPluginManager().callEvent(new ServerUpdateEvent(savedServer));
            });

            if (savedServer.getStatus() == ServerStatus.CLOSED || savedServer.getStatus() == ServerStatus.STOPPED) {
                CompletableFuture.runAsync(() -> {
                    if (this.localServerId == -1) return;
                    try {
                        if (getThisServer().equals(savedServer)) {
                            this.logger.info("Shutting down by remote update");
                            BackendBukkitPlugin.kickAll();
                            Bukkit.shutdown();

                            savedServer.setStatus(ServerStatus.STOPPED);
                            updateServer(savedServer);
                        }
                    } catch (Exception exception) {
                        logger.error("Cannot specify server! Shutting down", exception);
                        Bukkit.shutdown();
                    }
                });
            }

            if (savedServer.getStatus() == ServerStatus.STOPPED) {
                this.servers.remove(savedServer);
                logger.info("Removing server " + savedServer.getDisplayName());
            }

            return;
        }

        logger.info("Adding server " + server.getDisplayName());
        this.servers.add(server);
        CompletableFuture.runAsync(() -> {
            Bukkit.getPluginManager().callEvent(new ServerUpdateEvent(server));
        });
    }

    @Override
    public Optional<BackendServer> getServer(int id, int timeout) {
        if (id == -1) return Optional.empty();
        for (BackendServer server : this.servers) {
            if (server.getId().isEmpty()) continue;
            if (server.getId().get().equals(id)) return Optional.of(server);
        }

        return this.request.request(id);
    }

    @Override
    public List<Integer> getAllServers(int timeout) {
        int randomInt = new Random().nextInt();
        return this.requestAll.request(randomInt).orElseThrow(() -> new RuntimeException("Cannot get list"));
    }

    @Override
    public void updateServer(BackendServer server) {
        if (server == null) throw new NullPointerException("Server cannot be null");
        if (server.getId().isEmpty()) throw new IllegalStateException("Server not created yet");
        try {
            this.update.update(server);
            checkServer(server);
        } catch (Exception exception) {
            this.logger.error("Cannot update server", exception);
            BackendAPI.captureException(exception);
        }
    }

    @Override
    public BackendServer startServer(BackendServer server) {
        if (server == null) throw new NullPointerException("Server cannot be null");
        if (server.getId().isPresent()) throw new IllegalStateException("Server already started");
        try {
            Optional<BackendServer> request = this.start.request(server);
            if (request.isEmpty()) return null;
            server = request.get();
        } catch (Exception exception) {
            this.logger.error("Cannot start server", exception);
            BackendAPI.captureException(exception);
        }

        BackendServer finalServer = server;
        CompletableFuture.runAsync(() -> Bukkit.getPluginManager().callEvent(new ServerStartEvent(finalServer)));
        return server;
    }
}
