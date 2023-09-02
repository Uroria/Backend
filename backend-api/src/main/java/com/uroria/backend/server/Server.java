package com.uroria.backend.server;

import com.uroria.backend.Backend;
import com.uroria.backend.BackendObject;
import com.uroria.base.utils.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class Server extends BackendObject<Server> implements Serializable {
    @Serial private static final long serialVersionUID = 1;

    private final long identifier;
    @Getter
    private final String name;
    private final ObjectList<String> groups;
    private final int templateId;
    private final int type;
    private final int maxPlayerCount;
    private final ObjectList<UUID> onlinePlayers;
    private int status;
    private int id;
    private InetSocketAddress address;

    public Server(@NonNull String name, int templateId, @NonNull ServerType type, int maxPlayerCount) {
        synchronized (this) {
            long temp = System.nanoTime() - (name.hashCode() - hashCode() + System.currentTimeMillis());
            if (temp < 0) this.identifier = temp * -1;
            else this.identifier = temp;
        }
        this.id = -1;
        this.name = name;
        this.groups = new ObjectArrayList<>();
        this.templateId = templateId;
        this.type = type.getID();
        this.maxPlayerCount = maxPlayerCount;
        this.onlinePlayers = new ObjectArrayList<>();
        this.status = ServerStatus.EMPTY.getID();
    }

    public Server(@NonNull String name, int templateId, @NonNull ServerType type) {
        this(name, templateId, type, 100);
    }

    public void setAddress(@NonNull InetSocketAddress address) {
        this.address = address;
    }

    public @Nullable InetSocketAddress getAddress() {
        return this.address;
    }

    void setID(int id) {
        this.id = id;
    }

    public ServerStatus getStatus() {
        return ServerStatus.getById(this.status);
    }

    public ServerType getType() {
        return ServerType.getById(this.type);
    }

    public void addPlayer(@NonNull UUID uuid) {
        if (this.onlinePlayers.stream().anyMatch(uuid::equals)) return;
        this.onlinePlayers.add(uuid);
    }

    public void removePlayer(UUID uuid) {
        if (uuid == null) return;
        this.onlinePlayers.remove(uuid);
    }

    public boolean containsPlayer(UUID uuid) {
        if (uuid == null) return false;
        return this.onlinePlayers.contains(uuid);
    }

    public int getTemplateID() {
        return this.templateId;
    }

    public List<UUID> getOnlinePlayers() {
        return Collections.unmodifiableList(this.onlinePlayers);
    }

    public void setStatus(@NonNull ServerStatus status) {
        this.status = status.getID();
    }

    public void setProperty(@NonNull String key, @NonNull Serializable value) {
        this.properties.put(key, value);
    }

    public Optional<Serializable> getPropertyObject(@Nullable String key) {
        if (key == null) return Optional.empty();
        Object o = this.properties.get(key);
        if (o == null) return Optional.empty();
        return Optional.of((Serializable) o);
    }

    public boolean isFull() {
        return getPlayerCount() > getMaxPlayerCount();
    }

    public int getPlayerCount() {
        return this.onlinePlayers.size();
    }

    public int getMaxPlayerCount() {
        return this.maxPlayerCount;
    }

    public Server copy() {
        Server server = new Server(this.name, this.templateId, getType(), this.maxPlayerCount);
        server.setProperties(this.properties);
        return server;
    }

    public String getDisplayName() {
        return this.name + "-" + this.templateId + "=[" + this.identifier + "~" + this.id + "]";
    }

    public int getID() {
        return this.id;
    }

    public long getIdentifier() {
        return this.identifier;
    }

    public List<String> getGroups() {
        return Collections.unmodifiableList(this.groups);
    }

    public void addGroup(@NonNull String group) {
        if (this.groups.contains(group)) return;
        this.groups.add(group);
    }

    public void removeGroup(String group) {
        if (group == null) return;
        this.groups.remove(group);
    }

    @Override
    public void modify(Server server) {
        this.id = server.id;
        this.status = server.status;
        this.address = server.address;
        CollectionUtils.overrideMap(this.properties, server.properties);
        CollectionUtils.overrideCollection(this.onlinePlayers, server.onlinePlayers);
    }

    @Override
    public void update() {
        Backend.getAPI().getServerManager().updateServer(this);
    }

    public void create() {
        Backend.getAPI().getServerManager().startServer(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Server server) {
            return server.identifier == this.identifier;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Server{name="+this.name+", id="+this.id+", identifier="+this.identifier+", status="+getStatus()+"}";
    }
}
