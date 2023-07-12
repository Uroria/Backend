package com.uroria.backend.server.modules.player;

import com.uroria.backend.player.BackendPlayer;
import com.uroria.backend.pulsar.PulsarUpdate;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendPlayerUpdate extends PulsarUpdate<BackendPlayer> {
    private final BackendPlayerManager playerManager;

    public BackendPlayerUpdate(PulsarClient pulsarClient, BackendPlayerManager playerManager) throws PulsarClientException {
        super(pulsarClient, "player:update", "PlayerModule");
        this.playerManager = playerManager;
    }

    @Override
    protected void onUpdate(BackendPlayer player) {
        this.playerManager.updateLocal(player);
    }
}
