package com.uroria.backend.api.scheduler;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Scheduler {

    <T> AsyncTask<T> runTask(Supplier<? extends T> action);

    <T> AsyncTask<T> runTaskLater(Supplier<? extends T> action, long time, TimeUnit timeUnit);

    <T> AsyncTask<T> runTaskTimer(Function<Integer, ? extends T> action, long time, TimeUnit timeUnit);
}
