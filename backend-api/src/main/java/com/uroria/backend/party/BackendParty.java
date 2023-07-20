package com.uroria.backend.party;

import com.uroria.backend.BackendObject;
import com.uroria.backend.utils.ObjectUtils;
import lombok.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class BackendParty extends BackendObject<BackendParty> implements Serializable {
    @Serial private static final long serialVersionUID = 1;
    private final UUID operator;
    private final Set<UUID> members;
    private int currentServer;
    public BackendParty(@NonNull UUID operator, int currentServer) {
        this.operator = operator;
        this.members = ObjectUtils.newSet();
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

    public Set<UUID> getMembers() {
        return new HashSet<>(this.members);
    }

    @Override
    public synchronized void modify(BackendParty party) {
        this.currentServer = party.currentServer;
        ObjectUtils.overrideCollection(members, party.members);
    }
}
