package com.uroria.backend.server.modules.party;

import com.uroria.backend.party.PartyManager;
import com.uroria.backend.pluginapi.BackendRegistry;
import com.uroria.backend.pluginapi.events.party.PartyDeleteEvent;
import com.uroria.backend.pluginapi.events.party.PartyUpdateEvent;
import com.uroria.backend.party.BackendParty;
import com.uroria.backend.server.Uroria;
import com.uroria.backend.server.events.BackendEventManager;
import com.uroria.backend.server.modules.AbstractManager;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public final class BackendPartyManager extends AbstractManager implements PartyManager {
    private final PulsarClient pulsarClient;
    private final RedisCommands<String, String> cachedParties;
    private final BackendEventManager eventManager;

    public BackendPartyManager(Logger logger, PulsarClient pulsarClient, StatefulRedisConnection<String, String> cache) {
        super(logger, "PartyModule");
        this.pulsarClient = pulsarClient;
        this.cachedParties = cache.sync();
        this.eventManager = BackendRegistry.get(BackendEventManager.class).orElseThrow(() -> new NullPointerException("EventManager not initialized"));
    }

    @Override
    public void enable() {
        try {

        } catch (Exception exception) {
            this.logger.error("Cannot initialize handlers", exception);
        }
    }

    @Override
    public void disable() {
        try {

        } catch (Exception exception) {
            this.logger.error("Cannot close handlers", exception);
        }
    }

    @Override
    public Optional<BackendParty> getParty(@NotNull UUID operator) {
        try {
            BackendParty cachedPlayer = getCachedParty(operator);
            return Optional.ofNullable(cachedPlayer);
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            Uroria.captureException(exception);
            return Optional.empty();
        }
    }

    @Override
    public Optional<BackendParty> getParty(@NonNull UUID operator, int timeout) {
        return getParty(operator);
    }

    @Override
    public BackendParty updateParty(@NotNull BackendParty party) {
        try {
            this.cachedParties.del("party:" + party.getOperator());
            String json = Uroria.getGson().toJson(party);
            save(json);
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            Uroria.captureException(exception);
        }
        return party;
    }

    private void save(String json) {
        BackendParty party = Uroria.getGson().fromJson(json, BackendParty.class);
        String key = "party:" + party.getOperator();
        this.cachedParties.set(key, json, SetArgs.Builder.ex(Duration.ofDays(365)));
        if (party.isDeleted()) {
            PartyDeleteEvent partyDeleteEvent = new PartyDeleteEvent(party);
            this.eventManager.callEvent(partyDeleteEvent);
            return;
        }
        PartyUpdateEvent partyUpdateEvent = new PartyUpdateEvent(party);
        this.eventManager.callEvent(partyUpdateEvent);
    }

    private BackendParty getCachedParty(UUID member) {
        String cachedObject = this.cachedParties.get("party:" + member);
        if (cachedObject == null) return null;
        return Uroria.getGson().fromJson(cachedObject, BackendParty.class);
    }
}
