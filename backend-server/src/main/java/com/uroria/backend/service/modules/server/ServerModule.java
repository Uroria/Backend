package com.uroria.backend.service.modules.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.uroria.backend.cache.communication.DeleteBroadcast;
import com.uroria.backend.cache.communication.PartRequest;
import com.uroria.backend.cache.communication.PartResponse;
import com.uroria.backend.cache.communication.UpdateBroadcast;
import com.uroria.backend.cache.communication.server.GetAllServersRequest;
import com.uroria.backend.cache.communication.server.GetAllServersResponse;
import com.uroria.backend.cache.communication.server.GetServerRequest;
import com.uroria.backend.cache.communication.server.GetServerResponse;
import com.uroria.backend.communication.response.RequestListener;
import com.uroria.backend.service.BackendServer;
import com.uroria.backend.service.modules.LocalCachingModule;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;


public final class ServerModule extends LocalCachingModule {
    
    public ServerModule(BackendServer server) {
        super(server, "server", "server", "server", "ServerModule", "server");
        responsePoint.registerResponser(GetServerRequest.class, GetServerResponse.class, "CheckId", new RequestListener<>() {
            @Override
            protected Optional<GetServerResponse> onRequest(GetServerRequest request) {
                for (long id : getAll()) {
                    if (id == request.getId()) return Optional.of(new GetServerResponse(true));
                }
                return Optional.empty();
            }
        });
        responsePoint.registerResponser(GetAllServersRequest.class, GetAllServersResponse.class, "GetAll", new RequestListener<>() {
            @Override
            protected Optional<GetAllServersResponse> onRequest(GetAllServersRequest request) {
                return Optional.of(new GetAllServersResponse(getAll()));
            }
        });
    }

    public Collection<Long> getAll() {
        return this.allIdentifiers.stream().map(obj -> Long.parseLong((String) obj)).toList();
    }

    @Override
    protected Optional<PartResponse> request(PartRequest request) {
        String identifier = request.getIdentifier();
        String key = request.getKey();
        JsonElement part = getPart("identifier", identifier, key);
        if (part.isJsonNull()) return Optional.empty();
        return Optional.of(new PartResponse(identifier, key, part));
    }

    @Override
    protected void update(UpdateBroadcast broadcast) {
        String identifier = broadcast.getIdentifier();
        this.cache.set(prefix + ":" + identifier, new JsonPrimitive(identifier), Duration.ofHours(1));
        checkPart("identifier", identifier, broadcast.getKey(), broadcast.getElement());
    }

    @Override
    protected void delete(DeleteBroadcast broadcast) {
        String identifier = broadcast.getIdentifier();
        this.cache.delete(prefix + ":" + identifier);
        checkPart("identifier", identifier, "deleted", new JsonPrimitive(true));
    }
}
