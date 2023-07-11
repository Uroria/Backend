package com.uroria.backend.friend;

import com.uroria.backend.common.friends.BackendFriend;
import com.uroria.backend.common.pulsar.PulsarUpdate;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.function.Consumer;

public final class BackendFriendUpdate extends PulsarUpdate<BackendFriend> {
    private final Consumer<BackendFriend> friendConsumer;

    public BackendFriendUpdate(PulsarClient pulsarClient, String bridgeName, Consumer<BackendFriend> friendConsumer) throws PulsarClientException {
        super(pulsarClient, "friend:update", bridgeName);
        this.friendConsumer = friendConsumer;
    }

    @Override
    protected void onUpdate(BackendFriend object) {
        this.friendConsumer.accept(object);
    }
}
