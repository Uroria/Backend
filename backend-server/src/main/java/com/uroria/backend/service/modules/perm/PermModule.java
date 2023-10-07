package com.uroria.backend.service.modules.perm;

import com.uroria.backend.service.BackendServer;
import com.uroria.backend.service.modules.SavingModule;

public final class PermModule extends SavingModule {

    private final PermGroupObjectThread objectThread;
    private final PermGroupUpdateThread updateThread;
    private final PermGroupPartThread partThread;

    public PermModule(BackendServer server) {
        super(server, "PermModule", "perm_groups");
        this.objectThread = new PermGroupObjectThread(this);
        this.updateThread = new PermGroupUpdateThread(this);
        this.partThread = new PermGroupPartThread(this);
    }

    @Override
    protected void enable() throws Exception {
        this.objectThread.start();
        this.updateThread.start();
        this.partThread.start();
    }

    @Override
    protected void disable() throws Exception {
        this.objectThread.getResponseChannel().close();
        this.updateThread.getUpdateChannel().close();
        this.partThread.getResponseChannel().close();
    }

}
