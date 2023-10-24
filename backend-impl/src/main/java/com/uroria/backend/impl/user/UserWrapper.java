package com.uroria.backend.impl.user;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.uroria.backend.Backend;
import com.uroria.backend.Deletable;
import com.uroria.backend.cache.Wrapper;
import com.uroria.backend.cache.WrapperManager;
import com.uroria.backend.clan.Clan;
import com.uroria.backend.communication.Communicator;
import com.uroria.backend.impl.stats.StatsManager;
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
import com.uroria.base.permission.PermState;
import com.uroria.base.user.UserStatus;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class UserWrapper extends Wrapper implements User {
    private final StatsManager statsManager;
    private final UUID uuid;
    private final ObjectSet<Permission> permissions;
    private boolean deleted;

    public UserWrapper(WrapperManager<UserWrapper> wrapperManager, @NonNull UUID uuid, StatsManager statsManager) {
        super(wrapperManager);
        this.statsManager = statsManager;
        this.uuid = uuid;
        this.permissions = new ObjectArraySet<>();
    }


    @Override
    public void delete() {
        if (isDeleted()) return;
        this.deleted = true;
        this.object.set("deleted", true);
    }

    @Override
    public boolean isDeleted() {
        if (this.deleted) return true;
        boolean deleted = this.object.getBooleanOrElse("deleted", false);
        this.deleted = deleted;
        return deleted;
    }

    @Override
    public List<PermGroup> getPermGroups() {
        return this.object.getSet("groups", String.class).stream()
                .map(name -> Backend.getPermissionGroup(name).get())
                .filter(Objects::nonNull)
                .toList();
    }

    private void setPermission(String node, boolean value) {
        this.permissions.removeIf(perm -> perm.getNode().equals(node));
        PermState state;
        if (value) state = PermState.TRUE;
        else state = PermState.FALSE;
        this.permissions.add(getImpl(node, state));
        if (value) {
            ObjectSet<String> allowed = this.object.getSet("allowed", String.class);
            allowed.removeIf(someNode -> someNode.equals(node));
            allowed.add(node);
            this.object.set("allowed", allowed);
            return;
        }
        ObjectSet<String> disallowed = this.object.getSet("disallowed", String.class);
        disallowed.removeIf(someNode -> someNode.equals(node));
        disallowed.add(node);
        this.object.set("disallowed", disallowed);
    }

    private void setPermission(String node, PermState state) {
        node = node.toLowerCase();
        switch (state) {
            case TRUE -> setPermission(node, true);
            case FALSE -> setPermission(node, false);
            case NOT_SET -> unsetPermission(node);
        }
    }

    private void unsetPermission(String node) {
        ObjectSet<String> allowed = getRawAllowed();
        ObjectSet<String> disallowed = getRawDisallowed();
        allowed.remove(node);
        disallowed.remove(node);
        this.object.set("allowed", allowed);
        this.object.set("disallowed", disallowed);
    }

    @Override
    public @NotNull Permission getPermission(String node) {
        Permission permission = getRootPermission(node);
        if (permission != null) return permission;

        final String[] nodeParts = node.split("\\.");
        final StringBuilder currentNode = new StringBuilder();
        int i = 0;
        while (i <= nodeParts.length) {
            if (i > 0) currentNode.append(".");

            currentNode.append(nodeParts[i]);

            String current = currentNode.toString();

            Permission rootPermission = getRootPermission(current);
            if (rootPermission != null) {
                this.permissions.add(getImpl(node, rootPermission.getState()));
                return rootPermission;
            }

            String wildcardNode = current + ".*";

            Permission wildCardPermission = getRootPermission(wildcardNode);
            if (wildCardPermission != null) {
                this.permissions.add(getImpl(node, wildCardPermission.getState()));
                return wildCardPermission;
            }

            i++;
        }

        Permission rootPermission = getRootPermission("*");
        if (rootPermission != null) {
            return rootPermission;
        }

        return getImpl(node, PermState.NOT_SET);
    }

    private Permission getRootPermission(String node) {
        final String finalNode = node.toLowerCase();
        return this.permissions.stream()
                .filter(perm -> perm.getNode().equals(finalNode))
                .findAny()
                .orElse(null);
    }

    private Object2BooleanMap<String> getRawPermissions() {
        Object2BooleanMap<String> map = new Object2BooleanArrayMap<>();
        Collection<String> allowed = getRawAllowed();
        Collection<String> disallowed = getRawDisallowed();
        allowed.forEach(string -> map.put(string, true));
        disallowed.forEach(string -> map.put(string, false));
        return map;
    }

    private ObjectSet<String> getRawAllowed() {
        return object.getSet("allowed", String.class);
    }

    private ObjectSet<String> getRawDisallowed() {
        return object.getSet("disallowed", String.class);
    }

    @Override
    public synchronized void refreshPermissions() {
        Object2BooleanMap<String> raw = getRawPermissions();
        this.permissions.removeIf(perm -> !raw.containsKey(perm.getNode()));
        for (Permission perm : this.permissions) {
            String node = perm.getNode();
            boolean allowed = raw.getBoolean(node);
            if (perm.isGiven() == allowed) continue;
            this.permissions.remove(perm);
            PermState state;
            if (allowed) state = PermState.TRUE;
            else state = PermState.FALSE;
            this.permissions.add(getImpl(node, state));
        }
    }

    @Override
    public ObjectSet<Permission> getSetPermissions() {
        return this.permissions;
    }

    private Permission getImpl(final String node, final PermState finalState) {
        return new Permission() {
            private PermState state = finalState;

            @Override
            public void setState(@NonNull PermState state) {
                this.state = state;
                setPermission(node, state);
            }

            @Override
            public String getNode() {
                return node;
            }

            @Override
            public PermState getState() {
                return this.state;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Permission perm) {
                    return perm.getNode().equals(this.getNode());
                }
                return false;
            }
        };
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
        long identifier = this.object.getLongOrElse("connectedProxy", -1);
        if (identifier == -1) return Optional.empty();
        return Optional.ofNullable(Backend.getProxy(identifier).get());
    }

    @Override
    public Optional<ServerGroup> getConnectedServerGroup() {
        String serverGroup = this.object.getStringOrElse("connectedServerGroup", null);
        if (serverGroup == null) return Optional.empty();
        return Optional.ofNullable(Backend.getServerGroup(serverGroup).get());
    }

    @Override
    public Optional<Server> getConnectedServer() {
        long identifier = this.object.getLongOrElse("connectedServer", -1);
        if (identifier == -1) return Optional.empty();
        return Optional.ofNullable(Backend.getServer(identifier).get());
    }

    @Override
    public Optional<Long> getDiscordUserId() {
        long id = this.object.getLongOrElse("discordUserId", 0);
        if (id == 0) return Optional.empty();
        return Optional.of(id);
    }

    @Override
    public void setDiscordUserId(long id) {
        this.object.set("discordUserId", id);
    }

    @Override
    public @NotNull String getUsername() {
        return this.object.getStringOrElse("username", this.uuid.toString());
    }

    @Override
    public @NotNull Language getLanguage() {
        String lang = this.object.getStringOrElse("lang", null);
        if (lang == null) return Language.DEFAULT;
        return Language.fromTag(lang);
    }

    @Override
    public boolean isOnline() {
        return this.object.getBooleanOrElse("online", false);
    }

    @Override
    public UserStatus getStatus() {
        if (!isOnline()) return UserStatus.INVISIBLE;
        return getRealStatus();
    }

    @Override
    public UserStatus getRealStatus() {
        return UserStatus.fromCode(this.object.getIntOrElse("status", UserStatus.DEFAULT.toCode()));
    }

    @Override
    public long getPlaytime() {
        return this.object.getLongOrElse("playtime", 0);
    }

    @Override
    public void setPlaytime(long playtime) {
        this.object.set("playtime", playtime);
    }

    @Override
    public long getLastJoin() {
        return this.object.getLongOrElse("lastJoin", 0);
    }

    @Override
    public void setLastJoin(long lastJoin) {
        this.object.set("lastJoin", lastJoin);
    }

    @Override
    public long getFirstJoin() {
        return this.object.getLongOrElse("firstJoin", 0);
    }

    public void setFirstJoin(long firstJoin) {
        this.object.set("firstJoin", firstJoin);
    }

    @Override
    public void setStatus(@NonNull UserStatus status) {
        this.object.set("status", status.toCode());
    }

    @Override
    public void setLanguage(@NonNull Language language) {
        this.object.set("lang", language.toTag());
    }

    @Override
    public void setUsername(@NonNull String username) {
        this.object.set("username", username);
    }

    @Override
    public List<User> getFriends() {
        return getRawFriends().stream()
                .map(uuid -> Backend.getUser(uuid).get())
                .filter(Objects::nonNull)
                .toList();
    }

    private List<UUID> getRawFriends() {
        return this.object.getSet("friends", String.class).stream()
                .map(this::stringToUuid)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<User> getFriendRequests() {
        return getRawFriendRequests().stream()
                .map(uuid -> Backend.getUser(uuid).get())
                .filter(Objects::nonNull)
                .toList();
    }

    private List<UUID> getRawFriendRequests() {
        return this.object.getSet("friendRequests", String.class).stream()
                .map(this::stringToUuid)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public void addFriendRequest(@NonNull User user) {
        ObjectSet<String> friendRequests = this.object.getSet("friendRequests", String.class);
        friendRequests.add(user.getUniqueId().toString());
        this.object.set("friendRequests", friendRequests);
    }

    @Override
    public void removeFriendRequest(User user) {
        removeFriendRequest(user.getUniqueId());
    }

    @Override
    public void removeFriendRequest(UUID uuid) {
        ObjectSet<String> friendRequests = this.object.getSet("friendRequests", String.class);
        friendRequests.remove(uuid.toString());
        this.object.set("friendRequests", friendRequests);
    }

    @Override
    public void addFriend(@NonNull User user) {
        ObjectSet<String> friends = this.object.getSet("friends", String.class);
        friends.add(user.getUniqueId().toString());
        this.object.set("friends", friends);
    }

    @Override
    public void removeFriend(User user) {
        removeFriend(user.getUniqueId());
    }

    @Override
    public void removeFriend(UUID uuid) {
        ObjectSet<String> friends = this.object.getSet("friends", String.class);
        friends.remove(uuid.toString());
        this.object.set("friends", friends);
    }

    @Override
    public Result<Clan> getClan() {
        String tag = this.object.getStringOrElse("clan", null);
        if (tag != null) return Backend.getClan(tag);
        return Result.none();
    }

    @SuppressWarnings("SafetyWarnings")
    @Override
    public void joinClan(@NonNull Clan clan) {
        this.object.set("clan", clan.getName());
        if (clan.hasMember(this)) return;
        clan.addMember(this);
    }

    @SuppressWarnings("SafetyWarnings")
    @Override
    public void leaveClan() {
        getClan().ifPresent(clan -> clan.removeMember(this));
        this.object.unset("clan");
    }

    @Override
    public List<User> getCrew() {
        return getRawCrew().stream().map(uuid -> Backend.getUser(uuid).get())
                .filter(Objects::nonNull)
                .toList();
    }

    private List<UUID> getRawCrew() {
        return this.object.getSet("crew", String.class).stream()
                .map(this::stringToUuid)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public void addCrewMember(@NonNull User user) {
        ObjectSet<String> crew = this.object.getSet("crew", String.class);
        crew.add(user.getUniqueId().toString());
        this.object.set("crew", crew);
    }

    @Override
    public void removeCrewMember(User user) {
        removeCrewMember(user.getUniqueId());
    }

    @Override
    public void removeCrewMember(UUID uuid) {
        ObjectSet<String> crew = this.object.getSet("crew", String.class);
        crew.remove(uuid.toString());
        this.object.set("crew", crew);
    }

    @Override
    public List<Punishment> getActivePunishments() {
        return ObjectLists.emptyList();
    }

    @Override
    public List<Punishment> getExpiredPunishments() {
        return ObjectLists.emptyList();
    }

    @Override
    public List<Mute> getActiveMutes() {
        return ObjectLists.emptyList();
    }

    @Override
    public List<Mute> getExpiredMutes() {
        return ObjectLists.emptyList();
    }

    @Override
    public Map<String, Object> getProperties() {
        return this.object.getMap("properties", Object.class);
    }

    @Override
    public void unsetProperty(@NonNull String key) {
        Map<String, Object> properties = getProperties();
        properties.remove(key);
        this.object.set("properties", properties);
    }

    public void setProperty(String key, Object value) {
        Map<String, Object> properties = getProperties();
        properties.put(key, value);
        this.object.set("properties", properties);
    }

    @Override
    public void setProperties(@NonNull Map<String, Object> properties) {
        this.object.set("properties", new Object2ObjectArrayMap<>(properties));
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

    @Override
    public String getPropertyStringOrElse(@NonNull String key, @Nullable String defValue) {
        Object obj = getProperties().get(key);
        if (obj == null) return defValue;
        return (String) obj;
    }

    @Override
    public int getPropertyIntOrElse(@NonNull String key, int defValue) {
        Object obj = getProperties().get(key);
        if (obj == null) return defValue;
        return (int) obj;
    }

    @Override
    public long getPropertyLongOrElse(@NonNull String key, long defValue) {
        Object obj = getProperties().get(key);
        if (obj == null) return defValue;
        return (long) obj;
    }

    @Override
    public double getPropertyDoubleOrElse(@NonNull String key, double defValue) {
        Object obj = getProperties().get(key);
        if (obj == null) return defValue;
        return (double) obj;
    }

    @Override
    public float getPropertyFloatOrElse(@NonNull String key, float defValue) {
        Object obj = getProperties().get(key);
        if (obj == null) return defValue;
        return (float) obj;
    }

    @Override
    public boolean getPropertyBooleanOrElse(@NonNull String key, boolean defValue) {
        Object obj = getProperties().get(key);
        if (obj == null) return defValue;
        return (boolean) obj;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UserWrapper wrapper) {
            return wrapper.uuid.equals(this.uuid);
        }
        return false;
    }

    @Override
    public String getIdentifier() {
        return this.uuid.toString();
    }
}
