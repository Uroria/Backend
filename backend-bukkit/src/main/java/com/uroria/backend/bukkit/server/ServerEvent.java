package com.uroria.backend.bukkit.server;

import com.uroria.backend.server.Server;
import lombok.Getter;
import org.bukkit.event.Event;

public abstract class ServerEvent extends Event {

    private @Getter final Server server;

    public ServerEvent(Server server) {
        super(true);
        this.server = server;
    }
}
