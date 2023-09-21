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
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
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

    public List<UUID> getUnsafeCrew() {
        return null;
    }

    public List<UUID> getUnsafeFriends() {
        return null;
    }

    public List<UUID> getUnsafeFriendRequests() {
        return null;
    }

    @Override
    public Map<String, Object> getProperties() {
        return (Map<String, Object>) getObject(1, new Object2ObjectArrayMap<>());
    }

    @Override
    public String getPropertyStringOrElse(@NonNull String key, @Nullable String defValue) {
        String string = (String) getProperties().get(key);
        if (string == null) return defValue;
        return string;
    }

    @Override
    public int getPropertyIntOrElse(@NonNull String key, int defValue) {
        Integer i = (Integer) getProperties().get(key);
        if (i == null) return defValue;
        return i;
    }

    @Override
    public long getPropertyLongOrElse(@NonNull String key, long defValue) {
        Long l = (Long) getProperties().get(key);
        if (l == null) return defValue;
        return l;
    }

    @Override
    public double getPropertyDoubleOrElse(@NonNull String key, double defValue) {
        Double d = (Double) getProperties().get(key);
        if (d == null) return defValue;
        return d;
    }

    @Override
    public float getPropertyFloatOrElse(@NonNull String key, float defValue) {
        Float f = (Float) getProperties().get(key);
        if (f == null) return defValue;
        return f;
    }

    @Override
    public boolean getPropertyBooleanOrElse(@NonNull String key, boolean defValue) {
        Boolean b = (Boolean) getProperties().get(key);
        if (b == null) return defValue;
        return b;
    }

    @Override
    public void delete() {
        updateBoolean(1, true);
    }

    @Override
    public UUID getUniqueId() {
        return this.uuid;
    }

    @Override
    public void addCrewMember(@NonNull User user) {
        List<UUID> crew = getUnsafeCrew();
        crew.add(user.getUniqueId());
        updateObject(4, crew);
    }

    @Override
    public void removeCrewMember(User user) {
        if (user == null) return;
        removeCrewMember(user.getUniqueId());
    }

    @Override
    public void removeCrewMember(UUID uuid) {
        if (uuid == null) return;
        List<UUID> crew = getUnsafeCrew();
        crew.remove(uuid);
        updateObject(4, crew);
    }

    public void setFirstJoin(long firstJoin) {
        updateLong(3, firstJoin);
    }

    @Override
    public void setPlaytime(long playtime) {
        updateLong(2, playtime);
    }

    @Override
    public void setLastJoin(long lastJoin) {
        updateLong(1, lastJoin);
    }

    @Override
    public void setStatus(@NonNull UserStatus status) {
        updateInt(1, status.toCode());
    }

    @Override
    public void setLanguage(@NonNull Language language) {
        updateString(2, language.toTag());
    }

    @Override
    public void setUsername(@NonNull String username) {
        updateString(1, username);
    }

    @Override
    public void addFriendRequest(@NonNull User user) {
        List<UUID> friendRequests = getUnsafeFriendRequests();
        friendRequests.add(user.getUniqueId());
        updateObject(3, friendRequests);
    }

    @Override
    public void removeFriendRequest(User user) {
        if (user == null) return;
        removeFriendRequest(user.getUniqueId());
    }

    @Override
    public void removeFriendRequest(UUID uuid) {
        if (uuid == null) return;
        List<UUID> friendRequests = getUnsafeFriendRequests();
        friendRequests.remove(uuid);
        updateObject(3, friendRequests);
    }

    @Override
    public void addFriend(@NonNull User user) {
        List<UUID> friends = getUnsafeFriends();
        friends.add(user.getUniqueId());
        updateObject(2, friends);
    }

    @Override
    public void removeFriend(User user) {
        if (user == null) return;
        removeFriend(user.getUniqueId());
    }

    @Override
    public void removeFriend(UUID uuid) {
        if (uuid == null) return;
        List<UUID> friends = getUnsafeFriends();
        friends.remove(uuid);
        updateObject(2, friends);
    }

    @Override
    public void joinClan(@NonNull Clan clan) {
        updateString(3, clan.getName());
    }

    @Override
    public void leaveClan() {
        if (getClan().isEmpty()) return;
        updateString(3, null);
    }

    public void setProperty(@NonNull String key, Object object) {
        Map<String, Object> properties = getProperties();
        if (object != null) properties.put(key, object);
        else properties.remove(key);
        updateObject(1, properties);
    }

    @Override
    public void unsetProperty(@NonNull String key) {
        setProperty(key, (Object) null);
    }

    @Override
    public void setProperties(@NonNull Map<String, Object> properties) {
        updateObject(1, properties);
    }

    @Override
    public void setProperty(@NonNull String key, @NonNull String value) {
        setProperty(key, (Object) value);
    }

    @Override
    public void setProperty(@NonNull String key, int value) {
        setProperty(key, (Object) value);
    }

    @Override
    public void setProperty(@NonNull String key, long value) {
        setProperty(key, (Object) value);
    }

    @Override
    public void setProperty(@NonNull String key, double value) {
        setProperty(key, (Object) value);
    }

    @Override
    public void setProperty(@NonNull String key, float value) {
        setProperty(key, (Object) value);
    }

    @Override
    public void setProperty(@NonNull String key, boolean value) {
        setProperty(key, (Object) value);
    }
}
