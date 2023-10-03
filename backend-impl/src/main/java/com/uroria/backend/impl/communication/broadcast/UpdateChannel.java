package com.uroria.backend.impl.communication.broadcast;

import com.uroria.backend.impl.communication.TopicHolder;
import com.uroria.problemo.result.Result;
import lombok.NonNull;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface UpdateChannel extends Closeable, TopicHolder {

    Result<Void> updateSync(byte @NonNull [] data);

    Result<Void> updateSync(@NonNull Supplier<byte[]> data);

    CompletableFuture<Result<Void>> updateAsync(byte @NonNull [] data);

    CompletableFuture<Result<Void>> updateAsync(@NonNull Supplier<byte[]> data);

    Result<byte[]> awaitUpdate();

    Result<byte[]> awaitUpdate(int timeoutMs);
}
