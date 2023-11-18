package com.uroria.backend.impl.user;

import com.uroria.backend.Backend;
import com.uroria.backend.clan.Clan;
import com.uroria.backend.permission.PermGroup;
import com.uroria.backend.permission.Permission;
import com.uroria.backend.proxy.Proxy;
import com.uroria.backend.server.Server;
import com.uroria.backend.server.ServerGroup;
import com.uroria.backend.stats.Stat;
import com.uroria.backend.user.User;
import com.uroria.backend.user.punishment.Punishment;
import com.uroria.backend.user.punishment.mute.Mute;
import com.uroria.base.lang.Language;
import com.uroria.base.user.UserStatus;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class OfflineUser implements User {
    private final UUID uuid;
    private final Collection<String> groups;
    private String username;
    private boolean deleted;

    public OfflineUser(UUID uuid) {
        this.uuid = uuid;
        this.groups = new ObjectArraySet<>();
    }

    @Override
    public void delete() {
        if (isDeleted()) return;
        this.deleted = true;
    }

    @Override
    public boolean isDeleted() {
        return this.deleted;
    }

    @Override
    public List<PermGroup> getPermGroups() {
        return this.groups.stream().map(name -> Backend.permissionGroup(name).get()).toList();
    }

    @Override
    public void addGroup(@NonNull PermGroup group) {

    }

    @Override
    public void removeGroup(PermGroup group) {

    }

    @Override
    public void removeGroup(String groupName) {

    }

    @Override
    public Permission getPermission(String node) {
        return null;
    }

    @Override
    public void refreshPermissions() {

    }

    @Override
    public ObjectSet<Permission> getSetPermissions() {
        return null;
    }

    @Override
    public void addStat(int gameId, @NonNull String scoreKey, float value) {

    }

    @Override
    public void addStat(int gameId, @NonNull String scoreKey, int value) {

    }

    @Override
    public List<Stat> getStatsWithScoreGreaterThanValue(int gameId, @NonNull String scoreKey, int value) {
        return ObjectLists.emptyList();
    }

    @Override
    public List<Stat> getStatsWithScoreLowerThanValue(int gameId, @NonNull String scoreKey, int value) {
        return ObjectLists.emptyList();
    }

    @Override
    public List<Stat> getStatsWithScore(int gameId, @NonNull String scoreKey, int value) {
        return ObjectLists.emptyList();
    }

    @Override
    public List<Stat> getStatsWithScoreGreaterThanValue(int gameId, @NonNull String scoreKey, float value) {
        return ObjectLists.emptyList();
    }

    @Override
    public List<Stat> getStatsWithScoreLowerThanValue(int gameId, @NonNull String scoreKey, float value) {
        return ObjectLists.emptyList();
    }

    @Override
    public List<Stat> getStatsWithScore(int gameId, @NonNull String scoreKey, float value) {
        return ObjectLists.emptyList();
    }

    @Override
    public List<Stat> getStats(int gameId) {
        return ObjectLists.emptyList();
    }

    @Override
    public List<Stat> getStatsInTimeRangeOf(int gameId, long startMs, long endMs) {
        return ObjectLists.emptyList();
    }

    @Override
    public UUID getUniqueId() {
        return this.uuid;
    }

    @Override
    public Optional<Proxy> getConnectedProxy() {
        return Optional.empty();
    }

    @Override
    public Optional<ServerGroup> getConnectedServerGroup() {
        return Optional.empty();
    }

    @Override
    public Optional<Server> getConnectedServer() {
        return Optional.empty();
    }

    @Override
    public Optional<Long> getDiscordUserId() {
        return Optional.empty();
    }

    @Override
    public void setDiscordUserId(long id) {

    }

    @Override
    public @NotNull String getUsername() {
        return this.username;
    }

    @Override
    public @NotNull String getRealUsername() {
        return getUsername();
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
    public void setPlaytime(long playtime) {

    }

    @Override
    public long getLastJoin() {
        return 0;
    }

    @Override
    public void setLastJoin(long lastJoin) {

    }

    @Override
    public long getFirstJoin() {
        return 0;
    }

    @Override
    public void setStatus(@NonNull UserStatus status) {

    }

    @Override
    public void setLanguage(@NonNull Language language) {

    }

    @Override
    public void setUsername(@NonNull String username) {
        this.username = username;
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
    public void addFriendRequest(@NonNull User user) {

    }

    @Override
    public void removeFriendRequest(User user) {

    }

    @Override
    public void removeFriendRequest(UUID uuid) {

    }

    @Override
    public void addFriend(@NonNull User user) {

    }

    @Override
    public void removeFriend(User user) {

    }

    @Override
    public void removeFriend(UUID uuid) {

    }

    @Override
    public Result<Clan> getClan() {
        return null;
    }

    @Override
    public void joinClan(@NonNull Clan clan) {

    }

    @Override
    public void leaveClan() {

    }

    @Override
    public List<User> getCrew() {
        return null;
    }

    @Override
    public void addCrewMember(@NonNull User user) {

    }

    @Override
    public void removeCrewMember(User user) {

    }

    @Override
    public void removeCrewMember(UUID uuid) {

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
    public Language getLanguage() {
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
}
