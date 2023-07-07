package com.uroria.backend.server.modules;

import org.slf4j.Logger;

public abstract class AbstractManager {
    protected final Logger logger;
    private final String moduleName;
    public AbstractManager(Logger logger, String moduleName) {
        this.logger = logger;
        this.moduleName = moduleName;
    }

    public abstract void enable();

    public abstract void disable();

    public final void start() {
        this.logger.info("Starting " + moduleName + "...");
        this.enable();
    }

    public final void shutdown() {
        this.logger.info("Shutting down " + moduleName + "...");
        this.disable();
    }

    public final String getModuleName() {
        return this.moduleName;
    }
}
