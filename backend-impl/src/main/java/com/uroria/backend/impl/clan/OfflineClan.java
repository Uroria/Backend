package com.uroria.backend.impl.clan;

import com.uroria.backend.Backend;
import com.uroria.backend.clan.Clan;
import com.uroria.backend.user.User;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.NonNull;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public final class OfflineClan implements Clan {
    private final String name;
    private final long foundingDate;
    private final ObjectSet<UUID> members;
    private final ObjectSet<UUID> moderators;
    private final ObjectSet<UUID> operators;
    private String tag;
    private boolean deleted;

    public OfflineClan(String name, long foundingDate) {
        this.name = name;
        this.foundingDate = foundingDate;
        this.members = new ObjectArraySet<>();
        this.moderators = new ObjectArraySet<>();
        this.operators = new ObjectArraySet<>();
    }


    @Override
    public void delete() {
        if (isDeleted()) return;
        this.deleted = true;
        for (User user : getMembers()) {
            user.leaveClan();
        }
    }

    @Override
    public boolean isDeleted() {
        return this.deleted;
    }

    @Override
    public String getTag() {
        return this.tag;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setTag(@NonNull String tag) {
        this.tag = tag;
    }

    @Override
    public long getFoundingDate() {
        return this.foundingDate;
    }

    @Override
    public boolean hasMember(@NonNull User user) {
        return this.members.contains(user.getUniqueId());
    }

    @SuppressWarnings("SafetyWarnings")
    @Override
    public void addMember(@NonNull User user) {
        user.leaveClan();
        this.members.add(user.getUniqueId());
        user.joinClan(this);
    }

    @Override
    public void addOperator(@NonNull User user) {
        UUID uuid = user.getUniqueId();
        this.members.add(uuid);
        this.operators.add(uuid);
    }

    @Override
    public void addModerator(@NonNull User user) {
        UUID uuid = user.getUniqueId();
        this.members.add(uuid);
        this.moderators.add(uuid);
    }

    @Override
    public void removeMember(User user) {
        UUID uuid = user.getUniqueId();
        this.members.remove(uuid);
        this.moderators.remove(uuid);
        this.operators.remove(uuid);
        if (operators.isEmpty()) delete();
    }

    @Override
    public void removeModerator(User user) {
        this.moderators.remove(user.getUniqueId());
    }

    @Override
    public void removeOperator(User user) {
        this.operators.remove(user.getUniqueId());
    }

    @Override
    public void removeMember(UUID uuid) {
        this.moderators.remove(uuid);
        this.members.remove(uuid);
        this.operators.remove(uuid);
        if (operators.isEmpty()) delete();
    }

    @Override
    public void removeModerator(UUID uuid) {
        this.moderators.remove(uuid);
    }

    @Override
    public void removeOperator(UUID uuid) {
        this.operators.remove(uuid);
    }

    @Override
    public Collection<User> getOperators() {
        return this.operators.stream().map(uuid -> Backend.user(uuid).get())
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public Collection<User> getMembers() {
        return this.members.stream().map(uuid -> Backend.user(uuid).get())
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public Collection<User> getModerators() {
        return this.moderators.stream().map(uuid -> Backend.user(uuid).get())
                .filter(Objects::nonNull)
                .toList();
    }
}
