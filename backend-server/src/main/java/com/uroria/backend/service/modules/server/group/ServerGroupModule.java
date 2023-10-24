package com.uroria.backend.service.modules.server.group;

import com.google.gson.JsonObject;
import com.uroria.backend.cache.communication.DeleteBroadcast;
import com.uroria.backend.cache.communication.PartRequest;
import com.uroria.backend.cache.communication.PartResponse;
import com.uroria.backend.cache.communication.UpdateBroadcast;
import com.uroria.backend.service.BackendServer;
import com.uroria.backend.service.modules.SavingModule;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSets;

import java.util.Collection;
import java.util.Optional;

public final class ServerGroupModule extends SavingModule {

    public ServerGroupModule(BackendServer server) {
        super(server, "server_group", "ServerGroupModule", "servergroup", "servergroups");
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

    @Override
    protected Optional<PartResponse> request(PartRequest request) {
        return Optional.empty();
    }

    @Override
    protected void update(UpdateBroadcast broadcast) {

    }

    @Override
    protected void delete(DeleteBroadcast broadcast) {

    }
}
