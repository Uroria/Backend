package com.uroria.backend.impl.stats;

import com.uroria.backend.impl.AbstractManager;
import com.uroria.backend.stats.BackendStat;
import com.uroria.backend.stats.StatsManager;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

public abstract class AbstractStatsManager extends AbstractManager implements StatsManager {
    public AbstractStatsManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient, logger);
    }

    protected abstract void checkStat(BackendStat stat);
}
