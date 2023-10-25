package com.uroria.backend.cache;

import com.uroria.backend.Deletable;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.UUID;

public abstract class Wrapper implements Deletable {
    protected final WrapperManager<? extends Wrapper> wrapperManager;
    protected final BackendObject<? extends Wrapper> object;

    protected Wrapper(WrapperManager<? extends Wrapper> wrapperManager) {
        this.wrapperManager = wrapperManager;
        this.object = new BackendObject<>(this);
    }

    protected boolean nullCheck(Object object) {
        if (object == null) {
            getLogger().warn("Some object seems null while doing null-check", new NullPointerException("object is null"));
            return false;
        }
        return true;
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