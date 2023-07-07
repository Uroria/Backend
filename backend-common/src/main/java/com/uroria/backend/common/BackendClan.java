package com.uroria.backend.common;

import com.uroria.backend.common.helpers.PropertyHolder;
import com.uroria.backend.common.utils.ObjectUtils;
import lombok.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public final class BackendClan extends PropertyHolder<BackendClan> implements Serializable {
    @Serial private static final long serialVersionUID = 1;
    private final List<UUID> moderators;
    private final List<UUID> members;
    private final long foundingDate;
    private final String name;
    private String tag;
    private UUID operator;

    public BackendClan(@NonNull String name, String tag, UUID operator, long foundingDate) {
        this.name = name;
        this.tag = tag;
        this.operator = operator;
        this.moderators = new CopyOnWriteArrayList<>();
        this.members = new CopyOnWriteArrayList<>();
        this.foundingDate = foundingDate;
        this.members.add(operator);
    }

    public void addModerator(@NonNull UUID uuid) {
        this.members.add(uuid);
        this.moderators.remove(uuid);
    }

    public void removeModerator(@NonNull UUID uuid) {
        this.moderators.remove(uuid);
    }

    public void setOperator(@NonNull UUID operator) {
        this.members.add(operator);
        this.operator = operator;
    }

    public long getFoundingDate() {
        return foundingDate;
    }

    public UUID getOperator() {
        return operator;
    }

    public void setTag(@NonNull String tag) {
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        return tag;
    }

    public void addMember(@NonNull UUID uuid) {
        this.members.add(uuid);
    }

    public boolean isDeleted() {
        return this.operator != null;
    }

    public void setDeleted() {
        this.operator = null;
        this.moderators.clear();
        this.members.clear();
    }

    public void removeMember(@NonNull UUID uuid) {
        this.moderators.remove(uuid);
        this.members.remove(uuid);
        if (this.operator.equals(uuid)) {
            if (moderators.isEmpty()) {
                this.operator = this.members.stream().findAny().orElse(null);
                return;
            }
            this.operator = this.moderators.stream().findAny().get();
        }
    }

    public Collection<UUID> getModerators() {
        return moderators;
    }

    public Collection<UUID> getMembers() {
        return new ArrayList<>(this.members);
    }

    @Override
    public synchronized void modify(BackendClan clan) {
        this.tag = clan.tag;
        this.operator = clan.operator;
        ObjectUtils.overrideCollection(moderators, clan.moderators);
        ObjectUtils.overrideCollection(members, clan.members);
        ObjectUtils.overrideMap(properties, clan.properties);
    }
}
