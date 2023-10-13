package com.uroria.backend.service.modules.server.group;

import com.uroria.backend.impl.io.BackendInputStream;
import com.uroria.backend.impl.io.BackendOutputStream;
import com.uroria.backend.service.modules.thread.ResponseThread;

public final class ServerGroupPartThread extends ResponseThread {
    private final ServerGroupModule module;

    public ServerGroupPartThread(ServerGroupModule module) {
        super(module, "servergroup-request");
        this.module = module;
    }

    @Override
    protected void request(BackendInputStream input, BackendOutputStream output) throws Exception {
        String name = input.readUTF();
        String key = input.readUTF();
        output.writeJsonElement(module.getPart("name", name, key));
    }
}
