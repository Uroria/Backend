package com.uroria.backend.cache;

import com.uroria.backend.Deletable;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.UUID;

public abstract class Wrapper implements Deletable {
    private final WrapperManager<? extends Wrapper> wrapperManager;
    protected final BackendObject<? extends Wrapper> object;

    protected Wrapper(WrapperManager<? extends Wrapper> wrapperManager) {
        this.wrapperManager = wrapperManager;
        this.object = new BackendObject<>(this);
    }

    public abstract String getIdentifier();

    public BackendObject<? extends Wrapper> getBackendObject() {
        return object;
    }

    WrapperManager<? extends Wrapper> getWrapperManager() {
        return wrapperManager;
    }

    protected @Nullable UUID stringToUuid(String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (Exception exception) {
            getLogger().error("Cannot convert string " + uuid + " to uuid", exception);
            return null;
        }
    }

    protected Logger getLogger() {
        return this.wrapperManager.logger;
    }
}