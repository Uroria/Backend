package com.uroria.backend.impl.friend;

import com.uroria.backend.impl.AbstractManager;
import com.uroria.backend.friends.BackendFriend;
import com.uroria.backend.friends.FriendManager;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public abstract class AbstractFriendManager extends AbstractManager implements FriendManager {
    protected final Set<BackendFriend> friends;

    public AbstractFriendManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient, logger);
        this.friends = new ObjectArraySet<>();
    }

    abstract protected void checkFriend(BackendFriend friend);

    @Override
    public Optional<BackendFriend> getFriend(@NonNull UUID uuid) {
        return getFriend(uuid, 3000);
    }
}
