package com.uroria.backend.impl.user;

import com.uroria.backend.clan.Clan;
import com.uroria.backend.permission.PermGroup;
import com.uroria.backend.permission.Permission;
import com.uroria.backend.stats.Stat;
import com.uroria.backend.user.User;
import com.uroria.backend.user.punishment.Punishment;
import com.uroria.backend.user.punishment.mute.Mute;
import com.uroria.base.lang.Language;
import com.uroria.base.user.UserStatus;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class UserWrapper extends AbstractUser {
    private final AbstractUserManager userManager;

    public UserWrapper(AbstractUserManager userManager, @NonNull UUID uuid, long firstJoin) {
        super(uuid, firstJoin);
        this.userManager = userManager;
    }

    @Override
    public boolean isDeleted() {
        if (this.deleted == -1) {
            return
        }
        return deleted == 1;
    }

    @Override
    public Map<String, Object> getProperties() {
        return null;
    }

    @Override
    public String getPropertyStringOrElse(@NonNull String key, @Nullable String defValue) {
        return null;
    }

    @Override
    public int getPropertyIntOrElse(@NonNull String key, int defValue) {
        return 0;
    }

    @Override
    public long getPropertyLongOrElse(@NonNull String key, long defValue) {
        return 0;
    }

    @Override
    public double getPropertyDoubleOrElse(@NonNull String key, double defValue) {
        return 0;
    }

    @Override
    public float getPropertyFloatOrElse(@NonNull String key, float defValue) {
        return 0;
    }

    @Override
    public boolean getPropertyBooleanOrElse(@NonNull String key, boolean defValue) {
        return false;
    }

    @Override
    public List<PermGroup> getPermGroups() {
        return null;
    }

    @Override
    public Permission getPermission(String node) {
        return null;
    }

    @Override
    public List<Stat> getStats(@NonNull UUID holder, int gameId) {
        return null;
    }

    @Override
    public List<Stat> getStatsWithScoreGreaterThanValue(@NonNull UUID uuid, int gameId, @NonNull String scoreKey, long value) {
        return null;
    }

    @Override
    public List<Stat> getStatsWithScoreLowerThanValue(@NonNull UUID holder, int gameId, @NonNull String scoreKey, long value) {
        return null;
    }

    @Override
    public List<Stat> getStatsWithScore(@NonNull UUID holder, int gameId, @NonNull String scoreKey, long value) {
        return null;
    }

    @Override
    public List<Stat> getStatsInTimeRangeOf(@NonNull UUID holder, int gameId, long startMs, long endMs) {
        return null;
    }

    @Override
    public List<Stat> getStats(int gameId) {
        return null;
    }

    @Override
    public List<Stat> getStatsWithScoreGreaterThanValue(int gameId, @NonNull String scoreKey, long value) {
        return null;
    }

    @Override
    public List<Stat> getStatsWithScoreLowerThanValue(int gameId, @NonNull String scoreKey, long value) {
        return null;
    }

    @Override
    public List<Stat> getStatsWithScore(int gameId, @NonNull String scoreKey, long value) {
        return null;
    }

    @Override
    public List<Stat> getStatsInTimeRangeOf(int gameId, long startMs, long endMs) {
        return null;
    }

    @Override
    public @NotNull String getUsername() {
        return null;
    }

    @Override
    public @NotNull Language getLanguage() {
        return null;
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public UserStatus getStatus() {
        return null;
    }

    @Override
    public UserStatus getRealStatus() {
        return null;
    }

    @Override
    public long getPlaytime() {
        return 0;
    }

    @Override
    public long getLastJoin() {
        return 0;
    }

    @Override
    public long getFirstJoin() {
        return 0;
    }

    @Override
    public List<User> getFriends() {
        return null;
    }

    @Override
    public List<User> getFriendRequests() {
        return null;
    }

    @Override
    public Optional<Clan> getClan() {
        return Optional.empty();
    }

    @Override
    public List<User> getCrew() {
        return null;
    }

    @Override
    public List<Punishment> getActivePunishments() {
        return null;
    }

    @Override
    public List<Punishment> getExpiredPunishments() {
        return null;
    }

    @Override
    public List<Mute> getActiveMutes() {
        return null;
    }

    @Override
    public List<Mute> getExpiredMutes() {
        return null;
    }

    @Override
    public Object getObject(int key, Object defVal) {
        return null;
    }

    @Override
    public String getString(int key, String defVal) {
        return null;
    }

    @Override
    public int getInt(int key, int defVal) {
        return 0;
    }

    @Override
    public long getLong(int key, int defVal) {
        return 0;
    }

    @Override
    public boolean getBoolean(int key, boolean defVal) {
        return false;
    }

    @Override
    public float getFloat(int key, float defVal) {
        return 0;
    }

    @Override
    public double getDouble(int key, double defVal) {
        return 0;
    }

    @Override
    public void updateObject(int key, @Nullable Object value) {

    }

    @Override
    public void updateString(int key, @Nullable String value) {

    }

    @Override
    public int updateInt(int key, int value) {
        return 0;
    }

    @Override
    public long updateLong(int key, long value) {
        return 0;
    }

    @Override
    public boolean updateBoolean(int key, boolean value) {
        return false;
    }

    @Override
    public float updateFloat(int key, float value) {
        return 0;
    }

    @Override
    public double updateDouble(int key, double value) {
        return 0;
    }

    @Override
    public List<UUID> getUnsafeCrew() {
        return null;
    }

    @Override
    public List<UUID> getUnsafeFriends() {
        return null;
    }

    @Override
    public List<UUID> getUnsafeFriendRequests() {
        return null;
    }
}
