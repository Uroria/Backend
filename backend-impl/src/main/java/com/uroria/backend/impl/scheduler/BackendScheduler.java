package com.uroria.backend.impl.scheduler;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public final class BackendScheduler {
    public static  <T> BackendAsyncTask<T> runTask(Supplier<? extends T> action) {
        return new BackendAsyncTask<>(action);
    }

    public static <T> BackendAsyncTask<T> runTaskLater(Supplier<? extends T> action, long time, TimeUnit timeUnit) {
        return new BackendAsyncTask<>(action, time, timeUnit);
    }

    public static  <T> BackendAsyncTask<T> runTaskTimer(Function<Integer, ? extends T> action, long time, TimeUnit timeUnit) {
        return new BackendAsyncTask<>(action, time, timeUnit);
    }
}
