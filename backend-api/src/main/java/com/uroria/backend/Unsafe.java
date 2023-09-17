package com.uroria.backend;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Unsafe {
    private BackendWrapper instance;

    @Deprecated
    public void setInstance(BackendWrapper instance) {
        Unsafe.instance = instance;
    }

    public BackendWrapper getInstance() {
        if (instance == null) throw new IllegalStateException("Backend not initialized!");
        return instance;
    }
}
