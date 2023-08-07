package com.uroria.backend.impl.pulsar;

import com.uroria.backend.utils.BackendInputStream;
import com.uroria.backend.utils.BackendOutputStream;
import lombok.NonNull;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public abstract class PulsarRequest<O, K> {
    protected static final Logger LOGGER = Pulsar.getLogger();

    private final Producer<byte[]> producer;
    private final Consumer<byte[]> consumer;
    protected final String name;
    protected final int timeout;

    public PulsarRequest(@NonNull PulsarClient pulsarClient, @NonNull String requestTopic, @NonNull String responseTopic, @NonNull String name, int timeout) throws PulsarClientException {
        this.producer = pulsarClient.newProducer()
                .producerName(name)
                .topic(requestTopic)
                .create();
        this.consumer = pulsarClient.newConsumer()
                .consumerName(name)
                .subscriptionName(name)
                .topic(responseTopic)
                .negativeAckRedeliveryDelay(5, TimeUnit.MILLISECONDS)
                .ackTimeout(30, TimeUnit.MINUTES)
                .subscribe();
        this.name = name;
        this.timeout = timeout;
    }

    public final Optional<O> request(@NonNull K requestKey) {
        return request(requestKey, this.timeout);
    }

    public final Optional<O> request(@NonNull K requestKey, int timeout) {
        try (BackendOutputStream output = new BackendOutputStream()) {
            output.writeObject(requestKey);
            output.close();
            this.producer.send(output.toByteArray());
        } catch (Exception exception) {
            throw new RuntimeException("Unhandled exception in output", exception);
        }

        long startTime = System.currentTimeMillis();
        try {
            O obj = null;
            while (true) {
                if ((System.currentTimeMillis() - startTime) > timeout) break;
                Message<byte[]> message = this.consumer.receive(timeout, TimeUnit.MILLISECONDS);
                if (message == null) continue;
                try (BackendInputStream input = new BackendInputStream(message.getData())) {
                    K responseKey = (K) input.readObject();
                    if (!requestKey.equals(responseKey)) {
                        this.consumer.negativeAcknowledge(message);
                        continue;
                    }
                    this.consumer.acknowledge(message);
                    if (input.readBoolean()) obj = (O) input.readObject();
                    break;
                } catch (Exception exception) {
                    throw new RuntimeException("Cannot receive message", exception);
                }
            }
            return Optional.ofNullable(obj);
        } catch (Exception exception) {
            throw new RuntimeException("Unhandled pulsar exception in consuming", exception);
        }
    }

    public final void close() throws PulsarClientException {
        if (this.consumer.isConnected()) this.consumer.close();
        if (this.producer.isConnected()) this.producer.close();
    }
}
