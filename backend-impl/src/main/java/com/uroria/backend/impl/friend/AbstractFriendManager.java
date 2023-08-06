package com.uroria.backend.impl.friend;

import com.uroria.backend.friend.FriendHolder;
import com.uroria.backend.friend.FriendManager;
import com.uroria.backend.impl.AbstractManager;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

public abstract class AbstractFriendManager extends AbstractManager implements FriendManager {
    protected final ObjectArraySet<FriendHolder> holders;

    public AbstractFriendManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient, logger);
        this.holders = new ObjectArraySet<>();
    }

    abstract protected void checkFriend(@NonNull FriendHolder holder);
}
