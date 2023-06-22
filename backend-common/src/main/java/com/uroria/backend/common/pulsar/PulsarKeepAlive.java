package com.uroria.backend.common.pulsar;

import com.uroria.backend.common.utils.BackendOutputStream;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PulsarKeepAlive<T> extends Thread {
    protected static final Logger LOGGER = LoggerFactory.getLogger("PulsarKeepAlive");

    private final PulsarClient pulsarClient;
    private final Producer<byte[]> producer;
    protected final String bridgeName;
    protected final T obj;

    public PulsarKeepAlive(PulsarClient pulsarClient, String topic, String bridgeName, T object) throws PulsarClientException {
        this.pulsarClient = pulsarClient;
        this.producer = pulsarClient.newProducer()
                .producerName(bridgeName)
                .topic(topic)
                .create();
        this.bridgeName = bridgeName;
        this.obj = object;
    }

    public final void close() throws PulsarClientException {
        this.producer.close();
    }

    @Override
    public final void run() {
        while (!this.pulsarClient.isClosed() && this.producer.isConnected()) {
            try (BackendOutputStream output = new BackendOutputStream()) {
                output.writeObject(obj);
                output.close();
                this.producer.send(output.toByteArray());
                LOGGER.info("Send");
            } catch (Exception exception) {
                LOGGER.warn("Cannot send keep alive", exception);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException exception) {
                LOGGER.warn("Couldn't sleep thread", exception);
            }
        }
    }
}
