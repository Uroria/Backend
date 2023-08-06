package com.uroria.backend.impl.user;

import com.uroria.backend.impl.pulsar.PulsarUpdate;
import com.uroria.backend.user.User;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.function.Consumer;

public final class UserUpdateChannel extends PulsarUpdate<User> {
    private final Consumer<User> userConsumer;

    public UserUpdateChannel(@NonNull PulsarClient pulsarClient, @NonNull String name, @NonNull Consumer<User> userConsumer) throws PulsarClientException {
        super(pulsarClient, "user:update", name);
        this.userConsumer = userConsumer;
    }

    @Override
    protected void onUpdate(User user) {
        this.userConsumer.accept(user);
    }
}
