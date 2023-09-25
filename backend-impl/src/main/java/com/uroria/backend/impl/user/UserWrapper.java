package com.uroria.backend.impl.user;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.uroria.backend.Backend;
import com.uroria.backend.clan.Clan;
import com.uroria.backend.impl.pulsar.PulsarObject;
import com.uroria.backend.impl.pulsar.Result;
import com.uroria.backend.permission.PermGroup;
import com.uroria.backend.permission.Permission;
import com.uroria.backend.stats.Stat;
import com.uroria.backend.user.User;
import com.uroria.backend.user.punishment.Punishment;
import com.uroria.backend.user.punishment.mute.Mute;
import com.uroria.base.lang.Language;
import com.uroria.base.user.UserStatus;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class UserWrapper implements User {
    private final PulsarObject object;
    private final UUID uuid;
    private final String prefix;

    public UserWrapper(@NonNull PulsarObject object, @NonNull UUID uuid) {
        this.object = object;
        this.uuid = uuid;
        this.prefix = getPrefix();
    }

    public void clear() {
        this.object.remove("user." + this.uuid);
    }

    private String getPrefix() {
        return "user." + uuid + ".";
    }

    @Override
    public void delete() {
        object.set(prefix + "deleted", true);
    }

    @Override
    public boolean isDeleted() {
        Result<JsonElement> result = object.get(prefix + "deleted");
        JsonElement element = result.get();
        if (element == null) return false;
        return element.getAsBoolean();
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
    public UUID getUniqueId() {
        return this.uuid;
    }

    @Override
    public @NotNull String getUsername() {
        Result<JsonElement> result = object.get(prefix + "username");
        JsonElement element = result.get();
        if (element == null) return "N/A";
        return element.getAsString();
    }

    @Override
    public @NotNull Language getLanguage() {
        Result<JsonElement> result = object.get(prefix + "lang");
        JsonElement element = result.get();
        if (element == null) return Language.DEFAULT;
        return Language.fromTag(element.getAsString());
    }

    @Override
    public boolean isOnline() {
        Result<JsonElement> result = object.get(prefix + "onlineStatus");
        JsonElement element = result.get();
        if (element == null) return false;
        return element.getAsBoolean();
    }

    @Override
    public UserStatus getStatus() {
        if (!isOnline()) return UserStatus.INVISIBLE;
        return getRealStatus();
    }

    @Override
    public UserStatus getRealStatus() {
        Result<JsonElement> result = object.get(prefix + "status");
        JsonElement element = result.get();
        if (element == null) return UserStatus.INVISIBLE;
        return UserStatus.fromCode(element.getAsInt());
    }

    @Override
    public long getPlaytime() {
        Result<JsonElement> result = object.get(prefix + "playtime");
        JsonElement element = result.get();
        if (element == null) return 0;
        return element.getAsLong();
    }

    @Override
    public void setPlaytime(long playtime) {
        this.object.set(prefix + "playtime", playtime);
    }

    @Override
    public long getLastJoin() {
        Result<JsonElement> result = object.get(prefix + "lastJoin");
        JsonElement element = result.get();
        if (element == null) return 0;
        return element.getAsLong();
    }

    @Override
    public void setLastJoin(long lastJoin) {
        this.object.set(prefix + "lastJoin", lastJoin);
    }

    @Override
    public long getFirstJoin() {
        Result<JsonElement> result = object.get(prefix + "firstJoin");
        JsonElement element = result.get();
        if (element == null) return 0;
        return element.getAsLong();
    }

    public void setFirstJoin(long firstJoin) {
        this.object.set(prefix + "firstJoin", firstJoin);
    }

    @Override
    public void setStatus(@NonNull UserStatus status) {
        this.object.set(prefix + "status", status.toCode());
    }

    @Override
    public void setLanguage(@NonNull Language language) {
        this.object.set(prefix + "lang", language.toTag());
    }

    @Override
    public void setUsername(@NonNull String username) {
        this.object.set(prefix + "username", username);
    }

    @Override
    public List<User> getFriends() {
        return getRawFriends().stream()
                .map(uuid -> Backend.getUser(uuid).orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    private List<UUID> getRawFriends() {
        Result<JsonElement> result = this.object.get(prefix + "friends");
        JsonElement element = result.get();
        if (element == null) return ObjectLists.emptyList();
        JsonArray uuidArray = element.getAsJsonArray();
        return uuidArray.asList().stream()
                .map(el -> UUID.fromString(el.getAsString()))
                .toList();
    }

    @Override
    public List<User> getFriendRequests() {
        return getRawFriendRequests().stream()
                .map(uuid -> Backend.getUser(uuid).orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    private List<UUID> getRawFriendRequests() {
        Result<JsonElement> result = this.object.get(prefix + "friendRequests");
        JsonElement element = result.get();
        if (element == null) return ObjectLists.emptyList();
        JsonArray uuidArray = element.getAsJsonArray();
        return uuidArray.asList().stream()
                .map(el -> UUID.fromString(el.getAsString()))
                .toList();
    }

    @Override
    public void addFriendRequest(@NonNull User user) {
        Result<JsonElement> result = this.object.get(prefix + "friendRequests");
        JsonElement element = result.get();
        if (element == null) return;
        JsonArray array = element.getAsJsonArray();
        array.add(user.getUniqueId().toString());
        this.object.set(prefix + "friendRequests", element);
    }

    @Override
    public void removeFriendRequest(User user) {
        removeFriendRequest(user.getUniqueId());
    }

    @Override
    public void removeFriendRequest(UUID uuid) {
        Result<JsonElement> result = this.object.get(prefix + "friendRequests");
        JsonElement element = result.get();
        if (element == null) return;
        JsonArray array = element.getAsJsonArray();
        for (JsonElement value : array) {
            if (!value.getAsString().equals(uuid.toString())) continue;
            array.remove(value);
            break;
        }
        this.object.set(prefix + "friendRequests", element);
    }

    @Override
    public void addFriend(@NonNull User user) {
        Result<JsonElement> result = this.object.get(prefix + "friends");
        JsonElement element = result.get();
        if (element == null) return;
        JsonArray array = element.getAsJsonArray();
        array.add(user.getUniqueId().toString());
        this.object.set(prefix + "friends", element);
    }

    @Override
    public void removeFriend(User user) {
        removeFriend(user.getUniqueId());
    }

    @Override
    public void removeFriend(UUID uuid) {
        Result<JsonElement> result = this.object.get(prefix + "friends");
        JsonElement element = result.get();
        if (element == null) return;
        JsonArray array = element.getAsJsonArray();
        for (JsonElement value : array) {
            if (!value.getAsString().equals(uuid.toString())) continue;
            array.remove(value);
            break;
        }
        this.object.set(prefix + "friends", element);
    }

    @Override
    public Optional<Clan> getClan() {
        return getRawClan().flatMap(Backend::getClan);
    }

    public Optional<String> getRawClan() {
        Result<JsonElement> result = object.get(prefix + "clan");
        JsonElement element = result.get();
        if (element == null) return Optional.empty();
        return Optional.ofNullable(element.getAsString());
    }

    @Override
    public void joinClan(@NonNull Clan clan) {
        this.object.set(prefix + "clan", clan.getName());
        if (!clan.hasMember(this)) return;
        clan.addMember(this);
    }

    @Override
    public void leaveClan() {
        getClan().ifPresent(clan -> clan.removeMember(this));
        this.object.set(prefix + "clan", JsonNull.INSTANCE);
    }

    @Override
    public List<User> getCrew() {
        return getRawCrew().stream().map(uuid -> Backend.getUser(uuid).orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    private List<UUID> getRawCrew() {
        Result<JsonElement> result = this.object.get(prefix + "crew");
        JsonElement element = result.get();
        if (element == null) return ObjectLists.emptyList();
        JsonArray uuidArray = element.getAsJsonArray();
        return uuidArray.asList().stream()
                .map(el -> UUID.fromString(el.getAsString()))
                .toList();
    }

    @Override
    public void addCrewMember(@NonNull User user) {
        Result<JsonElement> result = this.object.get(prefix + "crew");
        JsonElement element = result.get();
        if (element == null) return;
        JsonArray array = element.getAsJsonArray();
        array.add(user.getUniqueId().toString());
        this.object.set(prefix + "crew", element);
    }

    @Override
    public void removeCrewMember(User user) {
        removeCrewMember(user.getUniqueId());
    }

    @Override
    public void removeCrewMember(UUID uuid) {
        Result<JsonElement> result = this.object.get(prefix + "crew");
        JsonElement element = result.get();
        if (element == null) return;
        JsonArray array = element.getAsJsonArray();
        for (JsonElement value : array) {
            if (!value.getAsString().equals(uuid.toString())) continue;
            array.remove(value);
            break;
        }
        this.object.set(prefix + "crew", element);
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
    public Map<String, Object> getProperties() {
        return null;
    }

    @Override
    public void unsetProperty(@NonNull String key) {

    }

    @Override
    public void setProperties(@NonNull Map<String, Object> properties) {

    }

    @Override
    public void setProperty(@NonNull String key, @NonNull String value) {

    }

    @Override
    public void setProperty(@NonNull String key, int value) {

    }

    @Override
    public void setProperty(@NonNull String key, long value) {

    }

    @Override
    public void setProperty(@NonNull String key, double value) {

    }

    @Override
    public void setProperty(@NonNull String key, float value) {

    }

    @Override
    public void setProperty(@NonNull String key, boolean value) {

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
    public boolean equals(Object obj) {
        if (obj instanceof UserWrapper wrapper) {
            return wrapper.uuid.equals(this.uuid);
        }
        return false;
    }
}
