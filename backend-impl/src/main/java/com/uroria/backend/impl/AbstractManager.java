package com.uroria.backend.impl;

import com.uroria.backend.impl.pulsar.PulsarObject;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.UUID;

public abstract class AbstractManager {
    protected final PulsarClient pulsarClient;
    protected final Logger logger;
    protected final PulsarObject object;
    @Nullable
    protected final CryptoKeyReader cryptoKeyReader;

    public AbstractManager(PulsarClient pulsarClient, Logger logger, String requestTopic, String updateTopic, @Nullable CryptoKeyReader cryptoKeyReader) {
        this.pulsarClient = pulsarClient;
        this.logger = logger;
        this.cryptoKeyReader = cryptoKeyReader;
        this.object = new PulsarObject(pulsarClient, cryptoKeyReader, UUID.randomUUID().toString(), requestTopic, updateTopic);
    }

    abstract protected void start() throws PulsarClientException;

    abstract protected void shutdown() throws PulsarClientException;
}
