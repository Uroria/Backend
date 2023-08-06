package com.uroria.backend.bukkit.server;

import com.uroria.backend.bukkit.BackendBukkitPlugin;
import com.uroria.backend.bukkit.configuration.ServerConfiguration;
import com.uroria.backend.bukkit.utils.BukkitUtils;
import com.uroria.backend.impl.server.AbstractServerManager;
import com.uroria.backend.impl.server.AllServersRequestChannel;
import com.uroria.backend.impl.server.KeepAlive;
import com.uroria.backend.impl.server.ServerRequestChannel;
import com.uroria.backend.impl.server.ServerStartChannel;
import com.uroria.backend.impl.server.ServerUpdateChannel;
import com.uroria.backend.server.Server;
import com.uroria.backend.server.ServerManager;
import com.uroria.backend.server.ServerStatus;
import com.uroria.backend.server.ServerType;
import com.uroria.backend.utils.ThreadUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.bukkit.Bukkit;
import org.slf4j.Logger;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public final class ServerManagerImpl extends AbstractServerManager implements ServerManager {
    private final int localServerId;
    private ServerRequestChannel request;
    private ServerUpdateChannel update;
    private AllServersRequestChannel requestAll;
    private ServerStartChannel start;
    private KeepAlive keepAlive;
    private Server thisServer;

    public ServerManagerImpl(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient, logger);
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

    @Override
    public void start(String identifier) throws PulsarClientException {
        this.request = new ServerRequestChannel(this.pulsarClient, identifier);
        this.update = new ServerUpdateChannel(this.pulsarClient, identifier, this::checkServer);
        this.requestAll = new AllServersRequestChannel(this.pulsarClient, identifier);
        this.start = new ServerStartChannel(this.pulsarClient, identifier);

        if (this.localServerId == -1) return;
        if (this.localServerId == -2) {
            this.thisServer = new Server("Offline", -1, ServerType.OTHER);
            this.thisServer.setStatus(ServerStatus.READY);
            return;
        }
        this.thisServer = getServer(this.localServerId, 5000).orElse(null);
        if (this.thisServer == null) {
            logger.error("Cannot find server that fits for local id " + this.localServerId);
            ThreadUtils.sleep(5000);
            Bukkit.shutdown();
            return;
        }

        try {
            ServerConfiguration.getProperties().forEach((key, value) -> {
                thisServer.setProperty(key, (Serializable) value);
            });
            thisServer.update();

            this.keepAlive = new KeepAlive(this.pulsarClient, identifier, this.localServerId);
            this.keepAlive.start();
        } catch (Exception exception) {
            this.logger.error("Cannot update this server ", exception);
            Bukkit.shutdown();
        }
    }

    public Server getThisServer() {
        return this.thisServer;
    }

    @Override
    public void shutdown() throws PulsarClientException {
        if (this.request != null) this.request.close();
        if (this.update != null) this.update.close();
        if (this.requestAll != null) this.requestAll.close();
        if (this.start != null) this.start.close();
        if (this.keepAlive != null) this.keepAlive.close();
    }

    @Override
    protected void checkServer(@NonNull Server server) {
        if (this.servers.stream().noneMatch(server::equals)) return;

        if (server.isDeleted()) {
            this.servers.remove(server);
            BukkitUtils.callAsyncEvent(new ServerUpdateEvent(server));
            return;
        }

        for (Server cachedServer : this.servers) {
            if (!cachedServer.equals(server)) continue;
            cachedServer.modify(server);

            logger.info("Updated " + server);

            BukkitUtils.callAsyncEvent(new ServerUpdateEvent(cachedServer));
            return;
        }

        logger.info("Adding " + server);
        this.servers.add(server);
        BukkitUtils.callAsyncEvent(new ServerUpdateEvent(server));
    }

    @Override
    public Optional<Server> getServer(long identifier, int timeout) {
        for (Server server : this.servers) {
            if (server.getIdentifier() == identifier) return Optional.of(server);
        }

        if (BackendBukkitPlugin.isOffline()) return Optional.empty();

        Optional<Server> request = this.request.request(identifier, timeout);
        request.ifPresent(this.servers::add);
        return request;
    }

    @Override
    public List<Server> getServers() {
        if (BackendBukkitPlugin.isOffline()) return new ObjectArrayList<>();

        Optional<List<Server>> request = this.requestAll.request(1, 20000);
        return request.orElse(new ObjectArrayList<>());
    }

    @Override
    public Server startServer(@NonNull Server server) throws IllegalStateException {
        if (server.getID() != -1) throw new IllegalStateException("Server already started");
        try {
            if (BackendBukkitPlugin.isOffline()) return server;
            Optional<Server> request = this.start.request(server, 1000000);
            if (request.isEmpty()) return null;
            server = request.get();
        } catch (Exception exception) {
            this.logger.error("Cannot start server ", exception);
        }

        return server;
    }

    @Override
    public void updateServer(@NonNull Server server) {
        if (server.getID() == -1) throw new IllegalStateException("Server was never started");
        try {
            checkServer(server);
            if (BackendBukkitPlugin.isOffline()) return;
            this.update.update(server);
        } catch (Exception exception) {
            this.logger.error("Cannot update server " + server, exception);
        }
    }
}
