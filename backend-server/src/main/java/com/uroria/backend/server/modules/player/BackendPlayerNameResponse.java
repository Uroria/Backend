package com.uroria.backend.server.modules.player;

import com.uroria.backend.player.BackendPlayer;
import com.uroria.backend.pulsar.PulsarResponse;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendPlayerNameResponse extends PulsarResponse<String, BackendPlayer> {
    private final BackendPlayerManager playerManager;

    public BackendPlayerNameResponse(PulsarClient pulsarClient, BackendPlayerManager playerManager) throws PulsarClientException {
        super(pulsarClient, "player:request:name", "player:response:name", "PlayerModule");
        this.playerManager = playerManager;
    }

    @Override
    protected BackendPlayer response(String key) {
        return this.playerManager.getPlayer(key).orElse(null);
    }
}
