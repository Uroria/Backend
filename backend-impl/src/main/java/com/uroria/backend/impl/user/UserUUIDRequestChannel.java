package com.uroria.backend.impl.user;

import com.uroria.backend.impl.pulsar.PulsarRequest;
import com.uroria.backend.user.User;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.UUID;

public final class UserUUIDRequestChannel extends PulsarRequest<User, UUID> {
    public UserUUIDRequestChannel(@NonNull PulsarClient pulsarClient, @NonNull String name) throws PulsarClientException {
        super(pulsarClient, "user:request:uuid", "user:response:uuid", name, 3000);
    }
}
