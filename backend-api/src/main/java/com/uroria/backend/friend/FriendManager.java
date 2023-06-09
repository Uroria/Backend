package com.uroria.backend.friend;

import com.uroria.backend.AbstractManager;
import com.uroria.backend.common.BackendFriend;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class FriendManager extends AbstractManager {
    protected final Logger logger;
    protected final Collection<BackendFriend> friends;

    public FriendManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient);
        this.logger = logger;
        this.friends = new CopyOnWriteArrayList<>();
    }

    @Override
    abstract protected void start(String identifier);

    @Override
    abstract protected void shutdown();

    abstract protected void checkFriend(BackendFriend friend);

    abstract public Optional<BackendFriend> getFriend(UUID uuid, int timeout);

    abstract public void updateFriend(BackendFriend friend);
}
