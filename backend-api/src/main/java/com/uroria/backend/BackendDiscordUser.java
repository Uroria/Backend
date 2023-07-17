package com.uroria.backend;

import com.uroria.backend.helpers.PropertyHolder;
import com.uroria.backend.utils.ObjectUtils;
import lombok.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

public final class BackendDiscordUser extends PropertyHolder<BackendDiscordUser> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1;
    private UUID uuid;
    private String lastDiscordUserName;
    private long discordId;

    public BackendDiscordUser(@NonNull UUID uuid, String lastDiscordUserName, long discordId) {
        this.uuid = uuid;
        this.lastDiscordUserName = lastDiscordUserName;
        this.discordId = discordId;
    }

    public void setLastDiscordUserName(String lastDiscordUserName) {
        this.lastDiscordUserName = lastDiscordUserName;
    }

    public Optional<String> getLastDiscordUserName() {
        return Optional.ofNullable(lastDiscordUserName);
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public void setDiscordId(long discordId) {
        this.discordId = discordId;
    }

    public long getDiscordId() {
        return this.discordId;
    }

    public Optional<UUID> getUUID() {
        return Optional.ofNullable(uuid);
    }

    @Override
    public synchronized void modify(BackendDiscordUser user) {
        this.uuid = user.uuid;
        this.lastDiscordUserName = user.lastDiscordUserName;
        this.discordId = user.discordId;
        ObjectUtils.overrideMap(properties, user.properties);
    }
}