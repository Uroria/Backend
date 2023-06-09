package com.uroria.backend.server;

import com.uroria.backend.common.pulsar.PulsarRequest;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.List;

public class BackendAllServersRequest extends PulsarRequest<List<Integer>, Integer> {

    public BackendAllServersRequest(PulsarClient pulsarClient, String bridgeName) throws PulsarClientException {
        super(pulsarClient, "server:request:all", "server:response:all", bridgeName, 2000, 2000);
    }
}
