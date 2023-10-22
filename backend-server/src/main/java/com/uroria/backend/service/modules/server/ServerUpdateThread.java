package com.uroria.backend.service.modules.server;

import com.google.gson.JsonElement;
import com.uroria.backend.impl.io.BackendInputStream;
import com.uroria.backend.service.modules.thread.UpdateThread;

public final class ServerUpdateThread extends UpdateThread {
    private final ServerModule module;

    public ServerUpdateThread(ServerModule module) {
        super(module, "server-update");
        this.module = module;
    }

    @Override
    protected void update(BackendInputStream input) throws Exception {
        long id = Long.parseLong(input.readUTF());
        String key = input.readUTF();
        JsonElement element = input.readJsonElement();
        this.module.checkPart("identifier", id, key, element);
    }
}
