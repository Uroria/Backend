package com.uroria.backend;

import com.uroria.backend.helpers.CosmeticType;
import com.uroria.backend.helpers.PropertyHolder;
import lombok.NonNull;

import java.io.Serial;
import java.io.Serializable;

public final class Cosmetic extends PropertyHolder<Cosmetic> implements Serializable {
    @Serial private static final long serialVersionUID = 1;
    private final int id;
    private final int type;
    private final long releaseDate;
    private int currentPrice;
    private int price;

    public Cosmetic(int id, @NonNull CosmeticType type, long releaseDate, int price) {
        this.id = id;
        this.type = type.getId();
        this.releaseDate = releaseDate;
        this.currentPrice = price;
        this.price = price;
    }

    public void setCurrentPrice(int price) {
        this.currentPrice = price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public CosmeticType getType() {
        return CosmeticType.getByID(this.type);
    }

    public long getReleaseDateMs() {
        return releaseDate;
    }

    public int getID() {
        return id;
    }

    public int getCurrentPrice() {
        return currentPrice;
    }

    public int getPrice() {
        return price;
    }

    @Override
    public void modify(Cosmetic cosmetic) {
        currentPrice = cosmetic.currentPrice;
        price = cosmetic.price;
    }
}
