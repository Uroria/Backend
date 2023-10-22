package com.uroria.backend.service.modules.server;

import com.uroria.backend.service.BackendServer;
import com.uroria.backend.service.modules.LocalCachingModule;

import java.util.Collection;


public final class ServerModule extends LocalCachingModule {
    private final ServerObjectThread objectThread;
    private final ServerPartThread partThread;
    private final ServerUpdateThread updateThread;
    
    public ServerModule(BackendServer server) {
        super(server, "ServerModule", "server");
        this.objectThread = new ServerObjectThread(this);
        this.partThread = new ServerPartThread(this);
        this.updateThread = new ServerUpdateThread(this);
    }

    @Override
    protected void enable() throws Exception {
        this.objectThread.start();
        this.partThread.start();
        this.updateThread.start();
    }

    @Override
    protected void disable() throws Exception {
        this.objectThread.getResponseChannel().close();
        this.partThread.getResponseChannel().close();
        this.updateThread.getUpdateChannel().close();
    }

    public Collection<Long> getAll() {
        return this.allIdentifiers.stream().map(obj -> (Long) obj).toList();
    }
}
