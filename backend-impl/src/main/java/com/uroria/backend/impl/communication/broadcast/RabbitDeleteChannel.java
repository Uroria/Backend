package com.uroria.backend.impl.communication.broadcast;

import com.rabbitmq.client.Connection;
import com.uroria.backend.impl.io.BackendOutputStream;
import com.uroria.problemo.result.Result;
import lombok.NonNull;

import java.util.concurrent.CompletableFuture;

public class RabbitDeleteChannel extends RabbitUpdateChannel implements DeleteChannel {
    public RabbitDeleteChannel(@NonNull Connection connection, String topic) throws RuntimeException {
        super(connection, topic);
    }

    @Override
    public Result<Void> deleteSync(String key, String valueKey) {
        return this.updateSync(() -> {
           try {
               BackendOutputStream output = new BackendOutputStream();
               output.writeUTF(key);
               output.writeUTF(valueKey);
               output.close();
               return output.toByteArray();
           } catch (Exception exception) {
               throw new RuntimeException(exception);
           }
        });
    }

    @Override
    public CompletableFuture<Result<Void>> deleteAsync(String key, String valueKey) {
        return CompletableFuture.supplyAsync(() -> deleteSync(key, valueKey));
    }
}
