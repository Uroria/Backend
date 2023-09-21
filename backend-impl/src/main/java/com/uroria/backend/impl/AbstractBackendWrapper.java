package com.uroria.backend.impl;

import com.uroria.backend.BackendWrapper;
import com.uroria.backend.impl.configurations.PulsarConfiguration;
import com.uroria.base.event.EventManager;
import com.uroria.base.event.EventManagerFactory;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import org.apache.pulsar.client.api.AutoClusterFailoverBuilder;
import org.apache.pulsar.client.api.ClientBuilder;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.ServiceUrlProvider;
import org.apache.pulsar.client.impl.AutoClusterFailover;
import org.apache.pulsar.client.impl.DefaultCryptoKeyReader;
import org.apache.pulsar.client.impl.DefaultCryptoKeyReaderBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class AbstractBackendWrapper implements BackendWrapper {
    protected final PulsarClient pulsarClient;
    @Nullable
    @Getter
    protected final CryptoKeyReader cryptoKeyReader;
    private final EventManager eventManager;

    protected AbstractBackendWrapper() {
        ClientBuilder builder = PulsarClient.builder();

        boolean encryption = PulsarConfiguration.isPulsarEncryptionEnabled();
        if (encryption) {
            DefaultCryptoKeyReaderBuilder keyReaderBuilder = DefaultCryptoKeyReader.builder();
            keyReaderBuilder.defaultPublicKey(PulsarConfiguration.getPulsarEncryptionPublicKey());
            keyReaderBuilder.defaultPrivateKey(PulsarConfiguration.getPulsarEncryptionPrivateKey());
            this.cryptoKeyReader = keyReaderBuilder.build();
        } else this.cryptoKeyReader = null;

        AutoClusterFailoverBuilder failOverBuilder = AutoClusterFailover.builder();
        failOverBuilder.primary(PulsarConfiguration.getPulsarPrimaryUrl());

        List<String> pulsarUrls = new ObjectArrayList<>();
        pulsarUrls.addAll(Arrays.asList(PulsarConfiguration.getPulsarBackupUrls()));

        if (!pulsarUrls.isEmpty()) failOverBuilder.secondary(pulsarUrls);

        failOverBuilder.failoverDelay(20, TimeUnit.SECONDS);
        failOverBuilder.switchBackDelay(60, TimeUnit.SECONDS);
        failOverBuilder.checkInterval(2, TimeUnit.SECONDS);

        ServiceUrlProvider serviceUrlProvider = failOverBuilder.build();

        try {
            this.pulsarClient = builder.statsInterval(10, TimeUnit.MINUTES)
                    .serviceUrlProvider(serviceUrlProvider)
                    .build();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        this.eventManager = EventManagerFactory.create("BackendEvents");
    }

    protected AbstractBackendWrapper(PulsarClient pulsarClient, @Nullable CryptoKeyReader cryptoKeyReader) {
        this.pulsarClient = pulsarClient;
        this.cryptoKeyReader = cryptoKeyReader;
        this.eventManager = EventManagerFactory.create("BackendEvents");
    }

    @Override
    public final EventManager getEventManager() {
        return this.eventManager;
    }

    abstract public void start() throws PulsarClientException;

    public void shutdown() throws PulsarClientException {
        if (this.pulsarClient != null) this.pulsarClient.shutdown();
    }

    public final PulsarClient getPulsarClient() {
        return this.pulsarClient;
    }
}
