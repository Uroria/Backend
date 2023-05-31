package com.uroria.backend.server.scheduler;

import com.uroria.backend.api.scheduler.AsyncTask;
import com.uroria.backend.server.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class BackendAsyncTask<T> implements AsyncTask<T> {
    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(0);
    private static final Logger LOGGER = LoggerFactory.getLogger(BackendAsyncTask.class);

    private final Function<Integer, ? extends T> function;
    private final long time;
    private final TimeUnit timeUnit;

    BackendAsyncTask(Supplier<? extends T> supplier) {
        this(ignored -> supplier.get(), 0, TimeUnit.MILLISECONDS);
    }

    BackendAsyncTask(Supplier<? extends T> supplier, long time, TimeUnit timeUnit) {
        this(ignored -> supplier.get(), time, timeUnit);
    }

    BackendAsyncTask(Function<Integer, ? extends T> function, long time, TimeUnit timeUnit) {
        this.function = function;
        this.time = time;
        this.timeUnit = timeUnit;
    }

    @Override
    public void run(Consumer<? super T> success) {
        run(success, this::onError);
    }

    @Override
    public void run(Consumer<? super T> success, Consumer<? super Throwable> error) {
        CompletableFuture<T> future = time == 0 ? submit() : submitLater(time, timeUnit);
        future.whenComplete((onSuccess, onError) -> {
            if (future.isCompletedExceptionally()) {
                error.accept(onError);
                return;
            }
            success.accept(onSuccess);
        });
    }

    @Override
    public void run(BiConsumer<? super T, ? super ScheduledFuture<?>> success) {
        run(success, (throwable, runnable) -> throwable.printStackTrace());
    }

    @Override
    public void run(BiConsumer<? super T, ? super ScheduledFuture<?>> success, BiConsumer<? super Throwable, ? super ScheduledFuture<?>> error) {
        submitTimer(success, error);
    }

    private CompletableFuture<T> submit() {
        CompletableFuture<T> future = new CompletableFuture<T>();
        processFuture(future, 1);
        return future;
    }

    private CompletableFuture<T> submitLater(long time, TimeUnit timeUnit) {
        CompletableFuture<T> future = new CompletableFuture<T>();
        EXECUTOR_SERVICE.schedule(() -> processFuture(future, 1), time, timeUnit);
        return future;
    }

    private void submitTimer(BiConsumer<? super T, ? super ScheduledFuture<?>> success, BiConsumer<? super Throwable, ? super ScheduledFuture<?>> error) {
        AtomicReference<ScheduledFuture<?>> scheduledFuture = new AtomicReference<ScheduledFuture<?>>(null);
        AtomicInteger counter = new AtomicInteger(1);
        scheduledFuture.set(EXECUTOR_SERVICE.scheduleAtFixedRate(() -> {
            CompletableFuture<T> future = new CompletableFuture<T>();
            processFuture(future, counter.getAndIncrement());
            future.whenComplete((result, throwable) -> {
                if (future.isCompletedExceptionally()) {
                    error.accept(throwable, scheduledFuture.get());
                    return;
                }
                success.accept(result, scheduledFuture.get());
            });
        }, time, time, timeUnit));
    }

    private void processFuture(CompletableFuture<? super T> future, Integer counter) {
        Pair<T, Throwable> result = execute(counter).join();
        if (result.getSecond() != null) future.completeExceptionally(result.getSecond());
        else future.complete(result.getFirst());
    }


    private ForkJoinTask<Pair<T, Throwable>> execute(Integer counter) {
        return ForkJoinPool.commonPool().submit(ForkJoinTask.adapt(() -> {
            try {
                return new Pair<>(this.function.apply(counter), null);
            } catch (Throwable throwable) {
                return new Pair<>(null, throwable);
            }
        }));
    }

    private void onError(Throwable throwable) {
        LOGGER.error("Unhandled Exception in KebabTask", throwable);
    }
}
