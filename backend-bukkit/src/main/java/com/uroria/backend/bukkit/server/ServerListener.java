package com.uroria.backend.bukkit.server;

import com.uroria.backend.server.Server;
import com.uroria.backend.server.ServerStatus;
import com.uroria.backend.wrapper.server.ServerUpdateEvent;
import com.uroria.base.event.Listener;
import org.bukkit.Bukkit;
import org.slf4j.Logger;

public final class ServerListener extends Listener<ServerUpdateEvent> {
    private final Logger logger;
    private final ServerManager serverManager;

    ServerListener(Logger logger, ServerManager serverManager) {
        super(ServerUpdateEvent.class, 1);
        this.logger = logger;
        this.serverManager = serverManager;
    }

    @Override
    public void onEvent(ServerUpdateEvent serverUpdateEvent) {
        Server server = serverUpdateEvent.getServer();
        switch (server.getStatus()) {
            case CLOSED, STOPPED -> {
                if (serverManager.getServer() == null) return;
                if (serverManager.localServerId != -1) {
                    try {
                        if (server.equals(serverManager.getServer())) {
                            this.logger.info("Shutting down by remote update.");
                            Bukkit.shutdown();
                            serverManager.server = null;
                            server.setStatus(ServerStatus.STOPPED);
                            server.update();
                        }
                    } catch (Exception exception) {
                        logger.error("Cannot specify server! Shutting down!", exception);
                        Bukkit.shutdown();
                    }
                }
            }
        }
    }
}
