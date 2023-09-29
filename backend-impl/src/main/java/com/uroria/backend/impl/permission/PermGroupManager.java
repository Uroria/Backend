package com.uroria.backend.impl.permission;

import com.uroria.backend.impl.AbstractManager;
import com.uroria.backend.impl.pulsar.PulsarRequestChannel;
import com.uroria.backend.permission.PermGroup;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public final class PermGroupManager extends AbstractManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("Perms");

    private final PulsarRequestChannel request;
    private final ObjectSet<PermGroup> groups;

    public PermGroupManager(PulsarClient pulsar, @Nullable CryptoKeyReader reader) {
        super(pulsar, LOGGER, "permgroup/request", "permgroup/update", reader);
        this.groups = new ObjectArraySet<>();
        this.request = new PulsarRequestChannel(pulsar, reader, UUID.randomUUID().toString(), "permgroups/request");
    }

    @Override
    protected void start() throws PulsarClientException {

    }

    @Override
    protected void shutdown() throws PulsarClientException {
        this.request.close();
        this.object.close();
    }




}
