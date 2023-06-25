package com.uroria.backend.common;

import com.uroria.backend.common.helpers.PropertyHolder;
import com.uroria.backend.common.utils.ObjectUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

public final class BackendDiscordUser extends PropertyHolder<BackendDiscordUser> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1;
    private UUID uuid;
    private String lastDiscordUserName;
    private String discordId;

    public BackendDiscordUser(UUID uuid, String lastDiscordUserName, String discordId) {
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

    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }

    public Optional<String> getDiscordId() {
        return Optional.ofNullable(this.discordId);
    }

    public Optional<UUID> getUUID() {
        return Optional.ofNullable(uuid);
    }

    @Override
    public synchronized void modify(BackendDiscordUser user) {
        this.uuid = user.uuid;
        this.lastDiscordUserName = user.lastDiscordUserName;
        this.discordId = user.lastDiscordUserName;
        ObjectUtils.overrideMap(properties, user.properties);
    }
}
