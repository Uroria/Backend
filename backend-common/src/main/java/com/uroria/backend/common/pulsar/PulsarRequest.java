package com.uroria.backend.common.pulsar;

import com.uroria.backend.common.utils.BackendInputStream;
import com.uroria.backend.common.utils.BackendOutputStream;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public abstract class PulsarRequest<O, K> {
    protected static final Logger LOGGER = LoggerFactory.getLogger("PulsarRequest");

    private final Producer<byte[]> producer;
    private final Consumer<byte[]> consumer;
    protected final String bridgeName;
    protected final int timeout;

    public PulsarRequest(PulsarClient pulsarClient, String requestTopic, String responseTopic, String bridgeName, int timeout, int nackRedelivery) throws PulsarClientException {
        this.producer = pulsarClient.newProducer()
                .producerName(bridgeName)
                .topic(requestTopic)
                .create();
        this.consumer = pulsarClient.newConsumer()
                .consumerName(bridgeName)
                .topic(responseTopic)
                .subscriptionName(bridgeName)
                .negativeAckRedeliveryDelay(nackRedelivery, TimeUnit.MILLISECONDS)
                .ackTimeout(timeout, TimeUnit.MILLISECONDS)
                .subscribe();
        this.bridgeName = bridgeName;
        this.timeout = timeout;
    }

    protected abstract void onRequest(K key);

    public final Optional<O> request(K requestKey) {
        if (requestKey == null) throw new NullPointerException("Key cannot be null");
        onRequest(requestKey);

        try (BackendOutputStream output = new BackendOutputStream()) {
            output.writeObject(requestKey);
            output.close();
            this.producer.send(output.toByteArray());
        } catch (Exception exception) {
            throw new RuntimeException("Unhandled exception in producing", exception);
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
                    if (input.readBoolean()) {
                        obj = (O) input.readObject();
                        break;
                    }
                } catch (Exception exception) {
                    throw new RuntimeException("Unhandled exception in consuming", exception);
                }
            }

            return Optional.ofNullable(obj);
        } catch (PulsarClientException exception) {
            throw new RuntimeException("Unhandled pulsar exception in consuming", exception);
        }
    }

    public final void close() throws PulsarClientException {
        if (this.consumer.isConnected()) this.consumer.close();
        if (this.producer.isConnected()) this.producer.close();
    }
}
