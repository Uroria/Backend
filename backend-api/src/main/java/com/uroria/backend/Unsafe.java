package com.uroria.backend;

import com.uroria.annotations.markers.Error;
import com.uroria.annotations.markers.WeakWarning;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Unsafe {
    private BackendWrapper instance;

    @Error(message = "You should never execute this method if you're not a BackendWrapper")
    public void setInstance(BackendWrapper instance) {
        Unsafe.instance = instance;
    }

    @WeakWarning(message = "Not meant for regular use.")
    public BackendWrapper getInstance() {
        if (instance == null) throw new IllegalStateException("Backend not initialized!");
        return instance;
    }
}
