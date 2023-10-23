package com.uroria.backend.cache;

import com.uroria.backend.Deletable;

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
}