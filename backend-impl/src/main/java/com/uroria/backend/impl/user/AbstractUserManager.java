package com.uroria.backend.impl.user;

import com.uroria.backend.impl.AbstractManager;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import java.util.Optional;
import java.util.UUID;

public abstract class AbstractUserManager extends AbstractManager {
    protected final ObjectSet<UserWrapper> users;

    public AbstractUserManager() {
        super();
        this.users = new ObjectArraySet<>();
    }

    public Optional<AbstractUser> getUser(UUID uuid) {
        if (uuid == null) return Optional.empty();
        for (AbstractUser user : this.users) {
            if (user.getUniqueId().equals(uuid)) return Optional.of(user);
        }


    }

    public Optional<AbstractUser> getUser(String username) {
        if (username == null) return Optional.empty();
        for (AbstractUser user : this.users) {
            if (user.getUsername().equals(username)) return Optional.of(user);
        }


    }



}
