package com.uroria.backend.clan;

import com.uroria.backend.Backend;
import com.uroria.backend.property.PropertyObject;
import com.uroria.backend.user.User;
import com.uroria.backend.utils.ObjectUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.Getter;
import lombok.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class Clan extends PropertyObject<Clan> implements Serializable {
    @Serial private static final long serialVersionUID = 1;

    private final ObjectList<UUID> members;
    private final ObjectList<UUID> moderators;
    private final long foundingDate;
    private @Getter final String name;
    private @Getter String tag;
    private UUID operator;

    public Clan(@NonNull String name, @NonNull String tag, @NonNull UUID operator, long foundingDate) {
        this.name = name;
        this.tag = tag;
        this.operator = operator;
        this.foundingDate = foundingDate;
        this.members = new ObjectArrayList<>();
        this.moderators = new ObjectArrayList<>();
        this.members.add(operator);
    }

    public Clan(@NonNull String name, @NonNull String tag, @NonNull UUID operator) {
        this(name, tag, operator, System.currentTimeMillis());
    }

    public void addModerator(@NonNull User user) {
        addMember(user);
        if (!this.moderators.contains(user.getUUID())) this.moderators.add(user.getUUID());
        update();
    }

    public void removeModerator(User user) {
        if (user == null) return;
        this.moderators.remove(user.getUUID());
        update();
    }

    public boolean isMember(User user) {
        if (user == null) return false;
        return this.members.contains(user.getUUID());
    }

    public void addMember(@NonNull User user) {
        if (!user.getClanTag().map(tag -> this.tag.equals(tag)).orElse(false)) user.setClan(this);
        UUID uuid = user.getUUID();
        if (this.members.contains(uuid)) return;
        this.members.add(uuid);
        update();
    }

    public void removeMember(UUID uuid) {
        if (uuid == null) return;
        this.members.remove(uuid);
        this.moderators.remove(uuid);
        if (this.operator.equals(uuid)) {
            if (moderators.isEmpty()) this.operator = this.members.stream().findAny().orElse(null);
            else this.operator = this.moderators.stream().findAny().orElse(null);
            if (this.operator == null) {
                delete();
            }
        }
        update();
    }

    /**
     * Sets the clans tag used to search for it.
     * {@link Clan#update() Required to update!}
     */
    public void setTag(@NonNull String tag) {
        this.tag = tag;
        this.members.forEach(uuid -> {
            Backend.getAPI().getUserManager().getUser(uuid).ifPresent(user -> {
                user.setClan(this);
            });
        });
    }

    public long getFoundingDateMs() {
        return this.foundingDate;
    }

    public void setOperator(@NonNull UUID uuid) {
        if (!this.members.contains(uuid)) this.members.add(uuid);
        this.operator = uuid;
        update();
    }

    public UUID getOperator() {
        return this.operator;
    }

    public List<UUID> getMembers() {
        return Collections.unmodifiableList(this.members);
    }

    public List<UUID> getModerators() {
        return Collections.unmodifiableList(this.moderators);
    }

    @Override
    public void modify(Clan clan) {
        ObjectUtils.overrideCollection(this.members, clan.members);
        ObjectUtils.overrideCollection(this.moderators, clan.moderators);
        this.tag = clan.tag;
        this.operator = clan.operator;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Clan clan)) return false;
        return clan.getName().equals(this.name);
    }

    @Override
    public String toString() {
        return "Clan{name="+this.name+", tag="+this.tag+"}";
    }

    @Override
    public void update() {
        Backend.getAPI().getClanManager().updateClan(this);
    }
}
