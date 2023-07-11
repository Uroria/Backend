package com.uroria.backend.common;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Unsafe {
    private BackendAPI api;

    public static void setAPI(BackendAPI newApi) {
        if (api != null) return;
        api = newApi;
    }

    public BackendAPI getAPI() {
        if (api == null) throw new IllegalStateException("API not initialized yet");
        return api;
    }
}
