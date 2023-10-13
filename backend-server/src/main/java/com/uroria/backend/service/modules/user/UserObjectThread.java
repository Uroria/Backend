package com.uroria.backend.service.modules.user;

import com.uroria.backend.impl.io.BackendInputStream;
import com.uroria.backend.impl.io.BackendOutputStream;
import com.uroria.backend.service.modules.thread.ResponseThread;

import java.util.UUID;

public final class UserObjectThread extends ResponseThread {
    private final UserModule module;

    public UserObjectThread(UserModule module) {
        super(module, "user-requests");
        this.module = module;
    }

    @Override
    protected void request(BackendInputStream input, BackendOutputStream output) throws Exception {
        byte type = input.readByte();
        String object = input.readUTF();
        if (type == 1) {
            UUID uuid = module.getUUID(object);
            if (uuid == null) output.writeBoolean(false);
            else {
                output.writeBoolean(true);
                output.writeUTF(uuid.toString());
            }
            return;
        }
        output.writeUTF(object);
    }
}
