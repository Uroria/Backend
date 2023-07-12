package com.uroria.backend.pulsar;

import com.uroria.backend.BackendPing;
import com.uroria.backend.utils.BackendOutputStream;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PulsarKeepAlive extends Thread {
    protected static final Logger LOGGER = LoggerFactory.getLogger("PulsarKeepAlive");

    private final PulsarClient pulsarClient;
    private final Producer<byte[]> producer;
    protected final String bridgeName;
    protected final long identifier;
    public PulsarKeepAlive(PulsarClient pulsarClient, String topic, String bridgeName, long identifier) throws PulsarClientException {
        this.pulsarClient = pulsarClient;
        this.producer = pulsarClient.newProducer()
                .producerName(bridgeName)
                .topic(topic)
                .create();
        this.bridgeName = bridgeName;
        this.identifier = identifier;
    }

    public final void close() throws PulsarClientException {
        this.producer.close();
    }

    @Override
    public final void run() {
        while (!this.pulsarClient.isClosed() && this.producer.isConnected()) {
            try (BackendOutputStream output = new BackendOutputStream()) {
                output.writeObject(new BackendPing(this.identifier, System.currentTimeMillis()));
                output.close();
                this.producer.send(output.toByteArray());
            } catch (Exception exception) {
                LOGGER.warn("Cannot send keep alive", exception);
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException exception) {
                LOGGER.warn("Couldn't sleep thread", exception);
            }
        }
    }
}
