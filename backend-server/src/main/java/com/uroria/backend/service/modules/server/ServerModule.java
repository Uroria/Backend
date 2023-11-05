package com.uroria.backend.service.modules.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mongodb.lang.Nullable;
import com.uroria.backend.cache.communication.DeleteBroadcast;
import com.uroria.backend.cache.communication.PartRequest;
import com.uroria.backend.cache.communication.PartResponse;
import com.uroria.backend.cache.communication.UpdateBroadcast;
import com.uroria.backend.cache.communication.server.GetAllServersRequest;
import com.uroria.backend.cache.communication.server.GetAllServersResponse;
import com.uroria.backend.cache.communication.server.GetServerRequest;
import com.uroria.backend.cache.communication.server.GetServerResponse;
import com.uroria.backend.cache.communication.server.ServerPing;
import com.uroria.backend.communication.broadcast.BroadcastListener;
import com.uroria.backend.communication.broadcast.Broadcaster;
import com.uroria.backend.communication.response.RequestListener;
import com.uroria.backend.service.BackendServer;
import com.uroria.backend.service.modules.LocalCachingModule;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;

public final class ServerModule extends LocalCachingModule {
    private final Broadcaster<ServerPing> pingBroadcaster;
    private final ObjectSet<ServerApp> apps;
    public ServerModule(BackendServer server) {
        super(server, "server", "server", "server", "ServerModule", "server");
        this.apps = new ObjectArraySet<>();
        this.pingBroadcaster = broadcastPoint.registerBroadcaster(ServerPing.class, "Ping");
    }

    private void tick() {
        synchronized (apps) {
            for (ServerApp app : this.apps) {
                long id = app.getId();
                long lastPing = app.getLastPing();
                if ((System.currentTimeMillis() - lastPing) < 10000) continue;
                logger.warn("Server " + id + " timed out after 10 seconds");
                delete(new DeleteBroadcast(String.valueOf(id)));
                this.apps.remove(app);
            }
        }
    }

    @AllArgsConstructor
    private static final class TickThread extends Thread {
        private final ServerModule module;

        @Override
        public void run() {
            while (isAlive()) {
                module.tick();
            }
        }
    }

    @Override
    public void enable() {
        new TickThread(this).start();
        responsePoint.registerResponser(GetServerRequest.class, GetServerResponse.class, "CheckId", new RequestListener<>() {
            @Override
            protected Optional<GetServerResponse> onRequest(GetServerRequest request) {
                if (request.isAutoCreate()) {
                    return Optional.of(new GetServerResponse(true, null));
                }
                for (long id : getAll()) {
                    if (id == request.getId()) {
                        return Optional.of(new GetServerResponse(true, getPart("id", String.valueOf(id), "name").getAsString()));
                    }
                }
                return Optional.of(new GetServerResponse(false,  null));
            }
        });
        responsePoint.registerResponser(GetAllServersRequest.class, GetAllServersResponse.class, "GetAll", new RequestListener<>() {
            @Override
            protected Optional<GetAllServersResponse> onRequest(GetAllServersRequest request) {
                return Optional.of(new GetAllServersResponse(getAll()));
            }
        });
        pingBroadcaster.registerListener(new BroadcastListener<>() {
            @Override
            protected void onBroadcast(ServerPing broadcast) {
                long id = broadcast.getIdentifier();
                if (broadcast.isDisabled()) {
                    apps.removeIf(app -> app.getId() == id);
                    return;
                }
                ServerApp app = getOrCreateApp(id);
                app.setLastPing(broadcast.getCurrentMs());
            }
        });
    }

    private @NotNull ServerApp getOrCreateApp(long id) {
        ServerApp app = getApp(id);
        if (app == null) {
            app = new ServerApp(id);
            this.apps.add(app);
        }
        return app;
    }

    private @Nullable ServerApp getApp(long id) {
        return this.apps.stream().filter(app -> app.getId() == id).findAny().orElse(null);
    }

    public Collection<Long> getAll() {
        return this.allIdentifiers.stream().map(obj -> Long.parseLong((String) obj)).toList();
    }

    @Override
    protected Optional<PartResponse> request(PartRequest request) {
        String identifier = request.getIdentifier();
        String key = request.getKey();
        JsonElement part = getPart("id", identifier, key);
        if (part.isJsonNull()) return Optional.empty();
        return Optional.of(new PartResponse(identifier, key, part));
    }

    @Override
    protected void update(UpdateBroadcast broadcast) {
        String identifier = broadcast.getIdentifier();
        this.cache.set(prefix + ":" + identifier, new JsonPrimitive(identifier), Duration.ofHours(1));
        checkPart("id", identifier, broadcast.getKey(), broadcast.getElement());
    }

    @Override
    protected void delete(DeleteBroadcast broadcast) {
        String identifier = broadcast.getIdentifier();
        this.cache.delete(prefix + ":" + identifier);
        checkPart("id", identifier, "deleted", new JsonPrimitive(true));
    }
}
