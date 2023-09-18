package com.uroria.backend.impl.pulsar;

import lombok.NonNull;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class PulsarChannel {
    private static final String PREFIX = "non-persistent://";
    private static final String TENANT = "uroria";
    private static final String PRODUCER = "/producer";
    private static final String CONSUMER = "/consumer";
    protected static final Logger LOGGER = LoggerFactory.getLogger("PulsarChannel");

    protected final PulsarClient client;
    protected final Producer<byte[]> producer;
    protected final Consumer<byte[]> consumer;
    protected final String name;
    protected final String topic;

    public PulsarChannel(@NonNull PulsarClient client, @NonNull String name, @NonNull String topic) {
        this.client = client;
        this.name = name;
        this.topic = topic;
        this.producer = buildProducer();
        this.consumer = buildConsumer();
    }

    public void close() throws PulsarClientException {
        this.producer.close();
        this.consumer.close();
    }

    public final Result<MessageId> send(byte[] data) throws RuntimeException {
        return Result.of(() -> {
            try {
                return this.producer.send(data);
            } catch (Exception exception) {
                throw new RuntimeException("Cannot send message", exception);
            }
        });
    }

    public final Result<Message<byte[]>> receive() throws RuntimeException {
        return Result.of(() -> {
            try {
                return this.consumer.receive(20, TimeUnit.SECONDS);
            } catch (Exception exception) {
                throw new RuntimeException("Cannot receive message", exception);
            }
        });
    }

    public final Result<Message<byte[]>> ack(Message<byte[]> message) {
        return Result.of(() -> {
            try {
                this.consumer.acknowledge(message);
                return message;
            } catch (Exception exception) {
                throw new RuntimeException("Cannot acknowledge message", exception);
            }
        });
    }

    public final Result<Message<byte[]>> nAck(Message<byte[]> message) {
        return Result.of(() -> {
            try {
                this.consumer.negativeAcknowledge(message);
                return message;
            } catch (Exception exception) {
                throw new RuntimeException("Cannot negative acknowledge message", exception);
            }
        });
    }

    private Producer<byte[]> buildProducer() throws RuntimeException {
        Producer<byte[]> producer;
        try {
            producer = this.client.newProducer()
                    .producerName(this.name)
                    .topic(PREFIX + TENANT + "/" + this.topic + PRODUCER)
                    .create();
        } catch (PulsarClientException exception) {
            throw new RuntimeException(exception);
        }
        return producer;
    }

    private Consumer<byte[]> buildConsumer() throws RuntimeException {
        Consumer<byte[]> consumer;
        try {
            consumer = this.client.newConsumer()
                    .consumerName(this.name)
                    .subscriptionName(this.name)
                    .ackTimeout(20, TimeUnit.SECONDS)
                    .negativeAckRedeliveryDelay(2, TimeUnit.MILLISECONDS)
                    .topic(PREFIX + TENANT + "/" + this.topic + CONSUMER)
                    .subscribe();
        } catch (PulsarClientException exception) {
            throw new RuntimeException(exception);
        }
        return consumer;
    }
}
