package com.uroria.backend.service.modules.perm;

import com.google.gson.JsonElement;
import com.uroria.backend.impl.io.BackendInputStream;
import com.uroria.backend.service.modules.thread.UpdateThread;

public final class PermGroupUpdateThread extends UpdateThread {
    private final PermModule module;

    public PermGroupUpdateThread(PermModule module) {
        super(module, "permgroup-update");
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
