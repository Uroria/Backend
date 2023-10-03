package com.uroria.backend.service.modules;

import com.uroria.backend.service.BackendServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BackendModule {
    protected final BackendServer server;
    protected final Logger logger;
    protected final String moduleName;

    public BackendModule(BackendServer server, String moduleName) {
        this.server = server;
        this.logger = LoggerFactory.getLogger(moduleName);
        this.moduleName = moduleName;
    }

    protected void enable() throws Exception {

    }

    protected void disable() throws Exception {

    }

    public final void start() {
        try {
            this.logger.info("Enabling module " + moduleName);
            disable();
        } catch (Exception exception) {
            this.logger.error("Unable to enable module " + moduleName, exception);
        }
    }

    public final void shutdown() {
        try {
            this.logger.info("Disabling module " + moduleName);
            disable();
        } catch (Exception exception) {
            this.logger.error("Unable to disable module " + moduleName, exception);
        }
    }

    public final Logger getLogger() {
        return logger;
    }

    public final String getModuleName() {
        return moduleName;
    }

    public final BackendServer getServer() {
        return server;
    }
}
