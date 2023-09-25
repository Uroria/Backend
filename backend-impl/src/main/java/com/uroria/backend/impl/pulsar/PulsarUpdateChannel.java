package com.uroria.backend.impl.pulsar;

import com.google.gson.JsonElement;
import com.uroria.base.io.InsaneByteArrayInputStream;
import com.uroria.base.io.InsaneByteArrayOutputStream;
import lombok.NonNull;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class PulsarUpdateChannel extends PulsarChannel {

    public PulsarUpdateChannel(@NonNull PulsarClient client, CryptoKeyReader cryptoKeyReader, @NonNull String name, @NonNull String topic) {
        super(client, cryptoKeyReader, name, topic);
        UpdateThread updateThread = new UpdateThread(this);
        updateThread.start();
    }

    public final void update(@NonNull Consumer<InsaneByteArrayOutputStream> data) {
        try (InsaneByteArrayOutputStream output = new InsaneByteArrayOutputStream()) {
            data.accept(output);
            output.close();
            send(output.toByteArray());
        } catch (Exception exception) {
            LOGGER.error("Unable to update on " + topic, exception);
        }
    }

    public final void update(String key, JsonElement element) {
        update(output -> {
            try {
                output.writeUTF(key);
                output.writeUTF(element.getAsString());
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    public abstract void onUpdate(InsaneByteArrayInputStream input);

    private static final class UpdateThread extends Thread {
        private final PulsarUpdateChannel update;

        private UpdateThread(PulsarUpdateChannel update) {
            this.update = update;
        }

        @Override
        public void run() {
            while (!update.consumer.isConnected()) {
                try {
                    Result<Message<byte[]>> result = update.receive();
                    Message<byte[]> message = result.get();
                    if (message == null) continue;
                    CompletableFuture.runAsync(() -> {
                        try {
                            InsaneByteArrayInputStream input = new InsaneByteArrayInputStream(message.getData());
                            update.onUpdate(input);
                            input.close();
                        } catch (Exception exception) {
                            LOGGER.error("Unable to read message", exception);
                        }
                    });
                } catch (Exception exception) {
                    LOGGER.error("Unable to receive update", exception);
                }
            }
        }
    }
}
