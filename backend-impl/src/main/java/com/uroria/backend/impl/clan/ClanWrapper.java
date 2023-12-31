package com.uroria.backend.impl.clan;

import com.uroria.backend.Backend;
import com.uroria.backend.cache.Wrapper;
import com.uroria.backend.cache.WrapperManager;
import com.uroria.backend.clan.Clan;
import com.uroria.backend.user.User;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.NonNull;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public final class ClanWrapper extends Wrapper implements Clan {
    private final String name;
    private boolean deleted;

    ClanWrapper(WrapperManager<ClanWrapper> wrapperManager, String name) {
        super(wrapperManager);
        this.object.set("name", name);
        this.name = name;
    }

    @Override
    public void delete() {
        if (isDeleted()) return;
        this.deleted = true;
        for (User user : getMembers()) {
            user.leaveClan();
        }
        this.object.set("deleted", true);
    }

    @Override
    public boolean isDeleted() {
        if (this.deleted) return true;
        boolean deleted = this.object.getBooleanOrElse("deleted", false);
        this.deleted = deleted;
        return deleted;
    }

    @Override
    public String getTag() {
        return this.object.getStringOrElse("tag", getName());
    }

    @Override
    public String getName() {
        return this.object.getStringOrElse("name", this.name);
    }

    @Override
    public void setTag(@NonNull String tag) {
        if (isDeleted()) return;
        this.object.set("tag", tag);
    }

    @Override
    public long getFoundingDate() {
        return this.object.getLongOrElse("foundingDate", 0);
    }

    @Override
    public boolean hasMember(@NonNull User user) {
        return this.object.getSet("members", String.class).stream()
                .anyMatch(str -> user.getUniqueId().toString().equals(str));
    }

    @Override
    public void addMember(@NonNull User user) {
        if (isDeleted()) return;
        if (hasMember(user)) return;
        ObjectSet<String> moderators = this.object.getSet("moderators", String.class);
        moderators.add(user.getUniqueId().toString());
        this.object.set("members", moderators);
    }

    @Override
    public void addOperator(@NonNull User user) {
        if (isDeleted()) return;
        addMember(user);
        ObjectSet<String> moderators = this.object.getSet("moderators", String.class);
        moderators.add(user.getUniqueId().toString());
        this.object.set("operators", moderators);
    }

    @Override
    public void addModerator(@NonNull User user) {
        if (isDeleted()) return;
        addMember(user);
        ObjectSet<String> moderators = this.object.getSet("moderators", String.class);
        moderators.add(user.getUniqueId().toString());
        this.object.set("moderators", moderators);
    }

    @Override
    public void removeMember(User user) {
        if (user == null) return;
        if (isDeleted()) return;
        removeMember(user.getUniqueId());
    }

    @Override
    public void removeModerator(User user) {
        if (user == null) return;
        if (isDeleted()) return;
        removeModerator(user.getUniqueId());
    }

    @Override
    public void removeOperator(User user) {
        if (user == null) return;
        if (isDeleted()) return;
        removeOperator(user.getUniqueId());
    }

    @Override
    public void removeMember(UUID uuid) {
        if (uuid == null) return;
        if (isDeleted()) return;
        removeModerator(uuid);
        removeOperator(uuid);
        object.getSet("members").remove(uuid.toString());
    }

    @Override
    public void removeModerator(UUID uuid) {
        if (uuid == null) return;
        if (isDeleted()) return;
        object.getSet("moderators").remove(uuid.toString());
    }

    @Override
    public void removeOperator(UUID uuid) {
        if (isDeleted()) return;
        if (uuid == null) return;
        object.getSet("operators").remove(uuid.toString());
        if (getOperators().isEmpty()) delete();
    }

    @Override
    public Collection<User> getOperators() {
        return getUsers("operators");
    }

    @Override
    public Collection<User> getMembers() {
        return getUsers("members");
    }

    @Override
    public Collection<User> getModerators() {
        return getUsers("moderators");
    }

    private Collection<User> getUsers(String key) {
        return object.getSet(key, String.class).stream()
                .map(str -> {
                    try {
                        return UUID.fromString(str);
                    } catch (Exception exception) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(uuid -> Backend.user(uuid).get())
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public String getIdentifier() {
        return this.name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClanWrapper wrapper) {
            return wrapper.name.equals(this.name);
        }
        return false;
    }
}
