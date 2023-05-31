package com.uroria.backend.server.scheduler;

import com.uroria.backend.api.scheduler.Scheduler;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public final class BackendScheduler implements Scheduler {

    @Override
    public <T> BackendAsyncTask<T> runTask(Supplier<? extends T> action) {
        return new BackendAsyncTask<>(action);
    }

    @Override
    public <T> BackendAsyncTask<T> runTaskLater(Supplier<? extends T> action, long time, TimeUnit timeUnit) {
        return new BackendAsyncTask<>(action, time, timeUnit);
    }

    @Override
    public <T> BackendAsyncTask<T> runTaskTimer(Function<Integer, ? extends T> action, long time, TimeUnit timeUnit) {
        return new BackendAsyncTask<>(action, time, timeUnit);
    }
}
