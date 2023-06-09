package com.uroria.backend.friend;

import com.uroria.backend.common.BackendFriend;
import com.uroria.backend.common.pulsar.PulsarRequest;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.UUID;

public final class BackendFriendRequest extends PulsarRequest<BackendFriend, UUID> {
    public BackendFriendRequest(PulsarClient pulsarClient, String bridgeName) throws PulsarClientException {
        super(pulsarClient, "friend:request", "friend:response", bridgeName, 3000, 5);
    }
}
