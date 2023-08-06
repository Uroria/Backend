package com.uroria.backend.impl.friend;

import com.uroria.backend.friend.FriendHolder;
import com.uroria.backend.impl.pulsar.PulsarUpdate;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.function.Consumer;

public final class FriendUpdateChannel extends PulsarUpdate<FriendHolder> {
    private final Consumer<FriendHolder> friendHolderConsumer;

    public FriendUpdateChannel(@NonNull PulsarClient pulsarClient, @NonNull String name, Consumer<FriendHolder> friendHolderConsumer) throws PulsarClientException {
        super(pulsarClient, "friend:update", name);
        this.friendHolderConsumer = friendHolderConsumer;
    }

    @Override
    protected void onUpdate(FriendHolder friend) {
        this.friendHolderConsumer.accept(friend);
    }
}
