package com.uroria.backend.impl.pulsar;

import com.uroria.problemo.Problem;
import com.uroria.problemo.result.Result;
import lombok.NonNull;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.ConsumerBuilder;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.ProducerBuilder;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.jetbrains.annotations.Nullable;
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
    protected final CryptoKeyReader cryptoKeyReader;
    protected final Producer<byte[]> producer;
    protected final Consumer<byte[]> consumer;
    protected final String name;
    protected final String topic;

    public PulsarChannel(@NonNull PulsarClient client, @Nullable CryptoKeyReader cryptoKeyReader, @NonNull String name, @NonNull String topic) {
        this.client = client;
        this.cryptoKeyReader = cryptoKeyReader;
        this.name = name;
        this.topic = topic;
        this.producer = buildProducer();
        this.consumer = buildConsumer();
    }

    public void close() throws PulsarClientException {
        this.producer.close();
        this.consumer.close();
    }

    public final Result<MessageId> send(byte[] data) {
        try {
            return Result.some(this.producer.send(data));
        } catch (PulsarClientException.TimeoutException exception) {
            return Result.problem(Problem.unavailable("Connection timed out"));
        } catch (PulsarClientException.AlreadyClosedException exception) {
            return Result.problem(Problem.plain("Connection already closed"));
        } catch (PulsarClientException exception) {
            return Result.problem(Problem.unknown("Unknown pulsar exception."));
        }
    }

    public final Result<Message<byte[]>> receive() {
        try {
            return Result.of(this.consumer.receive(20, TimeUnit.SECONDS));
        } catch (PulsarClientException.AlreadyClosedException exception) {
            return Result.problem(Problem.plain("Connection already closed"));
        } catch (PulsarClientException exception) {
            return Result.problem(Problem.unknown("Unknown pulsar exception."));
        }
    }

    public final Result<Message<byte[]>> ack(Message<byte[]> message) {
        try {
            this.consumer.acknowledge(message);
            return Result.some(message);
        } catch (PulsarClientException.AlreadyClosedException exception) {
            return Result.problem(Problem.plain("Connection already closed"));
        } catch (PulsarClientException exception) {
            return Result.problem(Problem.unknown("Unknown pulsar exception."));
        }
    }

    public final Result<Message<byte[]>> nAck(Message<byte[]> message) {
        this.consumer.negativeAcknowledge(message);
        return Result.some(message);
    }

    private Producer<byte[]> buildProducer() throws RuntimeException {
        Producer<byte[]> producer;
        try {
            ProducerBuilder<byte[]> producerBuilder = this.client.newProducer();
            if (this.cryptoKeyReader != null) producerBuilder.cryptoKeyReader(this.cryptoKeyReader);
            producer = producerBuilder
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
            ConsumerBuilder<byte[]> consumerBuilder = this.client.newConsumer();
            if (this.cryptoKeyReader != null) consumerBuilder.cryptoKeyReader(this.cryptoKeyReader);
            consumer = consumerBuilder
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
