package com.uroria.backend.service.modules.server.group;

import com.google.gson.JsonElement;
import com.uroria.backend.impl.io.BackendInputStream;
import com.uroria.backend.service.modules.thread.UpdateThread;

public final class ServerGroupUpdateThread extends UpdateThread {
    private final ServerGroupModule module;

    public ServerGroupUpdateThread(ServerGroupModule module) {
        super(module, "servergroup-update");
        this.module = module;
    }

    @Override
    protected void update(BackendInputStream input) throws Exception {
        String name = input.readUTF();
        String key = input.readUTF();
        JsonElement element = input.readJsonElement();
        this.module.checkPart("name", name, key, element);
    }
}
