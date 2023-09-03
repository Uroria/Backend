package com.uroria.backend.user;

import com.uroria.backend.Backend;
import com.uroria.backend.BackendObject;
import com.uroria.backend.clan.Clan;
import com.uroria.backend.permission.PermCalculator;
import com.uroria.backend.permission.PermGroup;
import com.uroria.backend.permission.PermHolder;
import com.uroria.base.gson.annotations.GsonTransient;
import com.uroria.base.lang.Language;
import com.uroria.base.permission.PermState;
import com.uroria.base.user.UroriaUser;
import com.uroria.base.user.UserStatus;
import com.uroria.base.utils.CollectionUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class User extends BackendObject<User> implements Serializable, UroriaUser {
    @Serial private static final long serialVersionUID = 1;

    private final UUID uuid;
    private String name;
    private @NotNull String language;
    private @Getter @Setter boolean online;
    private int status;
    private String clanTag;
    @GsonTransient
    private transient PermHolder permHolder;

    public User(@NonNull UUID uuid) {
        this.uuid = uuid;
        this.language = Language.DEFAULT.toTag();
        this.status = UserStatus.DEFAULT.toCode();
    }

    @Override
    public UserStatus getRealStatus() {
        return UserStatus.fromCode(this.status);
    }

    @Override
    public UserStatus getStatus() {
        if (this.online) return getRealStatus();
        return UserStatus.INVISIBLE;
    }

    /**
     * Sets the online status of the user.
     * {@link User#update() Required to update!}
     */
    public void setStatus(@NonNull UserStatus status) {
        this.status = status.toCode();
    }

    /**
     * Sets the users locale.
     * {@link User#update() Required to update!}
     */
    public void setLanguage(@NonNull Language language) {
        this.language = language.toTag();
    }

    /**
     * Sets the users username.
     * {@link User#update() Required to update!}
     */
    public void setUsername(@NonNull String username) {
        this.name = username.toLowerCase();
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public String getUsername() {
        return this.name;
    }

    @Override
    public @NotNull Language getLanguage() {
        return Language.fromTag(this.language);
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
        this.language = user.language;
        this.online = user.online;
        this.status = user.status;
        CollectionUtils.overrideMap(this.properties, user.properties);
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

    @Override
    public boolean hasPermission(@Nullable String s) {
        checkPermHolder();
        return getPermissionState(s).asBooleanValue();
    }

    @Override
    public @NotNull PermState getPermissionState(@Nullable String node) {
        checkPermHolder();
        if (node == null) return PermState.NOT_SET;

        if (permHolder == null) return PermState.NOT_SET;

        List<PermGroup> groups = permHolder.getGroups();

        boolean group = groups.stream().min(Comparator.comparing(PermGroup::getPriority))
                .map(group1 -> group1.hasPermission(node)).orElse(false);
        boolean holder = permHolder.hasPermission(node);

        if (holder) return PermState.TRUE;
        if (group) return PermState.TRUE;
        return PermState.FALSE;
    }

    @Override
    public void setPermission(@NonNull String node, boolean val) {
        checkPermHolder();
        this.permHolder.setPermission(node, val);
        this.permHolder.update();
    }

    @Override
    public void setPermission(@NonNull String node, @NonNull PermState state) {
        checkPermHolder();
        this.permHolder.setPermission(node, state.asBooleanValue());
        this.permHolder.update();
    }

    @Override
    public void unsetPermission(@NonNull String node) {
        checkPermHolder();
        this.permHolder.unsetPermission(node);
        this.permHolder.update();
    }

    private void checkPermHolder() {
        if (this.permHolder != null) return;
        this.permHolder = Backend.getAPI().getPermissionManager().getHolder(this.uuid).orElse(null);
    }
}
