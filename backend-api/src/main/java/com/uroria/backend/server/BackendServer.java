package com.uroria.backend.server;

import com.uroria.backend.helpers.PropertyHolder;
import com.uroria.backend.utils.ObjectUtils;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public final class BackendServer extends PropertyHolder<BackendServer> implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger("BackendServer");
    @Serial private static final long serialVersionUID = 1;

    private final long identifier;
    private final String name;
    private final int templateId;
    private final int type;
    private final int maxPlayerCount;
    private final Set<UUID> onlinePlayers;
    private int status;
    private int id;

    public BackendServer(@NonNull String name, int templateId, @NonNull ServerType type, int maxPlayerCount) {
        this.identifier = System.currentTimeMillis() - hashCode() - new Random().nextInt(10000);
        this.id = -1;
        this.name = name;
        this.templateId = templateId;
        this.type = type.getId();
        this.status = ServerStatus.EMPTY.getId();
        this.maxPlayerCount = maxPlayerCount;
        this.onlinePlayers = ObjectUtils.newSet();
    }

    public BackendServer(@NonNull String name, int templateId, @NonNull ServerType type) {
        this(name, templateId, type, 50);
    }

    public long getIdentifier() {
        return this.identifier;
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

    public void addPlayer(@NonNull UUID uuid) {
        this.onlinePlayers.add(uuid);
    }

    public void removePlayer(@NonNull UUID uuid) {
        this.onlinePlayers.remove(uuid);
    }

    public boolean containsPlayer(UUID uuid) {
        if (uuid == null) return false;
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

    public void setStatus(@NonNull ServerStatus serverStatus) {
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

    public void setProperty(@NonNull String key, @NonNull Serializable value) {
        this.properties.put(key, value);
    }

    public Optional<Serializable> getPropertyObject(@Nullable String key) {
        if (key == null) return Optional.empty();
        Object o = this.properties.get(key);
        if (o == null) return Optional.empty();
        return Optional.of((Serializable) o);
    }

    public Set<UUID> getOnlinePlayers() {
        return onlinePlayers;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BackendServer server) {
            if (server.getId().isPresent() && this.getId().isPresent()) {
                return server.id == this.id;
            }
            return server.getIdentifier() == this.identifier;
        }
        return false;
    }

    public BackendServer copy() {
        BackendServer server = new BackendServer(this.name, this.templateId, getType(), this.maxPlayerCount);
        server.setProperties(this.properties);
        return server;
    }

    @Override
    public synchronized void modify(BackendServer server) {
        this.status = server.status;
        this.id = server.id;
        ObjectUtils.overrideMap(properties, server.properties);
        ObjectUtils.overrideCollection(onlinePlayers, server.onlinePlayers);
    }

    public static BackendServer createLobby() {
        return new BackendServer("Lobby", 1, ServerType.LOBBY, 100);
    }
}
