package com.uroria.backend.impl.permission;

import com.google.gson.JsonElement;
import com.rabbitmq.client.Connection;
import com.uroria.backend.Backend;
import com.uroria.backend.impl.AbstractManager;
import com.uroria.backend.impl.communication.CommunicationClient;
import com.uroria.backend.impl.communication.request.RabbitRequestChannel;
import com.uroria.backend.impl.communication.request.RequestChannel;
import com.uroria.backend.permission.events.GroupDeletedEvent;
import com.uroria.backend.permission.events.GroupUpdatedEvent;
import com.uroria.base.event.EventManager;
import com.uroria.base.io.InsaneByteArrayInputStream;
import com.uroria.base.io.InsaneByteArrayOutputStream;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PermGroupManager extends AbstractManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("Perms");

    private final CommunicationClient client;
    private final RequestChannel request;
    private final ObjectSet<GroupWrapper> groups;

    public PermGroupManager(Connection rabbit, Logger logger) {
        super(rabbit, logger);
        this.groups = new ObjectArraySet<>();
        EventManager eventManager = Backend.getEventManager();
        this.client = new CommunicationClient(rabbit, "permgroup-request", "permgroup-update", element -> {
            JsonElement nameElement = element.get("name");
            if (nameElement == null) return;
            String name = nameElement.getAsString();
            boolean delete = false;
            for (GroupWrapper wrapper : this.groups) {
                if (!wrapper.getName().equals(name)) continue;
                if (wrapper.isDeleted()) {
                    delete = true;
                    eventManager.callAndForget(new GroupDeletedEvent(wrapper));
                } else wrapper.refreshPermissions();
                eventManager.callAndForget(new GroupUpdatedEvent(wrapper));
                break;
            }
            if (delete) delete(name);
        });
        this.request = new RabbitRequestChannel(rabbit, "permgroup-requests");
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {
        this.request.close();
        this.client.close();
    }

    public void delete(String name) {
        this.groups.removeIf(group -> group.getName().equals(name));
        this.client.delete(name);
    }

    public GroupWrapper getWrapper(String name, boolean create) {
        for (GroupWrapper wrapper : this.groups) {
            if (!wrapper.getName().equals(name)) continue;
            return wrapper;
        }

        byte[] data;
        try {
            InsaneByteArrayOutputStream output = new InsaneByteArrayOutputStream();
            output.writeUTF(name);
            output.close();
            data = output.toByteArray();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        Result<byte[]> result = this.request.requestSync(data, 4000);

        if (result instanceof Result.Problematic<byte[]> problem) {
            LOGGER.error("Cannot request group " + name, problem.getProblem().getError().orElse(new RuntimeException("Unknown error")));
            return null;
        }

        byte[] bytes = result.get();
        if (bytes == null) {
            return null;
        }

        try {
            InsaneByteArrayInputStream input = new InsaneByteArrayInputStream(bytes);
            if (!input.readBoolean() && !create) {
                input.close();
                return null;
            }
            name = input.readUTF();
            input.close();
            GroupWrapper wrapper = new GroupWrapper(this.client, name);
            wrapper.getObject().addProperty("name", name);
            this.groups.add(wrapper);
            return wrapper;
        } catch (Exception exception) {
            LOGGER.error("Cannot read group " + name, exception);
            return null;
        }
    }
}
