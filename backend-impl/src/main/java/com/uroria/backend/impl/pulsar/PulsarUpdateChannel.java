package com.uroria.backend.impl.pulsar;

import com.uroria.base.io.InsaneByteArrayInputStream;
import com.uroria.base.io.InsaneByteArrayOutputStream;
import lombok.NonNull;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class PulsarUpdateChannel extends PulsarChannel {
    private final UpdateThread updateThread;

    public PulsarUpdateChannel(@NonNull PulsarClient client, @NonNull String name, @NonNull String topic) {
        super(client, name, topic);
        this.updateThread = new UpdateThread(this);
        this.updateThread.start();
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
                            update.onUpdate(new InsaneByteArrayInputStream(message.getData()));
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
