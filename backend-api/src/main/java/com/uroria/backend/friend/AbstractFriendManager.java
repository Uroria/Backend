package com.uroria.backend.friend;

import com.uroria.backend.AbstractManager;
import com.uroria.backend.common.friends.BackendFriend;
import com.uroria.backend.common.friends.FriendManager;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractFriendManager extends AbstractManager implements FriendManager {
    protected final Logger logger;
    protected final Collection<BackendFriend> friends;

    public AbstractFriendManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient);
        this.logger = logger;
        this.friends = new CopyOnWriteArrayList<>();
    }

    abstract protected void checkFriend(BackendFriend friend);

    @Override
    public Optional<BackendFriend> getFriend(@NonNull UUID uuid) {
        return getFriend(uuid, 3000);
    }
}
