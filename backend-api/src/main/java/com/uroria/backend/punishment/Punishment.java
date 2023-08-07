package com.uroria.backend.punishment;

import com.uroria.backend.BackendObject;
import lombok.Getter;
import lombok.NonNull;

import java.io.Serializable;

public final class Punishment extends BackendObject<Punishment> implements Serializable {
    private @Getter final String reason;
    private int type;
    private long endDate;

    public Punishment(@NonNull String reason, @NonNull PunishmentType type, long endDate) {
        this.reason = reason;
        this.type = type.getID();
        if (type.isPermanent()) this.endDate = -1;
        else this.endDate = endDate;
    }

    /**
     * Get EndDate or -1 if permanent
     */
    public long getEndDateMs() {
        return this.endDate;
    }

    public boolean isPermanent() {
        return this.endDate == -1;
    }

    public void setEndDateMs(long endDateMs) {
        this.endDate = endDateMs;
    }

    public void setDeprecated() {
        setType(PunishmentType.NONE);
    }
    
    public boolean isDeprecated() {
        return !getType().isValid();
    }

    public void setType(@NonNull PunishmentType type) {
        this.type = type.getID();
    }

    public void unban() {
        this.type = PunishmentType.NONE.getID();
        this.endDate = -1;
    }

    public PunishmentType getType() {
        return PunishmentType.byID(this.type);
    }

    @Override
    public void modify(Punishment punishment) {
        this.deleted = punishment.deleted;
        this.type = punishment.type;
        this.endDate = punishment.endDate;
    }

    @Override
    public void update() {

    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Punishment punishment) {
            return punishment.endDate == this.endDate && punishment.type == this.type;
        }
        return false;
    }
}
