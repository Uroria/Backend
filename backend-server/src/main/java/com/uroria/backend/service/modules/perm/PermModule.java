package com.uroria.backend.service.modules.perm;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.uroria.backend.cache.communication.DeleteBroadcast;
import com.uroria.backend.cache.communication.PartRequest;
import com.uroria.backend.cache.communication.PartResponse;
import com.uroria.backend.cache.communication.UpdateBroadcast;
import com.uroria.backend.cache.communication.permgroup.GetGroupRequest;
import com.uroria.backend.cache.communication.permgroup.GetGroupResponse;
import com.uroria.backend.communication.response.RequestListener;
import com.uroria.backend.service.BackendServer;
import com.uroria.backend.service.modules.SavingModule;

import java.time.Duration;
import java.util.Optional;

public final class PermModule extends SavingModule {

    public PermModule(BackendServer server) {
        super(server, "perm_group", "PermModule", "perm_group", "perm_groups");
        responsePoint.registerResponser(GetGroupRequest.class, GetGroupResponse.class, "CheckName", new RequestListener<>() {
            @Override
            protected Optional<GetGroupResponse> onRequest(GetGroupRequest request) {
                String name = request.getName();
                JsonElement element = cache.get(prefix + ":" + name).get();
                if (element == null) {
                    element = db.get("name", name, "name").get();
                    if (element == null) return Optional.of(new GetGroupResponse(false));
                }
                return Optional.of(new GetGroupResponse(true));
            }
        });
    }

    @Override
    protected Optional<PartResponse> request(PartRequest request) {
        String name = request.getIdentifier();
        this.cache.set(prefix + ":" + name, new JsonPrimitive(name), Duration.ofDays(10));
        String key = request.getKey();
        JsonElement part = getPart("name", name, key);
        if (part.isJsonNull()) return Optional.empty();
        return Optional.of(new PartResponse(name, key, part));
    }

    @Override
    protected void update(UpdateBroadcast broadcast) {
        String key = broadcast.getKey();
        String name = broadcast.getIdentifier();
        this.cache.set(prefix + ":" + name, new JsonPrimitive(name), Duration.ofDays(10));
        checkPart("name", name, key, broadcast.getElement());
    }

    @Override
    protected void delete(DeleteBroadcast broadcast) {
        String name = broadcast.getIdentifier();
        this.cache.delete(prefix + ":" + name);
        checkPart("name", name, "deleted", new JsonPrimitive(true));
    }
}
