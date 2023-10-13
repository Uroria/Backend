package com.uroria.backend.service.modules.server.group;

import com.uroria.backend.impl.io.BackendInputStream;
import com.uroria.backend.impl.io.BackendOutputStream;
import com.uroria.backend.service.modules.thread.ResponseThread;

import java.util.Collection;

public final class ServerGroupObjectThread extends ResponseThread {
    private final ServerGroupModule module;

    public ServerGroupObjectThread(ServerGroupModule module) {
        super(module, "servergroup-requestall");
        this.module = module;
    }

    @Override
    protected void request(BackendInputStream input, BackendOutputStream output) throws Exception {
        input.readBoolean();
        Collection<String> all = this.module.getAll();
        output.writeInt(all.size());
        for (String string : all) {
            output.writeUTF(string);
        }
    }
}
