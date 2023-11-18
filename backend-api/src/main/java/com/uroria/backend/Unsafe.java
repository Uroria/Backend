package com.uroria.backend;

import com.uroria.annotations.markers.Error;
import com.uroria.annotations.markers.WeakWarning;

public final class Unsafe {
    private Unsafe() {}

    private static Backend instance;

    @Error(message = "You should never execute this method if you're not a BackendWrapper")
    public static void setInstance(Backend instance) {
        Unsafe.instance = instance;
    }

    @WeakWarning(message = "Not meant for regular use.")
    public static Backend getInstance() {
        if (instance == null) throw new IllegalStateException("Backend not initialized!");
        return instance;
    }
}
