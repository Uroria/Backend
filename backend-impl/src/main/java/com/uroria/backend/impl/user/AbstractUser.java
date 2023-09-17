package com.uroria.backend.impl.user;

import com.uroria.backend.clan.Clan;
import com.uroria.backend.impl.AbstractBackendObject;
import com.uroria.backend.user.User;
import com.uroria.base.lang.Language;
import com.uroria.base.user.UserStatus;
import com.uroria.base.utils.CollectionUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractUser extends AbstractBackendObject implements User {
    protected final UUID uuid;
    protected String username;
    protected Language language;
    protected int status;
    protected byte online;
    protected byte deleted;
    protected long lastJoin;
    protected final long firstJoin;
    protected long playtime;
    protected String clan;
    protected final ObjectList<UUID> friends;
    protected final ObjectList<UUID> friendRequests;
    protected final ObjectList<UUID> crew;
    protected final Object2ObjectMap<String, Object> properties;

    public AbstractUser(@NonNull UUID uuid, long firstJoin) {
        this.uuid = uuid;
        this.firstJoin = firstJoin;
        this.online = -1;
        this.deleted = -1;
        this.friends = new ObjectArrayList<>();
        this.friendRequests = new ObjectArrayList<>();
        this.crew = new ObjectArrayList<>();
        this.properties = new Object2ObjectArrayMap<>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void checkObject(int key, @Nullable Object value) {
        switch (key) {
            case 1 -> {
                if (value == null) {
                    this.properties.clear();
                    return;
                }
                CollectionUtils.overrideMap(this.properties, (Map<String, Object>) value);
            }
            case 2 -> {
                if (value == null) {
                    this.friends.clear();
                    return;
                }
                CollectionUtils.overrideCollection(this.friends, (List<UUID>) value);
            }
            case 3 -> {
                if (value == null) {
                    this.friendRequests.clear();
                    return;
                }
                CollectionUtils.overrideCollection(this.friendRequests, (List<UUID>) value);
            }
            case 4 -> {
                if (value == null) {
                    this.crew.clear();
                    return;
                }
                CollectionUtils.overrideCollection(this.crew, (List<UUID>) value);
            }
        }
    }

    @Override
    public void checkString(int key, @Nullable String value) {
        switch (key) {
            case 1 -> {
                if (value == null) {
                    this.username = "N/A";
                    return;
                }
                this.username = value;
            }
            case 2 -> {
                if (value == null) {
                    this.language = Language.DEFAULT;
                    return;
                }
                this.language = Language.fromTag(value);
            }
            case 3 -> {
                this.clan = value;
            }
        }
    }

    @Override
    public void checkInt(int key, int value) {
        switch (key) {
            case 1 -> {
                this.status = value;
            }
        }
    }

    @Override
    public void checkLong(int key, int value) {
        switch (key) {
            case 1 -> {
                this.lastJoin = value;
            }
            case 2 -> {
                this.playtime = value;
            }
        }
    }

    @Override
    public void checkBoolean(int key, boolean value) {
        switch (key) {
            case 1 -> {
                if (value) this.deleted = 1;
                else this.deleted = 0;
            }
            case 2 -> {
                if (value) this.online = 1;
                else this.deleted = 0;
            }
        }
    }

    @Override
    public void checkFloat(int key, float value) {

    }

    @Override
    public void checkDouble(int key, double value) {

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

    public abstract List<UUID> getUnsafeCrew();

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

    public abstract List<UUID> getUnsafeFriends();

    public abstract List<UUID> getUnsafeFriendRequests();

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
