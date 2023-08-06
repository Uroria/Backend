package com.uroria.backend.impl.friend;

import com.uroria.backend.friend.FriendHolder;
import com.uroria.backend.impl.pulsar.PulsarRequest;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.UUID;

public final class FriendUUIDRequestChannel extends PulsarRequest<FriendHolder, UUID> {
    public FriendUUIDRequestChannel(@NonNull PulsarClient pulsarClient, @NonNull String name) throws PulsarClientException {
        super(pulsarClient, "friend:request", "friend:response", name, 5000);
    }
}
