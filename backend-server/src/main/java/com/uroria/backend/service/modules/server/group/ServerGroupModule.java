package com.uroria.backend.service.modules.server.group;

import com.google.gson.JsonObject;
import com.uroria.backend.service.BackendServer;
import com.uroria.backend.service.modules.SavingModule;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSets;

import java.util.Collection;

public final class ServerGroupModule extends SavingModule {
    private final ServerGroupObjectThread objectThread;
    private final ServerGroupPartThread partThread;
    private final ServerGroupUpdateThread updateThread;

    public ServerGroupModule(BackendServer server) {
        super(server, "ServerModule", "servergroups");
        this.objectThread = new ServerGroupObjectThread(this);
        this.updateThread = new ServerGroupUpdateThread(this);
        this.partThread = new ServerGroupPartThread(this);
    }

    @Override
    protected void enable() {
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

    public Collection<String> getAll() {
        Result<Collection<JsonObject>> result = this.db.getAll();
        if (!result.isPresent()) return ObjectSets.emptySet();
        Collection<JsonObject> objects = result.get();
        if (objects == null) return ObjectSets.emptySet();
        Collection<String> names = new ObjectArraySet<>();
        for (JsonObject object : objects) {
            try {
                String name = object.get("name").getAsString();
                names.add(name);
            } catch (Exception ignored) {}
        }
        return names;
    }
}
