package com.uroria.backend.punishment;

import com.uroria.backend.Backend;
import com.uroria.backend.BackendObject;
import com.uroria.base.utils.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class Punished extends BackendObject<Punished> implements Serializable {
    @Serial private static final long serialVersionUID = 1;

    private final UUID uuid;
    private final ObjectList<Punishment> punishments;

    public Punished(@NonNull UUID uuid) {
        this.uuid = uuid;
        this.punishments = new ObjectArrayList<>();
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public void punish(@NonNull Punishment punishment) {
        if (this.punishments.contains(punishment)) return;
        this.punishments.add(punishment);
    }

    public void unpunish(Punishment punishment) {
        if (punishment == null) return;
        this.punishments.remove(punishment);
    }

    public Optional<Punishment> getCurrentPunishment() {
        return this.punishments.stream().findAny();
    }

    public List<Punishment> getPunishments() {
        return Collections.unmodifiableList(this.punishments);
    }

    @Override
    public void modify(Punished punished) {
        this.deleted = punished.deleted;
        CollectionUtils.overrideCollection(punishments, punished.punishments);
        this.punishments.removeIf(BackendObject::isDeleted);
        for (Punishment punishment : this.punishments) {
            Optional<Punishment> any = punished.punishments.stream().filter(sub -> sub.equals(punishment)).findAny();
            if (any.isEmpty()) continue;
            punishment.modify(any.get());
        }
    }

    @Override
    public void update() {
        Backend.getAPI().getPunishmentManager().updatePunished(this);
    }

    @Override
    public String toString() {
        return "Punished{uuid="+this.uuid+"}";
    }
}
