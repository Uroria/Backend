package com.uroria.backend.common;

import com.uroria.backend.common.helpers.PropertyHolder;
import com.uroria.backend.common.utils.ObjectUtils;
import lombok.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CosmeticHolder extends PropertyHolder<CosmeticHolder> implements Serializable {
    @Serial private static final long serialVersionUID = 1;

    private final UUID uuid;
    private final List<Integer> cosmetics;
    private final List<Integer> equippedCosmetics;

    public CosmeticHolder(@NonNull UUID uuid) {
        this.uuid = uuid;
        this.cosmetics = new ArrayList<>();
        this.equippedCosmetics = new ArrayList<>();
    }

    public void addCosmetic(int id) {
        this.cosmetics.add(id);
    }

    public void removeCosmetic(int id) {
        this.equippedCosmetics.remove(id);
        this.cosmetics.remove(id);
    }

    public void equipCosmetic(int id) {
        if (!this.cosmetics.contains(id)) this.cosmetics.add(id);
        this.equippedCosmetics.add(id);
    }

    public void disrobeCosmetic(int id) {
        this.equippedCosmetics.remove(id);
    }

    public List<Integer> getEquippedCosmetics() {
        this.equippedCosmetics.retainAll(this.cosmetics);
        return new ArrayList<>(this.equippedCosmetics);
    }

    public List<Integer> getCosmetics() {
        return new ArrayList<>(this.cosmetics);
    }

    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public void modify(CosmeticHolder holder) {
        ObjectUtils.overrideCollection(cosmetics, holder.cosmetics);
        ObjectUtils.overrideCollection(equippedCosmetics, holder.equippedCosmetics);
    }
}
