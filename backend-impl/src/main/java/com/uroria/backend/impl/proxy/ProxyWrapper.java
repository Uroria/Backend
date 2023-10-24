package com.uroria.backend.impl.proxy;

import com.uroria.backend.Backend;
import com.uroria.backend.app.ApplicationStatus;
import com.uroria.backend.cache.Wrapper;
import com.uroria.backend.cache.WrapperManager;
import com.uroria.backend.proxy.Proxy;
import com.uroria.backend.server.Server;
import com.uroria.backend.user.User;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import lombok.NonNull;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public final class ProxyWrapper extends Wrapper implements Proxy {
    private final long identifier;
    private boolean deleted;

    public ProxyWrapper(WrapperManager<? extends Wrapper> wrapperManager, long identifier) {
        super(wrapperManager);
        this.identifier = identifier;
    }

    @Override
    public long getId() {
        return this.identifier;
    }

    @Override
    public String getName() {
        return this.object.getStringOrElse("name", getIdentifier());
    }

    @Override
    public Collection<User> getOnlineUsers() {
        return this.object.getSet("onlineUsers", String.class).stream()
                .map(uuidString -> {
                    try {
                        return Backend.getUser(UUID.fromString(uuidString)).get();
                    } catch (Exception exception) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public Collection<Server> getServers() {
        return getRawServers().stream()
                .map(identifier -> {
                    try {
                        return Backend.getServer(identifier).get();
                    } catch (Exception exception) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private Collection<Long> getRawServers() {
        return this.object.getSet("servers", Long.class);
    }

    @Override
    public void registerServer(Server server) {
        Collection<Long> servers = getRawServers();
        servers.add(server.getId());
        this.object.set("servers", new ObjectArraySet<>(servers));
        if (server.getProxies().stream().noneMatch(proxy -> proxy.getId() == this.identifier)) {
            server.addProxy(this);
        }
    }

    @Override
    public void unregisterServer(Server server) {
        Collection<Long> servers = getRawServers();
        servers.remove(server.getId());
        this.object.set("servers", new ObjectArraySet<>(servers));
        if (server.getProxies().stream().anyMatch(proxy -> proxy.getId() == this.identifier)) {
            server.removeProxy(this);
        }
    }

    @Override
    public ApplicationStatus getStatus() {
        return ApplicationStatus.getById(this.object.getIntOrElse("status", ApplicationStatus.EMPTY.getID()));
    }

    @Override
    public void setStatus(@NonNull ApplicationStatus status) {
        this.object.set("status", status.getID());
    }

    @Override
    public int getTemplateId() {
        return this.object.getIntOrElse("templateId", 0);
    }

    @Override
    public int getOnlineUserCount() {
        return this.object.getIntOrElse("playerCount", 0);
    }

    @Override
    public int getMaxUserCount() {
        return this.object.getIntOrElse("maxPlayerCount", 0);
    }

    @Override
    public void delete() {
        if (isDeleted()) return;
        this.deleted = true;
        this.object.set("deleted", true);
    }

    @Override
    public boolean isDeleted() {
        if (this.deleted) return true;
        boolean deleted = this.object.getBooleanOrElse("deleted", false);
        this.deleted = deleted;
        return deleted;
    }

    @Override
    public String getIdentifier() {
        return String.valueOf(identifier);
    }
}
