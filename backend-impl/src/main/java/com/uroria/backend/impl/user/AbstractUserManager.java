package com.uroria.backend.impl.user;

import com.uroria.backend.impl.AbstractManager;
import com.uroria.backend.user.User;
import com.uroria.backend.user.UserManager;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

public abstract class AbstractUserManager extends AbstractManager implements UserManager {
    protected final ObjectArraySet<User> users;

    public AbstractUserManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient, logger);
        this.users = new ObjectArraySet<>();
    }

    abstract protected void checkUser(@NonNull User user);
}
