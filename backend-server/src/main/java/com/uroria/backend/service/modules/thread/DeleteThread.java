package com.uroria.backend.service.modules.thread;

import com.uroria.backend.impl.io.BackendInputStream;
import com.uroria.backend.service.communication.database.Database;
import com.uroria.backend.service.modules.SavingModule;

public final class DeleteThread extends UpdateThread {
    private final Database database;

    public DeleteThread(SavingModule module, String topic) {
        super(module, topic);
        this.database = module.getDatabase();
    }

    @Override
    protected void update(BackendInputStream input) throws Exception {
        String objectKey = input.readUTF();
        String object = input.readUTF();
        this.database.delete(objectKey, object);
    }
}
