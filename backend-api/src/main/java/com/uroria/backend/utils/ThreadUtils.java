package com.uroria.backend.utils;

import lombok.experimental.UtilityClass;

import java.util.concurrent.TimeUnit;

@UtilityClass
public class ThreadUtils {

    public void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ignored) {}
    }

    public void sleep(long val, TimeUnit timeUnit) {
        sleep(timeUnit.toMillis(val));
    }
}
