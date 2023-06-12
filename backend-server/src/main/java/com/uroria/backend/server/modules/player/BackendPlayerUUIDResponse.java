package com.uroria.backend.server.modules.player;

import com.uroria.backend.common.BackendPlayer;
import com.uroria.backend.common.pulsar.PulsarResponse;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.UUID;

public final class BackendPlayerUUIDResponse extends PulsarResponse<UUID, BackendPlayer> {
    private final BackendPlayerManager playerManager;

    public BackendPlayerUUIDResponse(PulsarClient pulsarClient, BackendPlayerManager playerManager) throws PulsarClientException {
        super(pulsarClient, "player:request:uuid", "player:response:uuid", "PlayerModule");
        this.playerManager = playerManager;
    }

    @Override
    protected BackendPlayer response(UUID key) {
        LOGGER.debug("Requesting player by uuid " + key);
        return this.playerManager.getPlayer(key).orElse(null);
    }
}
