package com.uroria.backend.impl.user;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.uroria.backend.Backend;
import com.uroria.backend.Deletable;
import com.uroria.backend.clan.Clan;
import com.uroria.backend.impl.communication.CommunicationWrapper;
import com.uroria.backend.impl.stats.StatsManager;
import com.uroria.backend.impl.wrapper.Wrapper;
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
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class UserWrapper extends Wrapper implements User {
    private final UserManager userManager;
    private final CommunicationWrapper object;
    private final StatsManager statsManager;
    private final UUID uuid;
    private final ObjectSet<Permission> permissions;
    private boolean deleted;

    public UserWrapper(UserManager userManager, @NonNull UUID uuid, StatsManager statsManager) {
        this.userManager = userManager;
        this.statsManager = statsManager;
        this.object = new CommunicationWrapper(uuid.toString(), userManager.getClient());
        this.uuid = uuid;
        this.permissions = new ObjectArraySet<>();
    }

    @Override
    public void refresh() {
        refreshPermissions();
    }

    @Override
    public JsonObject getObject() {
        return this.object.getObject();
    }

    @Override
    public CommunicationWrapper getObjectWrapper() {
        return this.object;
    }

    @Override
    public String getIdentifierKey() {
        return "uuid";
    }

    @Override
    public String getStringIdentifier() {
        return this.uuid.toString();
    }

    @Override
    public void delete() {
        if (isDeleted()) return;
        this.deleted = true;
        object.set("deleted", true);
        this.userManager.getDelete().deleteSync("uuid", uuid.toString());
    }

    @Override
    public boolean isDeleted() {
        if (this.deleted) return true;
        boolean val = getBoolean("deleted");
        this.deleted = val;
        return val;
    }

    @Override
    public List<PermGroup> getPermGroups() {
        return new ObjectArrayList<>();
    }

    @SuppressWarnings("WeakWarningMarkers")
    @Override
    public Permission getPermission(String node) {
        Deletable.checkDeleted(this);
        Permission savedPermission = getRootPermission(node);
        if (savedPermission != null) return savedPermission;

        Permission groups = getPermGroups().stream().filter(group -> {
                    Permission permission = group.getPermission(node);
                    return permission.getState() != PermState.NOT_SET;
                }).max(Comparator.comparing(PermGroup::getPriority))
                .map(group -> group.getPermission(node))
                .orElse(null);

        if (groups != null) {
            this.permissions.add(groups);
            return groups;
        }

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

        Permission impl = getImpl(node, PermState.NOT_SET);
        this.permissions.add(impl);
        return impl;
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

    private void setPermission(String node, boolean value) {
        this.permissions.removeIf(perm -> perm.getNode().equals(node));
        PermState state;
        if (value) state = PermState.TRUE;
        else state = PermState.FALSE;
        this.permissions.add(getImpl(node, state));
        if (value) {
            List<String> allowed = getStringArray("allowed");
            allowed.removeIf(someNode -> someNode.equals(node));
            allowed.add(node);
            setStringArray(allowed, "allowed");
            return;
        }
        List<String> disallowed = getStringArray("disallowed");
        disallowed.removeIf(someNode -> someNode.equals(node));
        disallowed.add(node);
        setStringArray(disallowed, "disallowed");
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
        List<String> allowed = getStringArray("allowed");
        List<String> disallowed = getStringArray("disallowed");
        allowed.remove(node);
        disallowed.remove(node);
        setStringArray(allowed, "allowed");
        setStringArray(disallowed, "disallowed");
    }

    private List<String> getRawAllowed() {
        return getStringArray(".allowed");
    }

    private List<String> getRawDisallowed() {
        return getStringArray(".disallowed");
    }

    private List<String> getStringArray(String key) {
        Result<JsonElement> result = this.object.get(key);
        JsonElement element = result.get();
        if (element == null) return ObjectLists.emptyList();
        JsonArray stringArray = element.getAsJsonArray();
        return stringArray.asList().stream()
                .map(JsonElement::getAsString)
                .toList();
    }

    private void setStringArray(List<String> list, String key) {
        JsonArray array = new JsonArray();
        list.forEach(array::add);
        this.object.set(key, array);
    }

    private Object2BooleanMap<String> getRawPermissions() {
        Object2BooleanMap<String> map = new Object2BooleanArrayMap<>();
        List<String> allowed = getRawAllowed();
        List<String> disallowed = getRawDisallowed();
        allowed.forEach(string -> map.put(string, true));
        disallowed.forEach(string -> map.put(string, false));
        return map;
    }

    private Permission getRootPermission(String node) {
        final String finalNode = node.toLowerCase();
        return this.permissions.stream()
                .filter(perm -> perm.getNode().equals(finalNode))
                .findAny()
                .orElse(null);
    }

    @Override
    public void refreshPermissions() {
        Deletable.checkDeleted(this);
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

    @Override
    public void addStat(int gameId, @NonNull String scoreKey, float value) {
        this.statsManager.addStat(this.uuid, gameId, scoreKey, value);
    }

    @Override
    public void addStat(int gameId, @NonNull String scoreKey, int value) {
        this.statsManager.addStat(this.uuid, gameId, scoreKey, value);
    }

    @Override
    public List<Stat> getStatsWithScoreGreaterThanValue(int gameId, @NonNull String scoreKey, int value) {
        return this.statsManager.getStatsWithScoreGreaterThanValue(this.uuid, gameId, scoreKey, value);
    }

    @Override
    public List<Stat> getStatsWithScoreLowerThanValue(int gameId, @NonNull String scoreKey, int value) {
        return this.statsManager.getStatsWithScoreLowerThanValue(this.uuid, gameId, scoreKey, value);
    }

    @Override
    public List<Stat> getStatsWithScore(int gameId, @NonNull String scoreKey, int value) {
        return this.statsManager.getStatsWithScore(this.uuid, gameId, scoreKey, value);
    }

    @Override
    public List<Stat> getStatsWithScoreGreaterThanValue(int gameId, @NonNull String scoreKey, float value) {
        return this.statsManager.getStatsWithScoreGreaterThanValue(this.uuid, gameId, scoreKey, value);
    }

    @Override
    public List<Stat> getStatsWithScoreLowerThanValue(int gameId, @NonNull String scoreKey, float value) {
        return this.statsManager.getStatsWithScoreLowerThanValue(this.uuid, gameId, scoreKey, value);
    }

    @Override
    public List<Stat> getStatsWithScore(int gameId, @NonNull String scoreKey, float value) {
        return this.statsManager.getStatsWithScore(this.uuid, gameId, scoreKey, value);
    }

    @Override
    public List<Stat> getStats(int gameId) {
        return this.statsManager.getStats(this.uuid, gameId);
    }

    @Override
    public List<Stat> getStatsInTimeRangeOf(int gameId, long startMs, long endMs) {
        return this.statsManager.getStatsInTimeRangeOf(this.uuid, gameId, startMs, endMs);
    }

    @Override
    public UUID getUniqueId() {
        return this.uuid;
    }

    @Override
    public Optional<Proxy> getConnectedProxy() {
        long identifier = getLong("connectedProxy", -1);
        if (identifier == -1) return Optional.empty();
        return Optional.ofNullable(Backend.getProxy(identifier).get());
    }

    @Override
    public Optional<ServerGroup> getConnectedServerGroup() {
        String serverGroup = getString("connectedServerGroup", null);
        if (serverGroup == null) return Optional.empty();
        return Optional.ofNullable(Backend.getServerGroup(serverGroup).get());
    }

    @Override
    public Optional<Server> getConnectedServer() {
        long identifier = getLong("connectedServer", -1);
        if (identifier == -1) return Optional.empty();
        return Optional.ofNullable(Backend.getServer(identifier).get());
    }

    @Override
    public Optional<Long> getDiscordUserId() {
        long id = getLong("discordUserId", 0);
        if (id == 0) return Optional.empty();
        return Optional.of(id);
    }

    @Override
    public void setDiscordUserId(long id) {
        this.object.set("discordUserId", id);
    }

    @Override
    public @NotNull String getUsername() {
        String name = getString("username", null);
        if (name == null) return "N/A";
        return name;
    }

    @Override
    public @NotNull Language getLanguage() {
        Result<JsonElement> result = object.get("lang");
        JsonElement element = result.get();
        if (element == null) return Language.DEFAULT;
        return Language.fromTag(element.getAsString());
    }

    @Override
    public boolean isOnline() {
        return getBoolean("onlineStatus");
    }

    @Override
    public UserStatus getStatus() {
        if (!isOnline()) return UserStatus.INVISIBLE;
        return getRealStatus();
    }

    @Override
    public UserStatus getRealStatus() {
        Result<JsonElement> result = object.get("status");
        JsonElement element = result.get();
        if (element == null) return UserStatus.INVISIBLE;
        return UserStatus.fromCode(element.getAsInt());
    }

    @Override
    public long getPlaytime() {
        return getLong("playtime", 0);
    }

    @Override
    public void setPlaytime(long playtime) {
        this.object.set("playtime", playtime);
    }

    @Override
    public long getLastJoin() {
        return getLong("lastJoin", 0);
    }

    @Override
    public void setLastJoin(long lastJoin) {
        this.object.set("lastJoin", lastJoin);
    }

    @Override
    public long getFirstJoin() {
        return getLong("firstJoin", 0);
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
        Result<JsonElement> result = this.object.get("friends");
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
                .map(uuid -> Backend.getUser(uuid).get())
                .filter(Objects::nonNull)
                .toList();
    }

    private List<UUID> getRawFriendRequests() {
        Result<JsonElement> result = this.object.get("friendRequests");
        JsonElement element = result.get();
        if (element == null) return ObjectLists.emptyList();
        return element.getAsJsonArray().asList().stream()
                .map(el -> UUID.fromString(el.getAsString()))
                .toList();
    }

    @Override
    public void addFriendRequest(@NonNull User user) {
        Result<JsonElement> result = this.object.get("friendRequests");
        JsonElement element = result.get();
        if (element == null) return;
        JsonArray array = element.getAsJsonArray();
        array.add(user.getUniqueId().toString());
        this.object.set("friendRequests", element);
    }

    @Override
    public void removeFriendRequest(User user) {
        removeFriendRequest(user.getUniqueId());
    }

    @Override
    public void removeFriendRequest(UUID uuid) {
        Result<JsonElement> result = this.object.get("friendRequests");
        JsonElement element = result.get();
        if (element == null) return;
        JsonArray array = element.getAsJsonArray();
        for (JsonElement value : array) {
            if (!value.getAsString().equals(uuid.toString())) continue;
            array.remove(value);
            break;
        }
        this.object.set("friendRequests", element);
    }

    @Override
    public void addFriend(@NonNull User user) {
        Result<JsonElement> result = this.object.get("friends");
        JsonElement element = result.get();
        if (element == null) return;
        JsonArray array = element.getAsJsonArray();
        array.add(user.getUniqueId().toString());
        this.object.set("friends", element);
    }

    @Override
    public void removeFriend(User user) {
        removeFriend(user.getUniqueId());
    }

    @Override
    public void removeFriend(UUID uuid) {
        Result<JsonElement> result = this.object.get("friends");
        JsonElement element = result.get();
        if (element == null) return;
        JsonArray array = element.getAsJsonArray();
        for (JsonElement value : array) {
            if (!value.getAsString().equals(uuid.toString())) continue;
            array.remove(value);
            break;
        }
        this.object.set("friends", element);
    }

    @Override
    public Result<Clan> getClan() {
        Result<String> tag = getRawClan();
        if (tag.isPresent()) return Backend.getClan(tag.get());
        return Result.none();
    }

    public Result<String> getRawClan() {
        return getString("clan");
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
        this.object.set("clan", JsonNull.INSTANCE);
    }

    @Override
    public List<User> getCrew() {
        return getRawCrew().stream().map(uuid -> Backend.getUser(uuid).get())
                .filter(Objects::nonNull)
                .toList();
    }

    private List<UUID> getRawCrew() {
        Result<JsonElement> result = this.object.get("crew");
        JsonElement element = result.get();
        if (element == null) return ObjectLists.emptyList();
        JsonArray uuidArray = element.getAsJsonArray();
        return uuidArray.asList().stream()
                .map(el -> UUID.fromString(el.getAsString()))
                .toList();
    }

    @Override
    public void addCrewMember(@NonNull User user) {
        Result<JsonElement> result = this.object.get("crew");
        JsonElement element = result.get();
        if (element == null) return;
        JsonArray array = element.getAsJsonArray();
        array.add(user.getUniqueId().toString());
        this.object.set("crew", element);
    }

    @Override
    public void removeCrewMember(User user) {
        removeCrewMember(user.getUniqueId());
    }

    @Override
    public void removeCrewMember(UUID uuid) {
        Result<JsonElement> result = this.object.get("crew");
        JsonElement element = result.get();
        if (element == null) return;
        JsonArray array = element.getAsJsonArray();
        for (JsonElement value : array) {
            if (!value.getAsString().equals(uuid.toString())) continue;
            array.remove(value);
            break;
        }
        this.object.set("crew", element);
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
        return Object2ObjectMaps.emptyMap();
    }

    @Override
    public void unsetProperty(@NonNull String key) {
        this.object.set("property." + key, JsonNull.INSTANCE);
    }

    @Override
    public void setProperties(@NonNull Map<String, Object> properties) {
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Integer n) {
                setProperty("property." + key, n);
                return;
            }
            if (value instanceof Long n) {
                setProperty("property." + key, n);
                return;
            }
            if (value instanceof Double n) {
                setProperty("property." + key, n);
                return;
            }
            if (value instanceof Float n) {
                setProperty("property." + key, n);
                return;
            }
            if (value instanceof Boolean b) {
                setProperty("property." + key, b);
                return;
            }
            if (value instanceof String s) {
                setProperty("property." + key, s);
                return;
            }
        }
    }

    @Override
    public void setProperty(@NonNull String key, @NonNull String value) {
        this.object.set("property." + key, value);
    }

    @Override
    public void setProperty(@NonNull String key, int value) {
        this.object.set("property." + key, value);
    }

    @Override
    public void setProperty(@NonNull String key, long value) {
        this.object.set("property." + key, value);
    }

    @Override
    public void setProperty(@NonNull String key, double value) {
        this.object.set("property." + key, value);
    }

    @Override
    public void setProperty(@NonNull String key, float value) {
        this.object.set("property." + key, value);
    }

    @Override
    public void setProperty(@NonNull String key, boolean value) {
        this.object.set("property." + key, value);
    }

    @Override
    public String getPropertyStringOrElse(@NonNull String key, @Nullable String defValue) {
        JsonElement element = this.object.get("property." + key).get();
        if (element == null) return defValue;
        return element.getAsString();
    }

    @Override
    public int getPropertyIntOrElse(@NonNull String key, int defValue) {
        JsonElement element = this.object.get("property." + key).get();
        if (element == null) return defValue;
        return element.getAsInt();
    }

    @Override
    public long getPropertyLongOrElse(@NonNull String key, long defValue) {
        JsonElement element = this.object.get("property." + key).get();
        if (element == null) return defValue;
        return element.getAsLong();
    }

    @Override
    public double getPropertyDoubleOrElse(@NonNull String key, double defValue) {
        JsonElement element = this.object.get("property." + key).get();
        if (element == null) return defValue;
        return element.getAsDouble();
    }

    @Override
    public float getPropertyFloatOrElse(@NonNull String key, float defValue) {
        JsonElement element = this.object.get("property." + key).get();
        if (element == null) return defValue;
        return element.getAsFloat();
    }

    @Override
    public boolean getPropertyBooleanOrElse(@NonNull String key, boolean defValue) {
        JsonElement element = this.object.get("property." + key).get();
        if (element == null) return defValue;
        return element.getAsBoolean();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UserWrapper wrapper) {
            return wrapper.uuid.equals(this.uuid);
        }
        return false;
    }
}
