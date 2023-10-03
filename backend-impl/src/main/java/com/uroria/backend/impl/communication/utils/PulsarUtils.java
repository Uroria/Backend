package com.uroria.backend.impl.communication.utils;

import com.uroria.problemo.Problem;
import com.uroria.problemo.result.Result;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class PulsarUtils {

    public Result<Producer<byte[]>> buildProducer(@NonNull PulsarClient client, String topic) {
        Producer<byte[]> producer;
        try {
            String identifier = UUID.randomUUID().toString();
            producer = client.newProducer()
                    .topic(topic)
                    .producerName(identifier)
                    .create();
        } catch (PulsarClientException exception) {
            return Result.problem(Problem.error(exception));
        }
        return Result.some(producer);
    }

    public Result<Consumer<byte[]>> buildConsumer(@NonNull PulsarClient client, String topic) {
        Consumer<byte[]> consumer;
        try {
            String identifier = UUID.randomUUID().toString();
            consumer = client.newConsumer()
                    .topic(topic)
                    .consumerName(identifier)
                    .subscriptionName(identifier)
                    .ackTimeout(10, TimeUnit.MINUTES)
                    .negativeAckRedeliveryDelay(20, TimeUnit.MILLISECONDS)
                    .subscribe();
        } catch (Exception exception) {
            return Result.problem(Problem.error(exception));
        }
        return Result.some(consumer);
    }
}
