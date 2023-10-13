package com.uroria.backend.service.modules.perm;

import com.uroria.backend.impl.io.BackendInputStream;
import com.uroria.backend.impl.io.BackendOutputStream;
import com.uroria.backend.service.modules.thread.ResponseThread;

public final class PermGroupPartThread extends ResponseThread {
    private final PermModule module;

    public PermGroupPartThread(PermModule module) {
        super(module, "permgroup-request");
        this.module = module;
    }

    @Override
    protected void request(BackendInputStream input, BackendOutputStream output) throws Exception {
        String name = input.readUTF();
        String key = input.readUTF();
        output.writeJsonElement(module.getPart("name", name, key));
    }
}
