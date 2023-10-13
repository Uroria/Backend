package com.uroria.backend.impl.user;

import com.rabbitmq.client.Connection;
import com.uroria.backend.impl.communication.request.RabbitRequestChannel;
import com.uroria.backend.impl.communication.request.RequestChannel;
import com.uroria.backend.impl.io.BackendOutputStream;
import com.uroria.backend.impl.stats.StatsManager;
import com.uroria.backend.impl.wrapper.WrapperManager;
import com.uroria.backend.user.events.UserDeletedEvent;
import com.uroria.backend.user.events.UserUpdatedEvent;
import com.uroria.base.io.InsaneByteArrayInputStream;
import com.uroria.problemo.result.Result;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public final class UserManager extends WrapperManager<UserWrapper> {
    private final StatsManager statsManager;
    private final RequestChannel nameRequest;

    public UserManager(StatsManager statsManager, Connection rabbit) {
        super(rabbit, LoggerFactory.getLogger("Users"), "user", "uuid");
        this.statsManager = statsManager;
        this.nameRequest = new RabbitRequestChannel(rabbit, "user-name-request");
    }

    @Override
    protected void onUpdate(UserWrapper wrapper) {
        if (wrapper.isDeleted()) {
            eventManager.callAndForget(new UserDeletedEvent(wrapper));
        }
        eventManager.callAndForget(new UserUpdatedEvent(wrapper));
    }

    public UserWrapper getUserWrapper(UUID uuid) {
        return getWrapper(uuid.toString(), true);
    }

    public UserWrapper getUserWrapper(String username) {
        Result<byte[]> result = this.nameRequest.requestSync(() -> {
            try {
                BackendOutputStream output = new BackendOutputStream();
                output.writeByte(0);
                output.writeUTF(username);
                output.close();
                return output.toByteArray();
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }, 2000);
        if (result instanceof Result.Problematic<byte[]> problematic) {
            logger.error("Cannot request users uuid by name " + username, problematic.getProblem().getError().orElse(new RuntimeException("Unknown Exception")));
            return null;
        }
        try {
            byte[] bytes = result.get();
            if (bytes == null) return null;
            InsaneByteArrayInputStream input = new InsaneByteArrayInputStream(bytes);
            if (!input.readBoolean()) {
                input.close();
                return null;
            }
            String uuidString = input.readUTF();
            input.close();
            return getWrapper(uuidString, false);
        } catch (Exception exception) {
            logger.error("Cannot read uuid response for username " + username, exception);
            return null;
        }
    }

    public UserWrapper getUserWrapper(long discordId) {
        Result<byte[]> result = this.nameRequest.requestSync(() -> {
            try {
                BackendOutputStream output = new BackendOutputStream();
                output.writeByte(1);
                output.writeLong(discordId);
                output.close();
                return output.toByteArray();
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }, 2000);
        if (result instanceof Result.Problematic<byte[]> problematic) {
            logger.error("Cannot request users uuid by discordId " + discordId, problematic.getProblem().getError().orElse(new RuntimeException("Unknown Exception")));
            return null;
        }
        try {
            byte[] bytes = result.get();
            if (bytes == null) return null;
            InsaneByteArrayInputStream input = new InsaneByteArrayInputStream(bytes);
            if (!input.readBoolean()) {
                input.close();
                return null;
            }
            String uuidString = input.readUTF();
            input.close();
            return getWrapper(uuidString, false);
        } catch (Exception exception) {
            logger.error("Cannot read uuid response for discordId " + discordId, exception);
            return null;
        }
    }

    @Override
    protected UserWrapper createWrapper(String identifier) {
        return new UserWrapper(this, UUID.fromString(identifier), statsManager);
    }
}
