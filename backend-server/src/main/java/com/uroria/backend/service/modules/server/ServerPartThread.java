package com.uroria.backend.service.modules.server;

import com.uroria.backend.impl.io.BackendInputStream;
import com.uroria.backend.impl.io.BackendOutputStream;
import com.uroria.backend.service.modules.thread.ResponseThread;

public final class ServerPartThread extends ResponseThread {
    private final ServerModule module;

    public ServerPartThread(ServerModule module) {
        super(module, "server-request");
        this.module = module;
    }

    @Override
    protected void request(BackendInputStream input, BackendOutputStream output) throws Exception {
        long id = Long.parseLong(input.readUTF());
        String key = input.readUTF();
        output.writeJsonElement(module.getPart("identifier", id, key));
    }
}
