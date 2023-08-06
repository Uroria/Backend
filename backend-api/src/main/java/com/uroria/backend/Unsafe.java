package com.uroria.backend;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Unsafe {
    private Backend instance;

    @Deprecated
    public void setInstance(Backend instance) {
        Unsafe.instance = instance;
    }

    public Backend getInstance() {
        if (instance == null) throw new IllegalStateException("Backend not initialized!");
        return instance;
    }
}
