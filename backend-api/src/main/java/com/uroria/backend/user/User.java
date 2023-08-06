package com.uroria.backend.user;

import com.uroria.backend.Backend;
import com.uroria.backend.clan.Clan;
import com.uroria.backend.property.PropertyObject;
import com.uroria.backend.utils.ObjectUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public final class User extends PropertyObject<User> implements Serializable {
    @Serial private static final long serialVersionUID = 1;

    private final UUID uuid;
    private String name;
    private @Getter @NotNull Locale locale;
    private @Getter @Setter boolean online;
    private int status;
    private String clanTag;

    public User(@NonNull UUID uuid) {
        this.uuid = uuid;
        this.locale = Locale.GERMAN;
        this.status = OnlineStatus.ONLINE.getID();
    }

    public OnlineStatus getStatus() {
        if (!isOnline()) return OnlineStatus.INVISIBLE;
        return OnlineStatus.byID(this.status);
    }

    /**
     * Sets the online status of the user.
     * {@link User#update() Required to update!}
     */
    public void setStatus(@NonNull OnlineStatus status) {
        this.status = status.getID();
    }

    /**
     * Sets the users locale.
     * {@link User#update() Required to update!}
     */
    public void setLocale(@NonNull Locale locale) {
        this.locale = locale;
    }

    /**
     * Sets the users username.
     * {@link User#update() Required to update!}
     */
    public void setUsername(@NonNull String username) {
        this.name = username.toLowerCase();
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getUsername() {
        return this.name;
    }

    public Optional<String> getClanTag() {
        return Optional.ofNullable(this.clanTag);
    }

    public Optional<Clan> getClan() {
        return Backend.getAPI().getClanManager().getClan(this.clanTag);
    }

    public void setClan(@NonNull Clan clan) {
        this.clanTag = clan.getTag();
        update();
        if (clan.isMember(this)) return;
        clan.addMember(this);
    }

    /**
     * Automatically quits the clan the user is currently in.
     */
    public void quitClan() {
        if (this.clanTag == null) return;
        Backend.getAPI().getClanManager().getClan(this.clanTag).ifPresent(clan -> {
            clan.removeMember(this.uuid);
        });
        this.clanTag = null;
        update();
    }

    @Override
    public void modify(User user) {
        this.deleted = user.deleted;
        this.name = user.name;
        this.locale = user.locale;
        this.online = user.online;
        this.status = user.status;
        ObjectUtils.overrideMap(this.properties, user.properties);
    }

    @Override
    public void update() {
        Backend.getAPI().getUserManager().updateUser(this);
    }

    @Override
    public String toString() {
        return "User{uuid="+this.uuid+", name="+this.name+"}";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof User user)) return false;
        return this.uuid.equals(user.uuid);
    }
}
