package com.uroria.backend.server.modules.stats;

import com.uroria.backend.common.BackendStat;
import com.uroria.backend.common.pulsar.PulsarUpdate;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendStatsUpdate extends PulsarUpdate<BackendStat> {
    private final BackendStatsManager statsManager;

    public BackendStatsUpdate(PulsarClient pulsarClient, BackendStatsManager statsManager) throws PulsarClientException {
        super(pulsarClient, "stat:update", "StatsModule");
        this.statsManager = statsManager;
    }

    @Override
    protected void onUpdate(BackendStat object) {
        this.statsManager.updateLocal(object);
    }
}
