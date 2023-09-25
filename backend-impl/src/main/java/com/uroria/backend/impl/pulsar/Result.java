package com.uroria.backend.impl.pulsar;

import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract sealed class Result<T> permits Result.Some, Result.None, Result.Error {

    public static <T> Result<T> of(Supplier<T> func) {
        try {
            T value = func.get();
            if (value == null) return new None<>();
            return new Some<>(value);
        } catch (Throwable throwable) {
            return new Error<>(throwable);
        }
    }

    public static <T> Result<T> of(@Nullable T value) {
        if (value == null) return none();
        return some(value);
    }

    public static <T> Result<T> error(@NonNull Throwable throwable) {
        return new Error<>(throwable);
    }

    public static <T> Result<T> some(@NonNull T value) {
        return new Some<>(value);
    }

    public static <T> Result<T> none() {
        return new None<>();
    }

    public @Nullable T get() {
        if (this instanceof Result.Error<T>) return null;
        if (this instanceof Result.None<T>) return null;
        return getValue();
    }

    public boolean isPresent() {
        if (this instanceof Result.Error<T>) return false;
        return !(this instanceof Result.None<T>);
    }

    public T handle(Function<T,T> some, Supplier<T> none, Consumer<Throwable> error) {
        if (this instanceof Result.Error<T> errorResult) {
            error.accept(errorResult.throwable);
            return none.get();
        }
        if (this instanceof Result.Some<T> someResult) {
            some.apply(someResult.value);
            return someResult.value;
        }
        if (this instanceof Result.None<T>) {
            return none.get();
        }
        return null;
    }

    protected abstract @Nullable T getValue();

    public static final class Some<T> extends Result<T> {
        private final T value;

        private Some(T value) {
            this.value = value;
        }

        @Override
        public T getValue() {
            return this.value;
        }
    }

    public static final class None<T> extends Result<T> {

        @Override
        protected T getValue() {
            return null;
        }
    }

    public static final class Error<T> extends Result<T> {
        @Getter
        private final Throwable throwable;

        private Error(Throwable throwable) {
            this.throwable = throwable;
        }

        public Throwable getError() {
            return this.throwable;
        }

        @Override
        protected @Nullable T getValue() {
            return null;
        }
    }
}
