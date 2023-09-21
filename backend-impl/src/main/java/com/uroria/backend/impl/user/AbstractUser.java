package com.uroria.backend.impl.user;

import com.uroria.backend.impl.AbstractBackendObject;
import com.uroria.backend.user.User;
import com.uroria.base.lang.Language;
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
    protected final AbstractUserManager userManager;

    protected final UUID uuid;
    protected String username;
    protected Language language;
    protected int status;
    protected byte online;
    protected byte deleted;
    protected long lastJoin;
    protected long firstJoin;
    protected long playtime;
    protected String clan;
    protected ObjectList<UUID> friends;
    protected ObjectList<UUID> friendRequests;
    protected ObjectList<UUID> crew;
    protected Object2ObjectMap<String, Object> properties;

    AbstractUser(AbstractUserManager userManager, @NonNull UUID uuid) {
        this.userManager = userManager;
        this.uuid = uuid;
        this.status = -1;
        this.firstJoin = -1;
        this.online = -1;
        this.deleted = -1;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final Object getObject(int key, Object defVal) {
        switch (key) {
            case 1 -> {
                if (this.properties == null) {
                    Object object = this.userManager.getObject(this.uuid, key, defVal);
                    this.properties = (Object2ObjectMap<String, Object>) object;
                    return object;
                }
                return this.properties;
            }
            case 2 -> {
                if (this.friends == null) {
                    Object object = this.userManager.getObject(this.uuid, key, defVal);
                    this.friends = (ObjectList<UUID>) object;
                    return object;
                }
                return this.friends;
            }
            case 3 -> {
                if (this.friendRequests == null) {
                    Object object = this.userManager.getObject(this.uuid, key, defVal);
                    this.friendRequests = (ObjectList<UUID>) object;
                    return object;
                }
                return this.friendRequests;
            }
            case 4 -> {
                if (this.crew == null) {
                    Object object = this.userManager.getObject(this.uuid, key, defVal);
                    this.crew = (ObjectList<UUID>) object;
                    return object;
                }
                return this.crew;
            }
        }
        return defVal;
    }

    @Override
    public final String getString(int key, String defVal) {
        switch (key) {
            case 1 -> {
                if (this.username == null) {
                    String string = this.userManager.getString(this.uuid, key, defVal);
                    this.username = string;
                    return string;
                }
                return this.username;
            }
            case 2 -> {
                if (this.language == null) {
                    Language language = Language.fromTag(this.userManager.getString(this.uuid, key, defVal));
                    this.language = language;
                    return language.toTag();
                }
                return this.language.toTag();
            }
            case 3 -> {
                if (this.clan == null) {
                    String clan = this.userManager.getString(this.uuid, key, defVal);
                    this.clan = clan;
                    return clan;
                }
                return this.clan;
            }
        }
        return defVal;
    }

    @Override
    public final int getInt(int key, int defVal) {
        switch (key) {
            case 1 -> {
                if (this.status == -1) {
                    int status = this.userManager.getInt(this.uuid, key, defVal);
                    this.status = status;
                    return status;
                }
                return this.status;
            }
        }
        return defVal;
    }

    @Override
    public final long getLong(int key, int defVal) {
        switch (key) {
            case 1 -> {
                if (this.lastJoin == -1) {
                    long lastJoin = this.userManager.getLong(this.uuid, key, defVal);
                    this.lastJoin = lastJoin;
                    return lastJoin;
                }
                return this.lastJoin;
            }
            case 2 -> {
                if (this.playtime == -1) {
                    long playtime = this.userManager.getLong(this.uuid, key, defVal);
                    this.playtime = playtime;
                    return playtime;
                }
                return this.playtime;
            }
            case 3 -> {
                if (this.firstJoin == -1) {
                    long firstJoin = this.userManager.getLong(this.uuid, key, defVal);
                    this.firstJoin = firstJoin;
                    return firstJoin;
                }
                return this.firstJoin;
            }
        }
        return defVal;
    }

    @Override
    public final boolean getBoolean(int key, boolean defVal) {
        switch (key) {
            case 1 -> {
                if (this.deleted == -1) {
                    boolean deleted = this.userManager.getBoolean(this.uuid, key, defVal);
                    if (deleted) this.deleted = 1;
                    else this.deleted = 0;
                    return deleted;
                }
                return this.deleted == 1;
            }
            case 2 -> {
                if (this.online == -1) {
                    boolean online = this.userManager.getBoolean(this.uuid, key, defVal);
                    if (online) this.online = 1;
                    else this.online = 0;
                    return online;
                }
                return this.online == 1;
            }
        }
        return defVal;
    }

    @Override
    public final float getFloat(int key, float defVal) {
        return defVal;
    }

    @Override
    public final double getDouble(int key, double defVal) {
        return defVal;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void checkObject(int channel, @Nullable Object value) {
        switch (channel) {
            case 1 -> {
                if (this.properties == null) this.properties = new Object2ObjectArrayMap<>();
                if (value == null) {
                    this.properties.clear();
                    return;
                }
                CollectionUtils.overrideMap(this.properties, (Map<String, Object>) value);
            }
            case 2 -> {
                if (this.friends == null) this.friends = new ObjectArrayList<>();
                if (value == null) {
                    this.friends.clear();
                    return;
                }
                CollectionUtils.overrideCollection(this.friends, (List<UUID>) value);
            }
            case 3 -> {
                if (this.friendRequests == null) this.friendRequests = new ObjectArrayList<>();
                if (value == null) {
                    this.friendRequests.clear();
                    return;
                }
                CollectionUtils.overrideCollection(this.friendRequests, (List<UUID>) value);
            }
            case 4 -> {
                if (this.crew == null) this.crew = new ObjectArrayList<>();
                if (value == null) {
                    this.crew.clear();
                    return;
                }
                CollectionUtils.overrideCollection(this.crew, (List<UUID>) value);
            }
        }
    }

    @Override
    public final void checkString(int channel, @Nullable String value) {
        switch (channel) {
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
    public final void checkInt(int channel, int value) {
        switch (channel) {
            case 1 -> {
                this.status = value;
            }
        }
    }

    @Override
    public final void checkLong(int channel, long value) {
        switch (channel) {
            case 1 -> {
                this.lastJoin = value;
            }
            case 2 -> {
                this.playtime = value;
            }
            case 3 -> {
                this.firstJoin = value;
            }
        }
    }

    @Override
    public final void checkBoolean(int channel, boolean value) {
        switch (channel) {
            case 1 -> {
                if (value) this.deleted = 1;
                else this.deleted = 0;
            }
            case 2 -> {
                if (value) this.online = 1;
                else this.online = 0;
            }
        }
    }

    @Override
    public final void checkFloat(int channel, float value) {

    }

    @Override
    public final void checkDouble(int channel, double value) {

    }

    @Override
    public final void updateObject(int key, @Nullable Object value) {
        this.userManager.updateObject(this.uuid, key, value);
    }

    @Override
    public final void updateString(int key, @Nullable String value) {
        this.userManager.updateObject(this.uuid, key, value);
    }

    @Override
    public final void updateInt(int key, int value) {
        this.userManager.updateObject(this.uuid, key, value);
    }

    @Override
    public final void updateLong(int key, long value) {
        this.userManager.updateObject(this.uuid, key, value);
    }

    @Override
    public final void updateBoolean(int key, boolean value) {
        this.userManager.updateObject(this.uuid, key, value);
    }

    @Override
    public final void updateFloat(int key, float value) {
        this.userManager.updateObject(this.uuid, key, value);
    }

    @Override
    public final void updateDouble(int key, double value) {
        this.userManager.updateObject(this.uuid, key, value);
    }
}
