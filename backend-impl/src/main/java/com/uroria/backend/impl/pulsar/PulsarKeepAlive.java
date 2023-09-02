package com.uroria.backend.impl.pulsar;

import com.uroria.backend.impl.ping.BackendPing;
import com.uroria.base.io.InsaneByteArrayOutputStream;
import com.uroria.base.utils.ThreadUtils;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

public class PulsarKeepAlive extends Thread {
    protected static final Logger LOGGER = Pulsar.getLogger();

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
        start();
    }

    public final void close() throws PulsarClientException {
        this.producer.close();
    }

    @Override
    public final void run() {
        while (!this.pulsarClient.isClosed() && this.producer.isConnected()) {
            try (InsaneByteArrayOutputStream output = new InsaneByteArrayOutputStream()) {
                output.writeObject(new BackendPing(this.identifier, System.currentTimeMillis()));
                output.close();
                this.producer.send(output.toByteArray());
            } catch (Exception exception) {
                LOGGER.warn("Cannot send keep alive", exception);
            }
            ThreadUtils.sleep(2, TimeUnit.SECONDS);
        }
    }
}
