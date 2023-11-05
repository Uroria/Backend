package com.uroria.backend.service.modules.proxy;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mongodb.lang.Nullable;
import com.uroria.backend.cache.communication.DeleteBroadcast;
import com.uroria.backend.cache.communication.PartRequest;
import com.uroria.backend.cache.communication.PartResponse;
import com.uroria.backend.cache.communication.UpdateBroadcast;
import com.uroria.backend.cache.communication.proxy.*;
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

public final class ProxyModule extends LocalCachingModule {
    private final Broadcaster<ProxyPing> pingBroadcaster;
    private final ObjectSet<ProxyApp> apps;
    public ProxyModule(BackendServer server) {
        super(server, "proxy", "proxy", "proxy", "ProxyModule", "proxy");
        this.apps = new ObjectArraySet<>();
        this.pingBroadcaster = broadcastPoint.registerBroadcaster(ProxyPing.class, "Ping");
    }

    private synchronized void tick() {
        for (ProxyApp app : this.apps) {
            long id = app.getId();
            long lastPing = app.getLastPing();
            if ((System.currentTimeMillis() - lastPing) < 10000) continue;
            logger.warn("Proxy " + id + " timed out after 10 seconds");
            delete(new DeleteBroadcast(String.valueOf(id)));
        }
    }

    @AllArgsConstructor
    private static final class TickThread extends Thread {
        private final ProxyModule module;

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
        responsePoint.registerResponser(GetProxyRequest.class, GetProxyResponse.class, "CheckId", new RequestListener<>() {
            @Override
            protected Optional<GetProxyResponse> onRequest(GetProxyRequest request) {
                for (long id : getAll()) {
                    if (id == request.getId()) {
                        if (request.isAutoCreate()) return Optional.of(new GetProxyResponse(false));
                        return Optional.of(new GetProxyResponse(true));
                    }
                }
                if (request.isAutoCreate()) {
                    return Optional.of(new GetProxyResponse(true));
                }
                return Optional.of(new GetProxyResponse(false));
            }
        });
        responsePoint.registerResponser(GetAllProxiesRequest.class, GetAllProxiesResponse.class, "GetAll", new RequestListener<>() {
            @Override
            protected Optional<GetAllProxiesResponse> onRequest(GetAllProxiesRequest request) {
                String name = request.getName();
                if (name == null) {
                    return Optional.of(new GetAllProxiesResponse(getAll()));
                }
                ObjectSet<Long> ids = new ObjectArraySet<>();
                getAll().forEach(id -> {
                    PartResponse wrapperName = request(new PartRequest(String.valueOf(id), "name")).orElse(null);
                    if (wrapperName == null) return;
                    if (!wrapperName.getValue().getAsString().equals(name)) return;
                    ids.add(id);
                });
                return Optional.of(new GetAllProxiesResponse(ids));
            }
        });
        pingBroadcaster.registerListener(new BroadcastListener<>() {
            @Override
            protected void onBroadcast(ProxyPing broadcast) {
                long id = broadcast.getIdentifier();
                if (broadcast.isDisabled()) {
                    apps.removeIf(app -> app.getId() == id);
                    return;
                }
                ProxyApp app = getOrCreateApp(id);
                app.setLastPing(id);
            }
        });
    }

    private @NotNull ProxyApp getOrCreateApp(long id) {
        ProxyApp app = getApp(id);
        if (app == null) {
            app = new ProxyApp(id);
            this.apps.add(app);
        }
        return app;
    }

    private @Nullable ProxyApp getApp(long id) {
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
