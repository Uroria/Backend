package com.uroria.backend.common;

import com.uroria.backend.common.helpers.PropertyHolder;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public final class BackendClan extends PropertyHolder implements Serializable {
    @Serial private static final long serialVersionUID = 1;
    private final Collection<UUID> moderators;
    private final Collection<UUID> members;
    private final long foundingDate;
    private final String name;
    private String tag;
    private UUID operator;

    public BackendClan(String name, String tag, UUID operator, long foundingDate) {
        this.name = name;
        this.tag = tag;
        this.operator = operator;
        this.moderators = new ArrayList<>();
        this.members = new ArrayList<>();
        this.foundingDate = foundingDate;
        this.members.add(operator);
    }

    public void addModerator(UUID uuid) {
        if (uuid == null) throw new NullPointerException("UUID cannot be null");
        this.members.add(uuid);
        this.moderators.remove(uuid);
    }

    public void removeModerator(UUID uuid) {
        if (uuid == null) throw new NullPointerException("UUID cannot be null");
        this.moderators.remove(uuid);
    }

    public void setOperator(UUID operator) {
        if (operator == null) throw new NullPointerException("UUID cannot be null");
        this.members.add(operator);
        this.operator = operator;
    }

    public long getFoundingDate() {
        return foundingDate;
    }

    public UUID getOperator() {
        return operator;
    }

    public void setTag(String tag) {
        if (tag == null) throw new NullPointerException("Tag cannot be null");
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        return tag;
    }

    public void addMember(UUID uuid) {
        if (uuid == null) throw new NullPointerException("UUID cannot be null");
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

    public void removeMember(UUID uuid) {
        if (uuid == null) throw new NullPointerException("UUID cannot be null");
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
}
