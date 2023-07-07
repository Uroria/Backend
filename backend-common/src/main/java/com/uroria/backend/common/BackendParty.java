package com.uroria.backend.common;

import com.uroria.backend.common.utils.ObjectUtils;
import lombok.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public final class BackendParty extends BackendObject<BackendParty> implements Serializable {
    @Serial private static final long serialVersionUID = 1;
    private final UUID operator;
    private final List<UUID> members;
    private int currentServer;
    public BackendParty(@NonNull UUID operator, int currentServer) {
        this.operator = operator;
        this.members = new ArrayList<>();
        this.currentServer = currentServer;
        this.members.add(operator);
    }

    public void addMember(@NonNull UUID uuid) {
        this.members.add(uuid);
    }

    public void removeMember(@NonNull UUID uuid) {
        if (uuid.equals(this.operator)) {
            this.members.clear();
            return;
        }
        this.members.remove(uuid);
    }

    public void setCurrentServer(int currentServer) {
        this.currentServer = currentServer;
    }

    public int getCurrentServer() {
        return currentServer;
    }

    public void delete() {
        this.members.clear();
    }

    public boolean isDeleted() {
        return this.members.isEmpty();
    }

    public UUID getOperator() {
        return operator;
    }

    public Collection<UUID> getMembers() {
        return new ArrayList<>(this.members);
    }

    @Override
    public synchronized void modify(BackendParty party) {
        this.currentServer = party.currentServer;
        ObjectUtils.overrideCollection(members, party.members);
    }
}
