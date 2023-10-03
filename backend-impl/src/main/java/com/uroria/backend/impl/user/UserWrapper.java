package com.uroria.backend.impl.user;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.uroria.backend.Backend;
import com.uroria.backend.Deletable;
import com.uroria.backend.clan.Clan;
import com.uroria.backend.impl.communication.CommunicationClient;
import com.uroria.backend.impl.communication.CommunicationWrapper;
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

public final class UserWrapper implements User {
    private final CommunicationWrapper object;
    private final UUID uuid;
    private final ObjectSet<Permission> permissions;
    private boolean deleted;

    public UserWrapper(@NonNull CommunicationClient client, @NonNull UUID uuid) {
        this.object = new CommunicationWrapper(uuid.toString(), client);
        this.uuid = uuid;
        this.permissions = new ObjectArraySet<>();
    }

    public JsonObject getObject() {
        return this.object.getObject();
    }

    @Override
    public void delete() {
        if (isDeleted()) return;
        this.deleted = true;
        object.set("deleted", true);
    }

    @Override
    public boolean isDeleted() {
        if (this.deleted) return true;
        Result<JsonElement> result = object.get("deleted");
        JsonElement element = result.get();
        if (element == null) return false;
        boolean val = element.getAsBoolean();
        if (val) this.deleted = true;
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

    }

    @Override
    public void addStat(int gameId, @NonNull String scoreKey, int value) {

    }

    @Override
    public List<Stat> getStatsWithScoreGreaterThanValue(int gameId, @NonNull String scoreKey, int value) {
        return null;
    }

    @Override
    public List<Stat> getStatsWithScoreLowerThanValue(int gameId, @NonNull String scoreKey, int value) {
        return null;
    }

    @Override
    public List<Stat> getStatsWithScore(int gameId, @NonNull String scoreKey, int value) {
        return null;
    }

    @Override
    public List<Stat> getStatsWithScoreGreaterThanValue(int gameId, @NonNull String scoreKey, float value) {
        return null;
    }

    @Override
    public List<Stat> getStatsWithScoreLowerThanValue(int gameId, @NonNull String scoreKey, float value) {
        return null;
    }

    @Override
    public List<Stat> getStatsWithScore(int gameId, @NonNull String scoreKey, float value) {
        return null;
    }

    @Override
    public List<Stat> getStats(int gameId) {
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
    public @NotNull String getUsername() {
        Result<JsonElement> result = object.get("username");
        JsonElement element = result.get();
        if (element == null) return "N/A";
        return element.getAsString();
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
        Result<JsonElement> result = object.get("onlineStatus");
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
        Result<JsonElement> result = object.get("status");
        JsonElement element = result.get();
        if (element == null) return UserStatus.INVISIBLE;
        return UserStatus.fromCode(element.getAsInt());
    }

    @Override
    public long getPlaytime() {
        Result<JsonElement> result = object.get("playtime");
        JsonElement element = result.get();
        if (element == null) return 0;
        return element.getAsLong();
    }

    @Override
    public void setPlaytime(long playtime) {
        this.object.set("playtime", playtime);
    }

    @Override
    public long getLastJoin() {
        Result<JsonElement> result = object.get("lastJoin");
        JsonElement element = result.get();
        if (element == null) return 0;
        return element.getAsLong();
    }

    @Override
    public void setLastJoin(long lastJoin) {
        this.object.set("lastJoin", lastJoin);
    }

    @Override
    public long getFirstJoin() {
        Result<JsonElement> result = object.get("firstJoin");
        JsonElement element = result.get();
        if (element == null) return 0;
        return element.getAsLong();
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
        Result<JsonElement> result = object.get("clan");
        JsonElement element = result.get();
        if (element == null) return Result.none();
        return Result.of(element.getAsString());
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
