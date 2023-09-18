package com.uroria.backend.impl.pulsar;

import com.uroria.base.io.InsaneByteArrayInputStream;
import com.uroria.base.io.InsaneByteArrayOutputStream;
import lombok.NonNull;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.PulsarClient;

import java.util.Random;
import java.util.function.Consumer;

public final class PulsarRequestChannel extends PulsarChannel {
    private static long lastId;

    public PulsarRequestChannel(@NonNull PulsarClient client, @NonNull String name, @NonNull String topic) {
        super(client, name, topic);
    }

    public Result<InsaneByteArrayInputStream> request(@NonNull Consumer<InsaneByteArrayOutputStream> key, long timeout) {
        final long id = System.currentTimeMillis() - new Random().nextLong();
        if (id == lastId) {
            return request(key, timeout);
        }
        lastId = id;
        try (InsaneByteArrayOutputStream output = new InsaneByteArrayOutputStream()) {
            output.writeLong(id);
            key.accept(output);
            output.close();
            Result<MessageId> result = send(output.toByteArray());
            if (result instanceof Result.Error<MessageId> error) {
                return Result.error(error.getThrowable());
            }
        } catch (Exception exception) {
            return Result.error(exception);
        }

        long start = System.currentTimeMillis();
        try {
            InsaneByteArrayInputStream value = null;
            while (true) {
                if ((System.currentTimeMillis() - start) > timeout) break;
                Result<Message<byte[]>> result = receive();
                if (result instanceof Result.Error<Message<byte[]>> error) {
                    return Result.error(error.getThrowable());
                }

                Message<byte[]> message = result.getValue();
                if (message == null) continue;

                try (InsaneByteArrayInputStream input = new InsaneByteArrayInputStream(message.getData())) {
                    long idResponse = input.readLong();
                    if (idResponse != id) {
                        nAck(message);
                        continue;
                    }
                    value = input;
                    break;
                } catch (Exception exception) {
                    return Result.error(exception);
                }
            }
            return Result.of(value);
        } catch (Exception exception) {
            return Result.error(exception);
        }
    }
}
