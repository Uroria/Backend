package com.uroria.backend.stats;

import com.uroria.backend.AbstractManager;
import com.uroria.backend.common.BackendStat;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class StatsManager extends AbstractManager {
    protected final Logger logger;
    protected final Collection<BackendStat> stats;
    public StatsManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient);
        this.logger = logger;
        this.stats = new CopyOnWriteArrayList<>();
    }

    @Override
    abstract protected void start(String identifier);

    @Override
    abstract protected void shutdown();
}
