package com.uroria.backend.impl.user;

import com.uroria.backend.impl.AbstractManager;
import com.uroria.backend.impl.pulsar.PulsarRequestChannel;
import com.uroria.backend.impl.pulsar.Result;
import com.uroria.base.io.InsaneByteArrayInputStream;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.NonNull;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public final class UserManager extends AbstractManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("Users");

    private final PulsarRequestChannel request;
    private final ObjectSet<UserWrapper> users;

    public UserManager(@NonNull PulsarClient pulsarClient, @Nullable CryptoKeyReader cryptoKeyReader) {
        super(pulsarClient, LOGGER, "user/request", "user/update", cryptoKeyReader);
        this.users = new ObjectArraySet<>();
        this.request = new PulsarRequestChannel(pulsarClient, cryptoKeyReader, UUID.randomUUID().toString(), "users/request");
    }


    @Override
    public void start() throws PulsarClientException {

    }

    @Override
    public void shutdown() throws PulsarClientException {
        this.request.close();
        this.object.close();
    }

    public UserWrapper getWrapper(UUID uuid) {
        for (UserWrapper wrapper : this.users) {
            if (!wrapper.getUniqueId().equals(uuid)) continue;
            return wrapper;
        }

        Result<InsaneByteArrayInputStream> result = this.request.request(output -> {
            try {
                output.writeShort(0);
                output.writeUTF(uuid.toString());
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }, 2000);
        if (result instanceof Result.Error<InsaneByteArrayInputStream> error) {
            LOGGER.error("Cannot request user " + uuid, error.getError());
            return new UserWrapper(this.object, uuid);
        }

        InsaneByteArrayInputStream input = result.get();
        if (input == null) {
            return new UserWrapper(this.object, uuid);
        }
        try {
            String uuidString = input.readUTF();
            UserWrapper wrapper = new UserWrapper(this.object, UUID.fromString(uuidString));
            this.users.add(wrapper);
            return wrapper;
        } catch (Exception exception) {
            LOGGER.error("Cannot read user " + uuid, exception);
            return null;
        }
    }

    public UserWrapper getWrapper(String username) {
        for (UserWrapper wrapper : this.users) {
            if (!wrapper.getUsername().equalsIgnoreCase(username)) continue;
            return wrapper;
        }

        Result<InsaneByteArrayInputStream> result = this.request.request(output -> {
            try {
                output.writeShort(1);
                output.writeUTF(username);
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }, 2000);
        if (result instanceof Result.Error<InsaneByteArrayInputStream> error) {
            LOGGER.error("Cannot request user " + username, error.getError());
            return null;
        }

        InsaneByteArrayInputStream input = result.get();
        if (input == null) {
            return null;
        }
        try {
            if (!input.readBoolean()) return null;
            String uuidString = input.readUTF();
            UserWrapper wrapper = new UserWrapper(this.object, UUID.fromString(uuidString));
            this.users.add(wrapper);
            return wrapper;
        } catch (Exception exception) {
            LOGGER.error("Cannot read user " + username, exception);
            return null;
        }
    }
}
