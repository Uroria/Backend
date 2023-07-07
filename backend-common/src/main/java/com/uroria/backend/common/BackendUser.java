package com.uroria.backend.common;

import com.uroria.backend.common.helpers.PropertyHolder;
import com.uroria.backend.common.utils.ObjectUtils;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class BackendUser extends PropertyHolder<BackendUser> implements Serializable {
    @Serial private static final long serialVersionUID = 1;

    private final long discordUserId;
    private final List<String> oldNames;
    private final List<UUID> oldMinecraftUserIds;
    private String name;
    private UUID minecraftUserId;

    public BackendUser(long discordUserId) {
        if (discordUserId == 0) throw new IllegalArgumentException("Discord User id cannot be 0");
        this.discordUserId = discordUserId;
        this.oldNames = new ArrayList<>();
        this.oldMinecraftUserIds = new ArrayList<>();
    }

    public void setCurrentName(@Nullable String name) {
        if (name != null && this.name != null) {
            if (name.equals(this.name)) return;
            this.oldNames.add(this.name);
            this.name = name;
            return;
        }
        if (name == null && this.name != null) {
            this.oldNames.add(this.name);
            this.name = null;
            return;
        }
        this.name = name;
    }

    public void setMinecraftUserID(@Nullable UUID uuid) {
        if (uuid != null && this.minecraftUserId != null) {
            if (uuid.equals(this.minecraftUserId)) return;
            this.oldMinecraftUserIds.add(this.minecraftUserId);
            this.minecraftUserId = uuid;
            return;
        }
        if (uuid == null && this.minecraftUserId != null) {
            this.oldMinecraftUserIds.add(this.minecraftUserId);
            this.minecraftUserId = null;
            return;
        }
        this.minecraftUserId = uuid;
    }

    public Optional<UUID> getMinecraftUserID() {
        return Optional.ofNullable(this.minecraftUserId);
    }

    public Optional<String> getCurrentName() {
        return Optional.ofNullable(this.name);
    }

    public List<String> getOldNames() {
        return oldNames;
    }

    public List<UUID> getOldMinecraftUserIDs() {
        return new ArrayList<>(oldMinecraftUserIds);
    }

    public long getDiscordUserID() {
        return this.discordUserId;
    }

    @Override
    public void modify(BackendUser user) {
        ObjectUtils.overrideCollection(this.oldMinecraftUserIds, user.oldMinecraftUserIds);
        ObjectUtils.overrideCollection(this.oldNames, user.oldNames);
        this.name = user.name;
        this.minecraftUserId = user.minecraftUserId;
    }
}
