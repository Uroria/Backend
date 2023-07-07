package com.uroria.backend.pluginapi.scheduler;

import java.util.concurrent.ScheduledFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface AsyncTask<T> {
    void run(Consumer<? super T> success);

    void run(Consumer<? super T> success, Consumer<? super Throwable> error);

    void run(BiConsumer<? super T, ? super ScheduledFuture<?>> success);

    void run(BiConsumer<? super T, ? super ScheduledFuture<?>> success, BiConsumer<? super Throwable, ? super ScheduledFuture<?>> error);
}
