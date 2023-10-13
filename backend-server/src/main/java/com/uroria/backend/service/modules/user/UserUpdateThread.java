package com.uroria.backend.service.modules.user;

import com.google.gson.JsonElement;
import com.uroria.backend.impl.io.BackendInputStream;
import com.uroria.backend.service.modules.thread.UpdateThread;

import java.util.UUID;

public final class UserUpdateThread extends UpdateThread {
    private final UserModule module;

    public UserUpdateThread(UserModule module) {
        super(module, "user-update");
        this.module = module;
    }

    @Override
    protected void update(BackendInputStream input) throws Exception {
        UUID uuid = UUID.fromString(input.readUTF());
        String key = input.readUTF();
        JsonElement element = input.readJsonElement();
        this.module.checkPart(uuid, key, element);
    }
}
