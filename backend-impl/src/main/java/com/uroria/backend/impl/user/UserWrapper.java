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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("unchecked")
public final class UserWrapper extends AbstractUser {
    public UserWrapper(AbstractUserManager userManager, @NonNull UUID uuid) {
        super(userManager, uuid);
    }

    @Override
    public boolean isDeleted() {
        return getBoolean(1, false);
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
        return getString(1, "N/A");
    }

    @Override
    public @NotNull Language getLanguage() {
        return Language.fromTag(getString(2, "nil"));
    }

    @Override
    public boolean isOnline() {
        return getBoolean(2, true);
    }

    @Override
    public UserStatus getStatus() {
        if (!isOnline()) return UserStatus.INVISIBLE;
        return getRealStatus();
    }

    @Override
    public UserStatus getRealStatus() {
        return UserStatus.fromCode(getInt(1, 0));
    }

    @Override
    public long getPlaytime() {
        return getLong(2, 0);
    }

    @Override
    public long getLastJoin() {
        return getLong(1, 0);
    }

    @Override
    public long getFirstJoin() {
        return getLong(3, 0);
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
        return null;
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
