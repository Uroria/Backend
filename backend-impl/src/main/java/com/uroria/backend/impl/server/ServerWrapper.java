package com.uroria.backend.impl.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.uroria.backend.Backend;
import com.uroria.backend.app.ApplicationStatus;
import com.uroria.backend.impl.communication.CommunicationClient;
import com.uroria.backend.impl.communication.CommunicationWrapper;
import com.uroria.backend.impl.wrapper.Wrapper;
import com.uroria.backend.proxy.Proxy;
import com.uroria.backend.server.Server;
import com.uroria.backend.user.User;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class ServerWrapper extends Wrapper implements Server {
    private final CommunicationWrapper object;
    private final long identifier;
    private final ServerGroupWrapper group;

    private boolean deleted;
    private String type;
    private int templateId;
    private InetSocketAddress address;

    public ServerWrapper(@NonNull CommunicationClient client, long identifier) {
        this.object = new CommunicationWrapper(String.valueOf(identifier), client);
        this.identifier = identifier;
        this.templateId = -1;
        this.group = getGroup();
    }

    public ServerWrapper(@NonNull CommunicationClient client, long identifier, ServerGroupWrapper group) {
        this.object = new CommunicationWrapper(String.valueOf(identifier), client);
        this.identifier = identifier;
        this.templateId = -1;
        this.group = group;
    }

    @Override
    public CommunicationWrapper getObjectWrapper() {
        return this.object;
    }

    @Override
    public void refresh() {

    }

    @Override
    public JsonObject getObject() {
        return this.object.getObject();
    }

    @Override
    public String getIdentifierKey() {
        return String.valueOf(identifier);
    }

    @Override
    public void delete() {
        if (isDeleted()) return;
        this.deleted = true;
        object.set("deleted", true);
    }

    @Override
    public boolean isDeleted() {
        if (this.deleted) return true;
        boolean val = getBoolean("deleted", false);
        if (val) this.deleted = true;
        return val;
    }

    @Override
    public long getIdentifier() {
        return this.identifier;
    }

    @Override
    public String getStringIdentifier() {
        return String.valueOf(this.identifier);
    }

    @Override
    public void addProxy(@NonNull Proxy proxy) {
        addToLongList("proxies", proxy.getIdentifier());
    }

    @Override
    public void removeProxy(Proxy proxy) {
        removeFromLongList("proxies", proxy.getIdentifier());
    }

    @Override
    public Collection<Proxy> getProxies() {
        return getLongs("proxies").stream()
                .map(identifier -> Backend.getProxy(identifier).get())
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public ServerGroupWrapper getGroup() {
        return (ServerGroupWrapper) Backend.getServerGroup(getType()).get();
    }

    @Override
    public ApplicationStatus getStatus() {
        return ApplicationStatus.getById(getInt("status"));
    }

    @Override
    public void setStatus(@NonNull ApplicationStatus status) {
        this.object.set("status", status.getID());
    }

    @Override
    public Result<InetSocketAddress> getAddress() {
        if (this.address != null) return Result.some(this.address);
        Result<JsonElement> hostResult = object.get("host");
        Result<JsonElement> portResult = object.get("port");
        JsonElement hostElement = hostResult.get();
        JsonElement portElement = portResult.get();
        if (hostElement == null || portElement == null) return Result.none();
        InetSocketAddress address = new InetSocketAddress(hostElement.getAsString(), portElement.getAsInt());
        this.address = address;
        return Result.some(address);
    }

    public void setAddress(InetSocketAddress address) {
        this.address = address;
        this.object.set("host", address.getHostName());
        this.object.set("port", address.getPort());
    }

    @Override
    public int getTemplateId() {
        if (this.templateId != -1) return this.templateId;
        int templateId = getInt("templateId", -1);
        this.templateId = templateId;
        return templateId;
    }

    @Override
    public String getType() {
        if (this.type != null) return this.type;
        this.type = getString("type", String.valueOf(identifier));
        return this.type;
    }

    @Override
    public Collection<User> getOnlineUsers() {
        return getStrings("onlinePlayers").stream()
                .map(UUID::fromString)
                .map(uuid -> Backend.getUser(uuid).get())
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public int getOnlineUserCount() {
        return getInt("playerCount");
    }

    @Override
    public int getMaxUserCount() {
        return getInt("maxPlayerCount");
    }

    @Override
    public Map<String, Object> getProperties() {
        return Object2ObjectMaps.emptyMap();
    }

    @Override
    public void unsetProperty(@NonNull String key) {
        this.object.set("property." + key, JsonNull.INSTANCE);
    }

    @Override
    public void setProperties(@NonNull Map<String, Object> properties) {
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Integer n) {
                setProperty("property." + key, n);
                return;
            }
            if (value instanceof Long n) {
                setProperty("property." + key, n);
                return;
            }
            if (value instanceof Double n) {
                setProperty("property." + key, n);
                return;
            }
            if (value instanceof Float n) {
                setProperty("property." + key, n);
                return;
            }
            if (value instanceof Boolean b) {
                setProperty("property." + key, b);
                return;
            }
            if (value instanceof String s) {
                setProperty("property." + key, s);
                return;
            }
        }
    }

    @Override
    public void setProperty(@NonNull String key, @NonNull String value) {
        this.object.set("property." + key, value);
    }

    @Override
    public void setProperty(@NonNull String key, int value) {
        this.object.set("property." + key, value);
    }

    @Override
    public void setProperty(@NonNull String key, long value) {
        this.object.set("property." + key, value);
    }

    @Override
    public void setProperty(@NonNull String key, double value) {
        this.object.set("property." + key, value);
    }

    @Override
    public void setProperty(@NonNull String key, float value) {
        this.object.set("property." + key, value);
    }

    @Override
    public void setProperty(@NonNull String key, boolean value) {
        this.object.set("property." + key, value);
    }

    @Override
    public String getPropertyStringOrElse(@NonNull String key, @Nullable String defValue) {
        JsonElement element = this.object.get("property." + key).get();
        if (element == null) return defValue;
        return element.getAsString();
    }

    @Override
    public int getPropertyIntOrElse(@NonNull String key, int defValue) {
        JsonElement element = this.object.get("property." + key).get();
        if (element == null) return defValue;
        return element.getAsInt();
    }

    @Override
    public long getPropertyLongOrElse(@NonNull String key, long defValue) {
        JsonElement element = this.object.get("property." + key).get();
        if (element == null) return defValue;
        return element.getAsLong();
    }

    @Override
    public double getPropertyDoubleOrElse(@NonNull String key, double defValue) {
        JsonElement element = this.object.get("property." + key).get();
        if (element == null) return defValue;
        return element.getAsDouble();
    }

    @Override
    public float getPropertyFloatOrElse(@NonNull String key, float defValue) {
        JsonElement element = this.object.get("property." + key).get();
        if (element == null) return defValue;
        return element.getAsFloat();
    }

    @Override
    public boolean getPropertyBooleanOrElse(@NonNull String key, boolean defValue) {
        JsonElement element = this.object.get("property." + key).get();
        if (element == null) return defValue;
        return element.getAsBoolean();
    }
}
