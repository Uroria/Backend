package com.uroria.backend.impl.user;

import com.uroria.backend.impl.pulsar.PulsarRequest;
import com.uroria.backend.user.User;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class UserNameRequestChannel extends PulsarRequest<User, String> {
    public UserNameRequestChannel(@NonNull PulsarClient pulsarClient, @NonNull String name) throws PulsarClientException {
        super(pulsarClient, "user:request:name", "user:response:name", name, 3000);
    }
}
