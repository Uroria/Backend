package com.uroria.backend.impl.friend;

import com.uroria.backend.impl.AbstractManager;
import com.uroria.backend.friends.BackendFriend;
import com.uroria.backend.friends.FriendManager;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractFriendManager extends AbstractManager implements FriendManager {
    protected final Collection<BackendFriend> friends;

    public AbstractFriendManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient, logger);
        this.friends = new CopyOnWriteArrayList<>();
    }

    abstract protected void checkFriend(BackendFriend friend);

    @Override
    public Optional<BackendFriend> getFriend(@NonNull UUID uuid) {
        return getFriend(uuid, 3000);
    }
}
