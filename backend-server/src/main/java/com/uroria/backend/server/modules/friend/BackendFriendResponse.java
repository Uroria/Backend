package com.uroria.backend.server.modules.friend;

import com.uroria.backend.common.BackendFriend;
import com.uroria.backend.common.pulsar.PulsarResponse;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.UUID;

public final class BackendFriendResponse extends PulsarResponse<UUID, BackendFriend> {
    private final BackendFriendManager friendManager;

    public BackendFriendResponse(PulsarClient pulsarClient, BackendFriendManager friendManager) throws PulsarClientException {
        super(pulsarClient, "friend:request", "friend:response", "FriendModule");
        this.friendManager = friendManager;
    }

    @Override
    protected BackendFriend response(UUID key) {
        return this.friendManager.getFriend(key).orElse(null);
    }
}
