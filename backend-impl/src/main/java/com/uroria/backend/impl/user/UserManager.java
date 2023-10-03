package com.uroria.backend.impl.user;

import com.google.gson.JsonElement;
import com.rabbitmq.client.Connection;
import com.uroria.backend.Backend;
import com.uroria.backend.impl.AbstractManager;
import com.uroria.backend.impl.communication.CommunicationClient;
import com.uroria.backend.impl.communication.request.RabbitRequestChannel;
import com.uroria.backend.impl.communication.request.RequestChannel;
import com.uroria.backend.user.events.UserDeletedEvent;
import com.uroria.backend.user.events.UserUpdatedEvent;
import com.uroria.base.event.EventManager;
import com.uroria.base.io.InsaneByteArrayInputStream;
import com.uroria.base.io.InsaneByteArrayOutputStream;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public final class UserManager extends AbstractManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("Users");

    private final CommunicationClient client;
    private final RequestChannel request;
    private final ObjectSet<UserWrapper> users;

    public UserManager(Connection rabbit, Logger logger) {
        super(rabbit, logger);
        this.users = new ObjectArraySet<>();
        EventManager eventManager = Backend.getEventManager();
        this.client = new CommunicationClient( rabbit, "user-request", "user-update", element -> {
            JsonElement uuidStringElement = element.get("uuid");
            if (uuidStringElement == null) return;
            String uuidString = uuidStringElement.getAsString();
            UUID uuid;
            try {
                uuid = UUID.fromString(uuidString);
            } catch (Exception exception) {
                return;
            }
            boolean delete = false;
            for (UserWrapper wrapper : this.users) {
                if (!wrapper.getUniqueId().equals(uuid)) continue;
                if (wrapper.isDeleted()) {
                    delete = true;
                    eventManager.callAndForget(new UserDeletedEvent(wrapper));
                } else wrapper.refreshPermissions();
                eventManager.callAndForget(new UserUpdatedEvent(wrapper));
                break;
            }
            if (delete) delete(uuid);
        });
        this.request = new RabbitRequestChannel(rabbit, "user-requests");
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {
        this.request.close();
        this.client.close();
    }

    public void delete(UUID uuid) {
        this.users.removeIf(user -> user.getUniqueId().equals(uuid));
        this.client.delete(uuid.toString());
    }

    public UserWrapper getWrapper(UUID uuid) {
        for (UserWrapper wrapper : this.users) {
            if (!wrapper.getUniqueId().equals(uuid)) continue;
            return wrapper;
        }

        byte[] data;
        try {
            InsaneByteArrayOutputStream output = new InsaneByteArrayOutputStream();
            output.writeByte(0);
            output.writeUTF(uuid.toString());
            output.close();
            data = output.toByteArray();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        Result<byte[]> result = this.request.requestSync(data, 4000);

        if (result instanceof Result.Problematic<byte[]> problem) {
            LOGGER.error("Cannot request user " + uuid, problem.getProblem().getError().orElse(new RuntimeException("Unknown error")));
            return null;
        }

        byte[] bytes = result.get();
        if (bytes == null) {
            return null;
        }

        try {
            InsaneByteArrayInputStream input = new InsaneByteArrayInputStream(bytes);
            String uuidString = input.readUTF();
            input.close();
            UserWrapper wrapper = new UserWrapper(this.client, UUID.fromString(uuidString));
            wrapper.getObject().addProperty("uuid", uuid.toString());
            this.users.add(wrapper);
            return wrapper;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public UserWrapper getWrapper(String username) {
        for (UserWrapper wrapper : this.users) {
            if (!wrapper.getUsername().equals(username)) continue;
            return wrapper;
        }

        byte[] data;
        try {
            InsaneByteArrayOutputStream output = new InsaneByteArrayOutputStream();
            output.writeByte(1);
            output.writeUTF(username);
            output.close();
            data = output.toByteArray();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        Result<byte[]> result = this.request.requestSync(data, 4000);

        if (result instanceof Result.Problematic<byte[]> problem) {
            LOGGER.error("Cannot request user " + username, problem.getProblem().getError().orElse(new RuntimeException("Unknown error")));
            return null;
        }

        byte[] bytes = result.get();
        if (bytes == null) {
            return null;
        }

        try {
            InsaneByteArrayInputStream input = new InsaneByteArrayInputStream(bytes);
            if (!input.readBoolean()) return null;
            String uuidString = input.readUTF();
            input.close();
            UUID uuid = UUID.fromString(uuidString);
            UserWrapper wrapper = new UserWrapper(this.client, uuid);
            wrapper.getObject().addProperty("uuid", uuid.toString());
            this.users.add(wrapper);
            return wrapper;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
