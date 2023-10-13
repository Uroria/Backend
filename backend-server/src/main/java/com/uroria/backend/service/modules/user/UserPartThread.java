package com.uroria.backend.service.modules.user;

import com.uroria.backend.impl.io.BackendInputStream;
import com.uroria.backend.impl.io.BackendOutputStream;
import com.uroria.backend.service.modules.thread.ResponseThread;

import java.util.UUID;

public final class UserPartThread extends ResponseThread {
    private final UserModule module;

    public UserPartThread(UserModule module) {
        super(module, "user-request");
        this.module = module;
    }

    @Override
    protected void request(BackendInputStream input, BackendOutputStream output) throws Exception {
        UUID uuid = UUID.fromString(input.readUTF());
        String key = input.readUTF();
        output.writeJsonElement(module.getPart("uuid", uuid, key));
        output.close();
    }
}
