package com.uroria.backend.server.modules.friend;

import com.uroria.backend.common.BackendFriend;
import com.uroria.backend.common.pulsar.PulsarUpdate;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendFriendUpdate extends PulsarUpdate<BackendFriend> {
    private final BackendFriendManager friendManager;

    public BackendFriendUpdate(PulsarClient pulsarClient, BackendFriendManager friendManager) throws PulsarClientException {
        super(pulsarClient, "friend:update", "FriendModule");
        this.friendManager = friendManager;
    }

    @Override
    protected void onUpdate(BackendFriend friend) {
        LOGGER.debug("Updating friend with uuid " + friend.getHolder());
        this.friendManager.updateLocal(friend);
    }
}
