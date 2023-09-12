package com.uroria.backend.impl.scheduler;

import com.uroria.base.scheduler.AsyncTask;
import com.uroria.base.scheduler.Scheduler;
import com.uroria.base.scheduler.SchedulerFactory;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

@UtilityClass
public final class BackendScheduler {
    private final Scheduler scheduler;

    static {
        scheduler = SchedulerFactory.create("Backend");
    }


    public <T> AsyncTask<T> runTask(@NonNull Supplier<? extends T> action) {
        return scheduler.runTask(action);
    }

    public <T> AsyncTask<T> runTaskLater(@NonNull Supplier<? extends T> action, long time, @NonNull TimeUnit timeUnit) {
        return scheduler.runTaskLater(action, time, timeUnit);
    }

    public <T> AsyncTask<T> runTaskTimer(@NonNull Function<Integer, ? extends T> action, long time, @NonNull TimeUnit timeUnit) {
        return scheduler.runTaskTimer(action, time, timeUnit);
    }
}
