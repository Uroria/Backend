package com.uroria.backend.common;

import com.uroria.backend.common.helpers.PropertyHolder;
import com.uroria.backend.common.helpers.ServerStatus;
import com.uroria.backend.common.helpers.ServerType;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class BackendServer extends PropertyHolder implements Serializable {
    @Serial private static final long serialVersionUID = 1;
    private final String name;
    private int serverId;
    private int hostId;
    private final int ram;
    private final byte serverType;
    private final byte serverVersion;
    private final byte gameModeId;
    private final byte mapId;
    private final byte mapVariation;
    private final int maxPlayerCount;
    private final Map<String, Object> properties;
    private int status;
    public BackendServer(String name, int serverId, int hostId, int ram, ServerType serverType, int serverVersion, int gameModeId, int mapId, int mapVariation, int maxPlayerCount) {
        this.name = name;
        this.serverId = serverId;
        this.hostId = hostId;
        this.ram = ram;
        this.serverType = (byte) serverType.getId();
        this.serverVersion = (byte) serverVersion;
        this.gameModeId = (byte) gameModeId;
        this.mapId = (byte) mapId;
        this.mapVariation = (byte) mapVariation;
        this.maxPlayerCount = maxPlayerCount;
        this.properties = new HashMap<>();
        this.status = ServerStatus.EMPTY.getId();
    }

    public BackendServer(String name, int ram, ServerType serverType, int serverVersion, int gameModeId, int mapId, int mapVariation, int maxPlayerCount) {
        this(name, 0, 0, ram, serverType, serverVersion, gameModeId, mapId, mapVariation, maxPlayerCount);
    }

    public BackendServer createNewWithSameConfiguration() {
        return new BackendServer(this.name, this.ram, getServerType(), getServerVersion(), this.gameModeId, this.mapId, this.mapVariation, this.maxPlayerCount);
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public void setHostId(int hostId) {
        this.hostId = hostId;
    }

    public ServerStatus getStatus() {
        return ServerStatus.getById(status);
    }

    public void setStatus(ServerStatus status) {
        this.status = status.getId();
    }

    public void setProperty(String key, Serializable object) {
        this.properties.put(key, object);
    }

    public void unsetProperty(String key) {
        this.properties.remove(key);
    }

    public Optional<Object> getProperty(String key) {
        return Optional.ofNullable(this.properties.get(key));
    }

    public int getGameModeId() {
        return gameModeId;
    }

    public int getMapId() {
        return mapId;
    }

    public int getMapVariation() {
        return mapVariation;
    }

    public ServerType getServerType() {
        return ServerType.getById(this.serverType);
    }

    public int getServerVersion() {
        return this.serverVersion;
    }

    public String getDisplayName() {
        return name + "-" + serverId;
    }

    public String getName() {
        return name;
    }

    public int getServerId() {
        return serverId;
    }

    public int getHostId() {
        return hostId;
    }

    public int getRam() {
        return ram;
    }

    public int getMaxPlayerCount() {
        return maxPlayerCount;
    }

    public Map<String, Object> getProperties() {
        return new HashMap<>(this.properties);
    }
}
