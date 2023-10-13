package com.uroria.backend.service.modules.perm;

import com.google.gson.JsonElement;
import com.uroria.backend.impl.io.BackendInputStream;
import com.uroria.backend.impl.io.BackendOutputStream;
import com.uroria.backend.service.modules.thread.ResponseThread;

public final class PermGroupObjectThread extends ResponseThread {
    private final PermModule module;

    public PermGroupObjectThread(PermModule module) {
        super(module, "permgroup-requests");
        this.module = module;
    }

    @Override
    protected void request(BackendInputStream input, BackendOutputStream output) throws Exception {
        String name = input.readUTF();
        JsonElement element = this.module.getPart(name, "uuid");
        if (element.isJsonNull()) output.writeBoolean(false);
        else {
            output.writeBoolean(true);
            output.writeUTF(name);
        }
    }
}
