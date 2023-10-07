package com.uroria.backend.impl.communication.request;

import com.uroria.backend.impl.communication.TopicHolder;
import com.uroria.problemo.result.Result;
import lombok.NonNull;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface RequestChannel extends TopicHolder, Closeable {

    Result<byte[]> requestSync(byte @NonNull [] data, int timeout);

    Result<byte[]> requestSync(@NonNull Supplier<byte @NonNull []> data, int timeout);

    CompletableFuture<Result<byte[]>> requestAsync(byte @NonNull [] data, int timeout);

    CompletableFuture<Result<byte[]>> requestAsync(@NonNull Supplier<byte @NonNull []> data, int timeout);

}
