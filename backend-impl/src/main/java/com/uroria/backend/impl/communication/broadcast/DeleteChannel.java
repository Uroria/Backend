package com.uroria.backend.impl.communication.broadcast;

import com.uroria.problemo.result.Result;

import java.util.concurrent.CompletableFuture;

public interface DeleteChannel extends UpdateChannel {

    Result<Void> deleteSync(String key, String valueKey);

    CompletableFuture<Result<Void>> deleteAsync(String key, String valueKey);
}
