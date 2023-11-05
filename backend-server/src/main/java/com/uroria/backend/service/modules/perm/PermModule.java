package com.uroria.backend.service.modules.perm;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.uroria.backend.cache.communication.DeleteBroadcast;
import com.uroria.backend.cache.communication.PartRequest;
import com.uroria.backend.cache.communication.PartResponse;
import com.uroria.backend.cache.communication.UpdateBroadcast;
import com.uroria.backend.cache.communication.permgroup.GetAllGroupRequest;
import com.uroria.backend.cache.communication.permgroup.GetAllGroupResponse;
import com.uroria.backend.cache.communication.permgroup.GetGroupRequest;
import com.uroria.backend.cache.communication.permgroup.GetGroupResponse;
import com.uroria.backend.communication.response.RequestListener;
import com.uroria.backend.service.BackendServer;
import com.uroria.backend.service.modules.SavingModule;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSets;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;

public final class PermModule extends SavingModule {

    public PermModule(BackendServer server) {
        super(server, "perm_group", "PermModule", "perm_group", "perm_groups");
    }

    @Override
    public void enable() {
        responsePoint.registerResponser(GetGroupRequest.class, GetGroupResponse.class, "CheckName", new RequestListener<>() {
            @Override
            protected Optional<GetGroupResponse> onRequest(GetGroupRequest request) {
                String name = request.getName();
                if (request.isAutoCreate()) return Optional.of(new GetGroupResponse(true));
                JsonElement element = cache.get(prefix + ":" + name).get();
                if (element == null) {
                    element = db.get("name", name, "name").get();
                    if (element == null) return Optional.of(new GetGroupResponse(false));
                }
                return Optional.of(new GetGroupResponse(true));
            }
        });
        responsePoint.registerResponser(GetAllGroupRequest.class, GetAllGroupResponse.class, "GetAll", new RequestListener<>() {
            @Override
            protected Optional<GetAllGroupResponse> onRequest(GetAllGroupRequest request) {
                return Optional.of(new GetAllGroupResponse(getAll()));
            }
        });
    }

    public Collection<String> getAll() {
        Result<Collection<JsonObject>> result = this.db.getAll();
        if (!result.isPresent()) {
            getLogger().info("Database seems empty");
            return ObjectSets.emptySet();
        }
        Collection<JsonObject> objects = result.get();
        if (objects == null) return ObjectSets.emptySet();
        Collection<String> names = new ObjectArraySet<>();
        for (JsonObject object : objects) {
            try {
                String name = object.get("name").getAsString();
                names.add(name);
            } catch (Exception exception) {
                getLogger().error("Cannot get name of " + object.toString(), exception);
            }
        }
        return names;
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
        JsonObject object = this.db.get("name", name).get();
        if (object != null) {
            for (String key : object.keySet()) {
                this.cache.delete(prefix + ":" + name + ":" + key);
            }
        }
        checkPart("name", name, "deleted", new JsonPrimitive(true));
    }
}
