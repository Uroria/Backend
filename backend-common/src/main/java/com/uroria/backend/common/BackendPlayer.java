package com.uroria.backend.common;

import com.uroria.backend.common.helpers.PlayerStatus;
import com.uroria.backend.common.helpers.PropertyHolder;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public final class BackendPlayer extends PropertyHolder implements Serializable {
    @Serial
    private static final long serialVersionUID = 1;
    private final UUID uuid;
    private String clan;
    private final Collection<UUID> crew;
    private String currentName;
    private Locale locale;
    private int status;
    public BackendPlayer(UUID uuid, String currentName) {
        this.uuid = uuid;
        this.currentName = currentName;
        this.crew = new ArrayList<>();
        this.locale = Locale.ENGLISH;
        this.clan = null;
        this.status = 0;
    }

    public Optional<String> getClan() {
        return Optional.ofNullable(this.clan);
    }

    public void setClan(BackendClan clan) {
        if (clan == null) throw new NullPointerException("Clan cannot be null");
        this.clan = clan.getName();
        clan.addMember(this.uuid);
    }

    public void setClan(String clan) {
        if (clan == null) throw new NullPointerException("Clan cannot be null");
        this.clan = clan;
    }

    public void leaveClan() {
        this.clan = null;
    }

    public void setStatus(PlayerStatus status) {
        this.status = status.getId();
    }

    public PlayerStatus getStatus() {
        return PlayerStatus.getById(this.status);
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void setCurrentName(String name) {
        this.currentName = name;
    }

    public UUID getUUID() {
        return uuid;
    }

    public Optional<String> getCurrentName() {
        return Optional.ofNullable(currentName);
    }

    public Locale getLocale() {
        return locale;
    }

    public boolean isCrewMember(UUID uuid) {
        return this.crew.contains(uuid);
    }

    public Collection<UUID> getCrew() {
        return new ArrayList<>(crew);
    }

    public void addCrewMember(UUID uuid) {
        this.crew.add(uuid);
    }

    public void removeCrewMember(UUID uuid) {
        this.crew.remove(uuid);
    }
}
