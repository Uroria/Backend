package com.uroria.backend.service.modules.server;

import com.google.gson.JsonElement;
import com.uroria.backend.service.BackendServer;
import com.uroria.backend.service.modules.CachingModule;


public final class ServerModule extends CachingModule {
    private final ServerPartThread partThread;
    
    public ServerModule(BackendServer server) {
        super(server, "ServerModule", "server");
        this.partThread = new ServerPartThread(this);
    }

    @Override
    protected void enable() throws Exception {
        this.partThread.start();
    }

    @Override
    protected void disable() throws Exception {
        this.partThread.getResponseChannel().close();
    }

    @Override
    public JsonElement getPart(String identifierKey, Object identifier, String key) {
        return null;
    }

    @Override
    public void checkPart(String identifierKey, Object identifier, String key, JsonElement value) {

    }
}
