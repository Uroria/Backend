package com.uroria.backend.impl;

import com.uroria.backend.Backend;
import com.uroria.backend.Unsafe;
import com.uroria.base.event.EventManager;
import com.uroria.base.event.EventManagerFactory;
import lombok.NonNull;
import org.slf4j.Logger;

public abstract class AbstractBackendWrapper implements Backend {
    protected boolean started;
    protected final BackendEnvironment environment;
    protected final Logger logger;
    private final EventManager eventManager;

    @SuppressWarnings("ErrorMarkers")
    AbstractBackendWrapper(@NonNull Logger logger) {
        Unsafe.setInstance(this);
        this.logger = logger;
        this.eventManager = EventManagerFactory.create("BackendEvents");
        this.environment = new BackendEnvironment();
    }

    @Override
    public final BackendEnvironment getEnvironment() {
        return this.environment;
    }

    @Override
    public final EventManager getEventManager() {
        return this.eventManager;
    }

    abstract public void start() throws Exception;

    abstract public void shutdown() throws Exception;

    public final boolean isStarted() {
        return this.started;
    }

    public final Logger getLogger() {
        return this.logger;
    }
}
