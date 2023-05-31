package com.uroria.backend.server.modules.stats;

import com.uroria.backend.common.BackendStat;
import com.uroria.backend.common.pulsar.PulsarReceiver;
import com.uroria.backend.common.utils.IOUtils;
import com.uroria.backend.server.Uroria;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;

public final class BackendStatRequest extends PulsarReceiver {
    private final Logger logger;
    private final BackendStatsManager statsManager;
    BackendStatRequest(PulsarClient pulsarClient, BackendStatsManager statsManager, Logger logger) throws PulsarClientException {
        super(pulsarClient, "stats:request", "StatsModule", 100000);
        this.logger = logger;
        this.statsManager = statsManager;
    }

    @Override
    public void onReceive(Consumer<byte[]> consumer, Message<byte[]> message) throws PulsarClientException {
        consumer.acknowledge(message);
        UUID uuid = null;
        try {
            ByteArrayInputStream inputBuffer = new ByteArrayInputStream(message.getData());
            ObjectInputStream input = new ObjectInputStream(inputBuffer);
            uuid = (UUID) input.readObject();
            input.close();
            inputBuffer.close();
        } catch (Exception exception) {
            this.logger.error("Cannot read data from " + message.getProducerName(), exception);
            Uroria.captureException(exception);
        }

        BackendStat stat = this.statsManager.getStat(uuid).orElse(null);
        this.logger.debug("Requesting stat with uuid " + uuid);

        try {
            ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(outputBuffer);
            output.writeObject(uuid);
            IOUtils.writeObject(output, stat);
            output.close();
            outputBuffer.close();
            this.statsManager.getResponseSender().send(outputBuffer.toByteArray());
        } catch (Exception exception) {
            this.logger.error("Cannot write data for " + message.getProducerName(), exception);
            Uroria.captureException(exception);
        }
    }
}
