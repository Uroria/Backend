package com.uroria.backend.common;

import com.uroria.backend.common.helpers.PropertyHolder;
import com.uroria.backend.common.helpers.ServerStatus;
import com.uroria.backend.common.helpers.ServerType;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public final class BackendServer extends PropertyHolder implements Serializable {
    @Serial private static final long serialVersionUID = 1;

    private final String name;
    private final int templateId;
    private final int type;
    private final int maxPlayerCount;
    private final Collection<UUID> onlinePlayers;
    private int status;
    private int id;

    public BackendServer(String name, int templateId, ServerType type, int maxPlayerCount) {
        this.id = -1;
        this.name = name;
        this.templateId = templateId;
        this.type = type.getId();
        this.status = ServerStatus.EMPTY.getId();
        this.maxPlayerCount = maxPlayerCount;
        this.onlinePlayers = new ArrayList<>();
    }

    public BackendServer(String name, int templateId, ServerType type) {
        this(name, templateId, type, 50);
    }

    void setId(int id) {
        this.id = id;
    }

    public ServerStatus getStatus() {
        return ServerStatus.getById(this.status);
    }

    public ServerType getType() {
        return ServerType.getById(this.type);
    }

    public boolean isServerFull() {
        return getPlayerCount() > this.maxPlayerCount;
    }

    public void addPlayer(UUID uuid) {
        this.onlinePlayers.add(uuid);
    }

    public void removePlayer(UUID uuid) {
        this.onlinePlayers.remove(uuid);
    }

    public boolean containsPlayer(UUID uuid) {
        return this.onlinePlayers.contains(uuid);
    }

    public int getTemplateId() {
        return this.templateId;
    }

    public String getDisplayName() {
        return name + "=" + id + "-" + templateId;
    }

    public String getName() {
        return name;
    }

    public void setStatus(ServerStatus serverStatus) {
        this.status = serverStatus.getId();
    }

    public Optional<Integer> getId() throws IllegalStateException {
        if (id == -1) return Optional.empty();
        return Optional.of(id);
    }

    public int getMaxPlayerCount() {
        return maxPlayerCount;
    }

    public int getPlayerCount() {
        return this.onlinePlayers.size();
    }

    public void setProperty(String key, Serializable value) {
        this.properties.put(key, value);
    }

    public Optional<Serializable> getPropertyObject(String key) {
        Object o = this.properties.get(key);
        if (o == null) return Optional.empty();
        return Optional.of((Serializable) o);
    }

    public Collection<UUID> getOnlinePlayers() {
        return onlinePlayers;
    }

    public BackendServer copy() {
        BackendServer server = new BackendServer(this.name, this.templateId, getType(), this.maxPlayerCount);
        server.setProperties(this.properties);
        return server;
    }

    public static BackendServer createLobby() {
        return new BackendServer("Lobby", 1, ServerType.LOBBY, 100);
    }
}
