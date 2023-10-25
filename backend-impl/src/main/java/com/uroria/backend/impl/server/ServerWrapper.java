package com.uroria.backend.impl.server;

import com.uroria.backend.Backend;
import com.uroria.backend.app.ApplicationStatus;
import com.uroria.backend.cache.Wrapper;
import com.uroria.backend.cache.WrapperManager;
import com.uroria.backend.impl.server.group.ServerGroupWrapper;
import com.uroria.backend.proxy.Proxy;
import com.uroria.backend.server.Server;
import com.uroria.backend.user.User;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class ServerWrapper extends Wrapper implements Server {
    private final long identifier;
    private boolean deleted;
    private InetSocketAddress address;

    public ServerWrapper(WrapperManager<? extends Wrapper> wrapperManager, long identifier) {
        super(wrapperManager);
        this.identifier = identifier;
    }

    @Override
    public void delete() {
        if (isDeleted()) return;
        getGroup().removeServer(this.identifier);
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
    public long getId() {
        return this.identifier;
    }

    @Override
    public void addProxy(@NonNull Proxy proxy) {
        Collection<Long> proxies = getRawProxies();
        proxies.add(proxy.getId());
        this.object.set("proxies", new ObjectArraySet<>(proxies));
        if (proxy.getServers().stream().noneMatch(server -> server.getId() == this.identifier)) {
            proxy.registerServer(this);
        }
    }

    @Override
    public void removeProxy(Proxy proxy) {
        Collection<Long> proxies = getRawProxies();
        proxies.remove(proxy.getId());
        this.object.set("proxies", new ObjectArraySet<>(proxies));
        if (proxy.getServers().stream().anyMatch(server -> server.getId() == this.identifier)) {
            proxy.unregisterServer(this);
        }
    }

    @Override
    public Collection<Proxy> getProxies() {
        return getRawProxies().stream()
                .map(identifier -> {
                    try {
                        return Backend.getProxy(identifier).get();
                    } catch (Exception exception) {
                        return null;
                    }
                })
                .filter(this::nullCheck)
                .toList();
    }

    @Override
    public ServerGroupWrapper getGroup() {
        return (ServerGroupWrapper) Backend.getServerGroup(getName()).get();
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
    public Result<InetSocketAddress> getAddress() {
        try {
            if (this.address != null) return Result.some(this.address);
            String host = this.object.getStringOrElse("host", null);
            int port = this.object.getIntOrElse("port", 0);
            if (host == null || port == 0) return Result.none();
            InetSocketAddress address = new InetSocketAddress(host, port);
            this.address = address;
            return Result.some(address);
        } catch (Exception exception) {
            return Result.none();
        }
    }

    public void setAddress(InetSocketAddress address) {
        this.address = address;
        this.object.set("host", address.getHostName());
        this.object.set("port", address.getPort());
    }

    @Override
    public int getTemplateId() {
        return this.object.getIntOrElse("templateId", 0);
    }

    @Override
    public String getName() {
        return getGroup().getName();
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
                .filter(this::nullCheck)
                .toList();
    }

    private Collection<Long> getRawProxies() {
        return this.object.getSet("proxies", Long.class);
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
    public Map<String, Object> getProperties() {
        return this.object.getMap("properties", Object.class);
    }

    @Override
    public void unsetProperty(@NonNull String key) {
        Map<String, Object> properties = getProperties();
        properties.remove(key);
        this.object.set("properties", properties);
    }

    public void setProperty(String key, Object value) {
        Map<String, Object> properties = getProperties();
        properties.put(key, value);
        this.object.set("properties", properties);
    }

    @Override
    public void setProperties(@NonNull Map<String, Object> properties) {
        this.object.set("properties", new Object2ObjectArrayMap<>(properties));
    }

    @Override
    public void setProperty(@NonNull String key, @NonNull String value) {
        setProperty(key, (Object) value);
    }

    @Override
    public void setProperty(@NonNull String key, int value) {
        setProperty(key, (Object) value);
    }

    @Override
    public void setProperty(@NonNull String key, long value) {
        setProperty(key, (Object) value);
    }

    @Override
    public void setProperty(@NonNull String key, double value) {
        setProperty(key, (Object) value);
    }

    @Override
    public void setProperty(@NonNull String key, float value) {
        setProperty(key, (Object) value);
    }

    @Override
    public void setProperty(@NonNull String key, boolean value) {
        setProperty(key, (Object) value);
    }

    @Override
    public String getPropertyStringOrElse(@NonNull String key, @Nullable String defValue) {
        Object obj = getProperties().get(key);
        if (obj == null) return defValue;
        return (String) obj;
    }

    @Override
    public int getPropertyIntOrElse(@NonNull String key, int defValue) {
        Object obj = getProperties().get(key);
        if (obj == null) return defValue;
        return (int) obj;
    }

    @Override
    public long getPropertyLongOrElse(@NonNull String key, long defValue) {
        Object obj = getProperties().get(key);
        if (obj == null) return defValue;
        return (long) obj;
    }

    @Override
    public double getPropertyDoubleOrElse(@NonNull String key, double defValue) {
        Object obj = getProperties().get(key);
        if (obj == null) return defValue;
        return (double) obj;
    }

    @Override
    public float getPropertyFloatOrElse(@NonNull String key, float defValue) {
        Object obj = getProperties().get(key);
        if (obj == null) return defValue;
        return (float) obj;
    }

    @Override
    public boolean getPropertyBooleanOrElse(@NonNull String key, boolean defValue) {
        Object obj = getProperties().get(key);
        if (obj == null) return defValue;
        return (boolean) obj;
    }

    @Override
    public String getIdentifier() {
        return String.valueOf(this.identifier);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ServerWrapper wrapper) {
            return wrapper.identifier == identifier;
        }
        return false;
    }
}
