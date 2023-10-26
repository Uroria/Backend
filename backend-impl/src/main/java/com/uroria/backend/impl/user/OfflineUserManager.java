package com.uroria.backend.impl.user;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import java.util.UUID;

public final class OfflineUserManager {
    private final ObjectSet<OfflineUser> users;

    public OfflineUserManager() {
        this.users = new ObjectArraySet<>();
    }

    public OfflineUser getUser(UUID uuid) {
        if (uuid == null) return null;
        for (OfflineUser user : this.users) {
            if (user.getUniqueId().equals(uuid)) return user;
        }
        OfflineUser user = new OfflineUser(uuid);
        users.add(user);
        return user;
    }

    public OfflineUser getUser(String username) {
        if (username == null) return null;
        username = username.toLowerCase();
        for (OfflineUser user : this.users) {
            if (user.getUsername().equals(username)) return user;
        }
        return null;
    }
}
