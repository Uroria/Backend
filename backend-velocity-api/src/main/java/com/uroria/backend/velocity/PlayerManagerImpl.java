package com.uroria.backend.velocity;

import com.uroria.backend.common.utils.BackendInputStream;
import com.uroria.backend.common.utils.BackendOutputStream;
import com.uroria.backend.player.BackendPlayerUpdate;
import com.uroria.backend.player.PlayerManager;
import com.uroria.backend.common.BackendPlayer;
import com.uroria.backend.scheduler.BackendScheduler;
import com.uroria.backend.velocity.events.PlayerUpdateEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class PlayerManagerImpl extends PlayerManager {
    private final ProxyServer proxyServer;
    private final int keepAlive = BackendVelocityPlugin.getConfig().getOrSetDefault("cacheKeepAliveInMinutes.player", 20);
    private BackendPlayerUpdate playerUpdate;
    private Producer<byte[]> playerRequest;
    private Consumer<byte[]> playerResponse;
    PlayerManagerImpl(PulsarClient pulsarClient, Logger logger, ProxyServer proxyServer) {
        super(pulsarClient, logger);
        this.proxyServer = proxyServer;
    }

    @Override
    protected void start(String identifier) {
        runCacheChecker();
        try {
            this.playerUpdate = new BackendPlayerUpdate(this.pulsarClient, identifier, this.logger, this);
            this.playerRequest = this.pulsarClient.newProducer()
                    .producerName(identifier)
                    .topic("player:request")
                    .create();
            this.playerResponse = this.pulsarClient.newConsumer()
                    .consumerName(identifier)
                    .subscriptionName(identifier)
                    .topic("player:response")
                    .negativeAckRedeliveryDelay(500, TimeUnit.MILLISECONDS)
                    .subscribe();
        } catch (Exception exception) {
            this.logger.error("Cannot initialize handlers", exception);
            BackendAPI.captureException(exception);
        }
    }

    @Override
    protected void shutdown() {
        try {
            if (this.playerUpdate != null) this.playerUpdate.close();
            if (this.playerRequest != null) this.playerRequest.close();
            if (this.playerResponse != null) this.playerResponse.close();
        } catch (Exception exception) {
            this.logger.error("Cannot close handlers", exception);
            BackendAPI.captureException(exception);
        }
    }

    @Override
    protected void checkPlayer(BackendPlayer player) {
        if (this.players.stream().noneMatch(player1 -> player1.getUUID().equals(player.getUUID()))) return;
        this.players.removeIf(player1 -> player1.getUUID().equals(player.getUUID()));
        this.players.add(player);
        this.proxyServer.getEventManager().fireAndForget(new PlayerUpdateEvent(player));
    }

    @Override
    public Optional<BackendPlayer> getPlayer(UUID uuid, int timeout) {
        if (uuid == null) throw new NullPointerException("UUID cannot be null");
        for (BackendPlayer player : this.players) {
            if (player.getUUID().equals(uuid)) return Optional.of(player);
        }

        BackendPlayer player = null;
        try {
            BackendOutputStream output = new BackendOutputStream();
            output.writePlayerRequest(uuid);
            output.close();
            this.playerRequest.send(output.toByteArray());

            long startTime = System.currentTimeMillis();
            while (true) {
                if ((System.currentTimeMillis() - startTime) > timeout) break;
                Message<byte[]> message = this.playerResponse.receive(timeout, TimeUnit.MILLISECONDS);
                if (message == null) continue;
                BackendInputStream input = new BackendInputStream(message.getData());
                if (!input.readBoolean()) {
                    this.playerResponse.negativeAcknowledge(message);
                    continue;
                }
                UUID responseUUID = input.readUUID();
                if (responseUUID == null || !responseUUID.equals(uuid)) {
                    this.playerResponse.negativeAcknowledge(message);
                    continue;
                }
                this.playerResponse.acknowledge(message);
                if (input.readBoolean()) {
                    player = input.readPlayer();
                    input.close();
                    break;
                }
                input.close();
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        return Optional.ofNullable(player);
    }

    @Override
    public Optional<BackendPlayer> getPlayer(String name, int timeout) {
        if (name == null) throw new NullPointerException("Name cannot be null");
        name = name.toLowerCase();
        for (BackendPlayer player : this.players) {
            if (player.getCurrentName().isPresent() && player.getCurrentName().get().equals(name)) return Optional.of(player);
        }

        BackendPlayer player = null;
        try {
            BackendOutputStream output = new BackendOutputStream();
            output.writePlayerRequest(name);
            output.close();
            this.playerRequest.send(output.toByteArray());

            long startTime = System.currentTimeMillis();
            while (true) {
                if ((System.currentTimeMillis() - startTime) > timeout) break;
                Message<byte[]> message = this.playerResponse.receive(timeout, TimeUnit.MILLISECONDS);
                if (message == null) continue;
                BackendInputStream input = new BackendInputStream(message.getData());
                if (input.readBoolean()) {
                    this.playerResponse.negativeAcknowledge(message);
                    continue;
                }
                String responseName = input.readUTF();
                if (!responseName.equals(name)) {
                    this.playerResponse.negativeAcknowledge(message);
                    continue;
                }
                this.playerResponse.acknowledge(message);
                if (input.readBoolean()) {
                    player = input.readPlayer();
                    input.close();
                    break;
                }
                input.close();
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        return Optional.ofNullable(player);
    }

    @Override
    public void updatePlayer(BackendPlayer player) {
        if (player == null) throw new NullPointerException("Player cannot be null");
        try {
            checkPlayer(player);
            this.playerUpdate.updatePlayer(player);
        } catch (Exception exception) {
            this.logger.error("Cannot update player", exception);
            BackendAPI.captureException(exception);
        }
    }

    private void runCacheChecker() {
        BackendScheduler.runTaskLater(() -> {
            Collection<UUID> markedForRemoval = new ArrayList<>();
            for (BackendPlayer player : this.players) {
                if (this.proxyServer.getPlayer(player.getUUID()).isEmpty()) markedForRemoval.add(player.getUUID());
            }
            return markedForRemoval;
        }, keepAlive, TimeUnit.MINUTES).run(markedForRemoval -> {
            for (UUID uuid : markedForRemoval) {
                this.players.removeIf(player -> player.getUUID().equals(uuid));
            }
            runCacheChecker();
        }, throwable -> {
            this.logger.error("Unhandled exception", throwable);
            BackendAPI.captureException(throwable);
            runCacheChecker();
        });
    }
}
