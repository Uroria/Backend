package com.uroria.backend.impl.pulsar;

import com.uroria.base.io.InsaneByteArrayInputStream;
import com.uroria.base.io.InsaneByteArrayOutputStream;
import com.uroria.problemo.result.Result;
import lombok.NonNull;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public abstract class PulsarResponseChannel extends PulsarChannel {

    public PulsarResponseChannel(@NonNull PulsarClient client, @Nullable CryptoKeyReader cryptoKeyReader, @NonNull String name, @NonNull String topic) {
        super(client, cryptoKeyReader, name, topic);
        ResponseThread responseThread = new ResponseThread(this);
        responseThread.start();
    }

    public abstract void onRequest(InsaneByteArrayInputStream input, InsaneByteArrayOutputStream output);

    private static final class ResponseThread extends Thread {
        private final PulsarResponseChannel response;

        public ResponseThread(PulsarResponseChannel response) {
            this.response = response;
        }

        @Override
        public void run() {
            while (!response.consumer.isConnected()) {
                try {
                    Result<Message<byte[]>> result = response.receive();
                    Message<byte[]> message = result.get();
                    if (message == null) continue;
                    CompletableFuture.runAsync(() -> {
                        try {
                            InsaneByteArrayOutputStream output = new InsaneByteArrayOutputStream();
                            InsaneByteArrayInputStream input = new InsaneByteArrayInputStream(message.getData());
                            output.writeLong(input.readLong());
                            response.onRequest(input, output);
                            input.close();
                            output.close();
                            response.send(output.toByteArray());
                        } catch (Exception exception) {
                            LOGGER.error("Unable to response request", exception);
                        }
                    });
                } catch (Exception exception) {
                    LOGGER.error("Unable to receive request", exception);
                }
            }
        }
    }
}
