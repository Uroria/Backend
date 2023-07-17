package com.uroria.backend.impl.server;

import com.uroria.backend.pulsar.PulsarKeepAlive;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public class BackendServerKeepAlive extends PulsarKeepAlive {
    public BackendServerKeepAlive(PulsarClient pulsarClient, String bridgeName, Long object) throws PulsarClientException {
        super(pulsarClient, "server:keepalive", bridgeName, object);
    }
}