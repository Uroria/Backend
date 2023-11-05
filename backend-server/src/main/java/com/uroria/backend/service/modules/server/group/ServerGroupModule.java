package com.uroria.backend.service.modules.server.group;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.uroria.backend.cache.communication.DeleteBroadcast;
import com.uroria.backend.cache.communication.PartRequest;
import com.uroria.backend.cache.communication.PartResponse;
import com.uroria.backend.cache.communication.UpdateBroadcast;
import com.uroria.backend.cache.communication.server.group.GetAllServersGroupsRequest;
import com.uroria.backend.cache.communication.server.group.GetAllServersGroupsResponse;
import com.uroria.backend.cache.communication.server.group.GetServerGroupRequest;
import com.uroria.backend.cache.communication.server.group.GetServerGroupResponse;
import com.uroria.backend.communication.response.RequestListener;
import com.uroria.backend.service.BackendServer;
import com.uroria.backend.service.modules.SavingModule;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSets;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;

public final class ServerGroupModule extends SavingModule {

    public ServerGroupModule(BackendServer server) {
        super(server, "server_group", "ServerGroupModule", "servergroup", "servergroups");
    }

    @Override
    public void enable() {
        responsePoint.registerResponser(GetAllServersGroupsRequest.class, GetAllServersGroupsResponse.class, "GetAll", new RequestListener<>() {
            @Override
            protected Optional<GetAllServersGroupsResponse> onRequest(GetAllServersGroupsRequest request) {
                return Optional.of(new GetAllServersGroupsResponse(getAll()));
            }
        });
        responsePoint.registerResponser(GetServerGroupRequest.class, GetServerGroupResponse.class, "CheckName", new RequestListener<>() {
            @Override
            protected Optional<GetServerGroupResponse> onRequest(GetServerGroupRequest request) {
                String name = request.getName();
                if (request.isAutoCreate()) return Optional.of(new GetServerGroupResponse(true));
                JsonElement element = cache.get(prefix + ":" + name).get();
                if (element == null) {
                    element = db.get("name", name, "name").get();
                    if (element == null) return Optional.of(new GetServerGroupResponse(false));
                }
                return Optional.of(new GetServerGroupResponse(true));
            }
        });
    }

    public Collection<String> getAll() {
        Result<Collection<JsonObject>> result = this.db.getAll();
        if (!result.isPresent()) {
            getLogger().info("Local cache seems empty");
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
        this.cache.set(prefix + ":" + name, new JsonPrimitive(name), Duration.ofDays(14));
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
