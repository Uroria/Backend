package com.uroria.backend.bukkit;

import com.uroria.backend.bukkit.events.ServerStartEvent;
import com.uroria.backend.bukkit.events.ServerUpdateEvent;
import com.uroria.backend.common.BackendServer;
import com.uroria.backend.common.helpers.ServerStatus;
import com.uroria.backend.server.*;
import org.apache.pulsar.client.api.PulsarClient;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.*;
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
        try {
            BackendServer server = getThisServer();
            server.setStatus(ServerStatus.READY);
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
        this.servers.removeIf(server1 -> server1.equals(server));
        if (server.getId().isPresent() && server.getId().get().equals(this.localServerId) && server.getStatus() == ServerStatus.CLOSED) {
            server.setStatus(ServerStatus.STOPPED);
            updateServer(server);
            this.logger.info("Shutting down on remote command");
            try {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.getOnlinePlayers().forEach(player -> {
                            player.kickPlayer("");
                            try {
                                Thread.sleep(500);
                            } catch (Exception ignored) {
                            }
                        });
                    }
                }.runTask(JavaPlugin.getPlugin(BackendBukkitPlugin.class));
            } catch (Exception ignored) {}
            Bukkit.shutdown();
            return;
        }
        if (server.getStatus() != ServerStatus.STOPPED && server.getStatus() != ServerStatus.CLOSED) this.servers.add(server);
        this.logger.info("Remaining servers " + Arrays.toString(this.servers.stream().map(registeredServer -> registeredServer.getId().orElse(-1)).toArray()));
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
            checkServer(server);
            this.update.update(server);
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

        Bukkit.getPluginManager().callEvent(new ServerStartEvent(server));
        return server;
    }
}
