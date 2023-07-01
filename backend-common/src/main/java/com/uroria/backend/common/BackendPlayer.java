package com.uroria.backend.common;

import com.uroria.backend.common.helpers.PlayerStatus;
import com.uroria.backend.common.helpers.PropertyHolder;
import com.uroria.backend.common.utils.ObjectUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.net.URL;
import java.util.*;

public final class BackendPlayer extends PropertyHolder<BackendPlayer> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1;
    private final UUID uuid;
    private final List<UUID> crew;
    private final List<BackendPunishment> outdatedPunishments;
    private String skinURL;
    private String avatar;
    private String clan;
    private String currentName;
    private Locale locale;
    private int status;
    private BackendPunishment punishment;
    public BackendPlayer(UUID uuid, String currentName) {
        this.uuid = uuid;
        if (currentName == null) this.currentName = null;
        else this.currentName = currentName.toLowerCase();
        this.crew = new ArrayList<>();
        this.locale = Locale.GERMAN;
        this.clan = null;
        this.status = 0;
        this.punishment = null;
        this.outdatedPunishments = new ArrayList<>();
    }

    @Override
    public synchronized void modify(BackendPlayer player) {
        this.skinURL = player.skinURL;
        this.avatar = player.avatar;
        this.clan = player.clan;
        this.currentName = player.currentName;
        this.locale = player.locale;
        this.status = player.status;
        this.punishment = player.punishment;
        ObjectUtils.overrideCollection(outdatedPunishments, player.outdatedPunishments);
        ObjectUtils.overrideCollection(crew, player.crew);
        ObjectUtils.overrideMap(properties, player.properties);
    }

    public void clearOutdatedPunishments() {
        this.outdatedPunishments.clear();
    }

    public boolean wasPunishedBefore() {
        return !this.outdatedPunishments.isEmpty();
    }

    public boolean isPunished() {
        if (this.punishment == null) return false;
        if (this.punishment.isOutdated()) {
            this.outdatedPunishments.add(this.punishment);
            this.punishment = null;
            return false;
        }
        return true;
    }

    public Optional<BackendPunishment> getPunishment() {
        if (this.punishment != null && this.punishment.isOutdated()) {
            this.outdatedPunishments.add(this.punishment);
            this.punishment = null;
            return Optional.empty();
        }
        return Optional.ofNullable(this.punishment);
    }

    public void punish(BackendPunishment punishment) {
        if (punishment == null) throw new NullPointerException("Punishment cannot be null");
        this.punishment = punishment;
    }

    public void unpunish() {
        if (this.punishment != null) {
            this.outdatedPunishments.add(this.punishment);
            this.punishment = null;
        }
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
        if (name == null) {
            this.currentName = null;
            return;
        }
        this.currentName = name.toLowerCase();
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

    public Collection<BackendPunishment> getOutdatedPunishments() {
        return outdatedPunishments;
    }

    public void removeCrewMember(UUID uuid) {
        this.crew.remove(uuid);
    }

    public Optional<URL> getSkinURL() {
        if (this.skinURL == null) return Optional.empty();
        try {
            URL url = new URL(this.skinURL);
            return Optional.of(url);
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    public void setSkinURL(URL url) {
        if (url == null) {
            this.skinURL = null;
            return;
        }
        this.skinURL = url.toString();
    }

    public Optional<BufferedImage> getAvatar() {
        if (this.avatar == null) return Optional.empty();
        try (ByteArrayInputStream input = new ByteArrayInputStream(Base64.getDecoder().decode(this.avatar))) {
            return Optional.of(ImageIO.read(input));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    public void setAvatar(BufferedImage image) {
        if (image == null) {
            this.avatar = null;
            return;
        }
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", output);
            output.close();
            this.avatar = Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (Exception exception) {
            throw new RuntimeException("Cannot set avatar");
        }
    }

    @Override
    public String toString() {
        return this.uuid + "-" + this.currentName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof BackendPlayer player) {
            return player.getUUID().equals(this.uuid);
        }
        return false;
    }
}
