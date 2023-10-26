package com.uroria.backend.impl;

import com.uroria.are.Application;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UtilityClass
public class BackendInitializer {
    private final Logger logger = LoggerFactory.getLogger("Backend");
    private AbstractBackendWrapper wrapper;

    public AbstractBackendWrapper initialize() {
        if (wrapper != null) return wrapper;
        if (Application.isOffline()) wrapper = new OfflineBackendWrapper(logger);
        else wrapper = new BackendWrapperImpl(logger);
        return initialize();
    }
}
