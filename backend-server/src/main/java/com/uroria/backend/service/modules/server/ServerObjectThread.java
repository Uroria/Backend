package com.uroria.backend.service.modules.server;

import com.uroria.backend.impl.io.BackendInputStream;
import com.uroria.backend.impl.io.BackendOutputStream;
import com.uroria.backend.service.modules.thread.ResponseThread;

import java.util.Collection;

public final class ServerObjectThread extends ResponseThread {
    private final ServerModule module;

    public ServerObjectThread(ServerModule module) {
        super(module, "server-requestall");
        this.module = module;
    }

    @Override
    protected void request(BackendInputStream input, BackendOutputStream output) throws Exception {
        input.readBoolean();
        Collection<Long> all = this.module.getAll();
        output.writeInt(all.size());
        for (long id : all) {
            output.writeLong(id);
        }
    }
}
