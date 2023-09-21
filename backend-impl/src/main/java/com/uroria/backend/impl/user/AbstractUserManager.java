package com.uroria.backend.impl.user;

import com.uroria.backend.impl.AbstractManager;
import com.uroria.backend.impl.pulsar.Result;
import com.uroria.base.io.InsaneByteArrayInputStream;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.NonNull;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.PulsarClient;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public abstract class AbstractUserManager extends AbstractManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("Users");

    protected final ObjectSet<UserWrapper> users;

    public AbstractUserManager(@NonNull PulsarClient pulsarClient, @Nullable CryptoKeyReader cryptoKeyReader) {
        super(pulsarClient, LOGGER, "user/request", "user/update", cryptoKeyReader);
        this.users = new ObjectArraySet<>();
    }

    @Override
    public void checkObject(Object identifier, int channel, Object object) {
        UUID uuid = toUUID(identifier);
        if (uuid == null) return;
        for (UserWrapper wrapper : this.users) {
            if (!wrapper.getUniqueId().equals(uuid)) continue;
            wrapper.checkObject(channel, object);
            return;
        }
    }

    @Override
    public void checkString(Object identifier, int channel, @Nullable String value) {
        UUID uuid = toUUID(identifier);
        if (uuid == null) return;
        for (UserWrapper wrapper : this.users) {
            if (!wrapper.getUniqueId().equals(uuid)) continue;
            wrapper.checkString(channel, value);
            return;
        }
    }

    @Override
    public void checkInt(Object identifier, int channel, int value) {
        UUID uuid = toUUID(identifier);
        if (uuid == null) return;
        for (UserWrapper wrapper : this.users) {
            if (!wrapper.getUniqueId().equals(uuid)) continue;
            wrapper.checkInt(channel, value);
            return;
        }
    }

    @Override
    public void checkLong(Object identifier, int channel, long value) {
        UUID uuid = toUUID(identifier);
        if (uuid == null) return;
        for (UserWrapper wrapper : this.users) {
            if (!wrapper.getUniqueId().equals(uuid)) continue;
            wrapper.checkLong(channel, value);
            return;
        }
    }

    @Override
    public void checkBoolean(Object identifier, int channel, boolean value) {
        UUID uuid = toUUID(identifier);
        if (uuid == null) return;
        for (UserWrapper wrapper : this.users) {
            if (!wrapper.getUniqueId().equals(uuid)) continue;
            wrapper.checkBoolean(channel, value);
            return;
        }
    }

    @Override
    public void checkFloat(Object identifier, int channel, float value) {
        UUID uuid = toUUID(identifier);
        if (uuid == null) return;
        for (UserWrapper wrapper : this.users) {
            if (!wrapper.getUniqueId().equals(uuid)) continue;
            wrapper.checkFloat(channel, value);
            return;
        }
    }

    @Override
    public void checkDouble(Object identifier, int channel, double value) {
        UUID uuid = toUUID(identifier);
        if (uuid == null) return;
        for (UserWrapper wrapper : this.users) {
            if (!wrapper.getUniqueId().equals(uuid)) continue;
            wrapper.checkDouble(channel, value);
            return;
        }
    }

    private UUID toUUID(Object object) {
        try {
            return (UUID) object;
        } catch (Exception exception) {
            return null;
        }
    }

    public @Nullable final UserWrapper getWrapper(String name) {
        Result<InsaneByteArrayInputStream> result = this.request.request(out -> {
            try {
                out.writeInt(1);
                out.writeUTF(name);
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }, 2000);

        if (result instanceof Result.Error<InsaneByteArrayInputStream> error) {
            this.logger.error("Cannot request User", error.getError());
            return null;
        }

        try {
            InsaneByteArrayInputStream in = result.get();
            if (in == null) return null;
            String uuidString = in.readUTF();
            return new UserWrapper(this, UUID.fromString(uuidString));
        } catch (Exception exception) {
            this.logger.error("Cannot read UUID of User response", exception);
            return null;
        }
    }

    public @Nullable final UserWrapper getWrapper(UUID uuid) {
        Result<InsaneByteArrayInputStream> result = this.request.request(out -> {
            try {
                out.writeInt(2);
                out.writeUTF(uuid.toString());
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }, 2000);

        if (result instanceof Result.Error<InsaneByteArrayInputStream> error) {
            this.logger.error("Cannot request User", error.getError());
            return null;
        }

        try {
            InsaneByteArrayInputStream in = result.get();
            if (in == null) return null;
            return new UserWrapper(this, uuid);
        } catch (Exception exception) {
            this.logger.error("Cannot read UUID of User response", exception);
            return null;
        }
    }

    public final @Nullable AbstractUser getUser(UUID uuid) {
        if (uuid == null) return null;
        for (AbstractUser user : this.users) {
            if (user.getUniqueId().equals(uuid)) return user;
        }
        return getWrapper(uuid);
    }

    public final AbstractUser getUser(String username) {
        if (username == null) return null;
        for (AbstractUser user : this.users) {
            if (user.getUsername().equals(username)) return user;
        }
        return getWrapper(username);
    }
}
