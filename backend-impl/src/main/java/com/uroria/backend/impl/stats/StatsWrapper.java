package com.uroria.backend.impl.stats;

import com.google.gson.JsonObject;
import com.uroria.backend.Backend;
import com.uroria.backend.impl.communication.CommunicationWrapper;
import com.uroria.backend.impl.wrapper.Wrapper;
import com.uroria.backend.stats.Stat;
import com.uroria.backend.user.User;
import lombok.NonNull;

import java.util.Map;
import java.util.UUID;

public class StatsWrapper extends Wrapper implements Stat {
    private final StatsManager statsManager;
    private final UUID uuid;
    private final int gameId;

    private boolean deleted;

    public StatsWrapper(StatsManager statsManager, UUID uuid, int gameId) {
        this.statsManager = statsManager;
        this.uuid = uuid;
        this.gameId = gameId;
    }

    @Override
    public void delete() {
        if (isDeleted()) return;
        this.deleted = true;
        getObjectWrapper().set("deleted", true);
    }

    @Override
    public boolean isDeleted() {
        if (this.deleted) return true;
        boolean val = getBoolean("deleted");
        this.deleted = val;
        return val;
    }

    @Override
    public User getUser() {
        return Backend.getUser(uuid).get();
    }

    @Override
    public int getGameId() {
        return this.gameId;
    }

    @Override
    public long getDate() {
        return 0;
    }

    @Override
    public void setScore(@NonNull String key, int score) {

    }

    @Override
    public void setScore(@NonNull String key, float score) {

    }

    @Override
    public int getScoreOrElse(String key, int defValue) {
        return 0;
    }

    @Override
    public float getScoreOrElse(String key, float defValue) {
        return 0;
    }

    @Override
    public Map<String, Number> getScores() {
        return null;
    }

    @Override
    public void refresh() {

    }

    @Override
    public JsonObject getObject() {
        return null;
    }

    @Override
    public CommunicationWrapper getObjectWrapper() {
        return null;
    }

    @Override
    public String getIdentifierKey() {
        return null;
    }

    @Override
    public String getStringIdentifier() {
        return null;
    }
}
