package com.uroria.backend.impl.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Connection;
import com.uroria.backend.Backend;
import com.uroria.backend.app.ApplicationStatus;
import com.uroria.backend.impl.AbstractManager;
import com.uroria.backend.impl.communication.CommunicationClient;
import com.uroria.backend.impl.communication.request.RabbitRequestChannel;
import com.uroria.backend.impl.communication.request.RequestChannel;
import com.uroria.backend.server.events.ServerDeletedEvent;
import com.uroria.backend.server.events.ServerGroupDeletedEvent;
import com.uroria.backend.server.events.ServerGroupUpdatedEvent;
import com.uroria.backend.server.events.ServerUpdatedEvent;
import com.uroria.base.event.EventManager;
import com.uroria.base.io.InsaneByteArrayInputStream;
import com.uroria.base.io.InsaneByteArrayOutputStream;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public final class  ServerManager extends AbstractManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("Servers");

    private final CommunicationClient serverClient;
    private final CommunicationClient groupClient;
    private final RequestChannel serverRequest;
    private final RequestChannel serverStartRequest;
    private final RequestChannel groupRequest;
    private final ObjectSet<ServerWrapper> servers;
    private final ObjectSet<ServerGroupWrapper> groups;

    public ServerManager(Connection rabbit, Logger logger) {
        super(rabbit, logger);
        this.servers = new ObjectArraySet<>();
        this.groups = new ObjectArraySet<>();
        EventManager eventManager = Backend.getEventManager();
        this.serverClient = new CommunicationClient(rabbit, "server-request", "server-update", element -> {
            JsonElement identifierElement = element.get("identifier");
            if (identifierElement == null) return;
            long identifier = identifierElement.getAsLong();
            boolean delete = false;
            for (ServerWrapper wrapper : this.servers) {
                if (wrapper.getIdentifier() != identifier) continue;
                if (wrapper.isDeleted()) {
                    delete = true;
                    eventManager.callAndForget(new ServerDeletedEvent(wrapper));
                }
                eventManager.callAndForget(new ServerUpdatedEvent(wrapper));
                break;
            }
            if (delete) deleteServer(identifier);
        });
        this.groupClient = new CommunicationClient(rabbit, "servergroup-request", "servergroup-update", element -> {
            JsonElement typeElement = element.get("type");
            if (typeElement == null) return;
            String type = typeElement.getAsString();
            boolean delete = false;
            for (ServerGroupWrapper wrapper : this.groups) {
                if (!wrapper.getType().equals(type)) continue;
                if (wrapper.isDeleted()) {
                    delete = true;
                    eventManager.callAndForget(new ServerGroupDeletedEvent(wrapper));
                }
                eventManager.callAndForget(new ServerGroupUpdatedEvent(wrapper));
                break;
            }
            if (delete) deleteGroup(type);
        });
        this.serverRequest = new RabbitRequestChannel(rabbit, "server-requests");
        this.serverStartRequest = new RabbitRequestChannel(rabbit, "server-start-request");
        this.groupRequest = new RabbitRequestChannel(rabbit, "servergroup-requests");
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {
        this.groupClient.close();
        this.serverClient.close();
        this.serverRequest.close();
        this.groupRequest.close();
    }

    public void deleteServer(long identifier) {
        this.servers.removeIf(server -> server.getIdentifier() == identifier);
        this.serverClient.delete(String.valueOf(identifier));
    }

    public void deleteGroup(String type) {
        this.groups.removeIf(group -> group.getType().equals(type));
        this.groupClient.delete(type);
    }

    public ServerWrapper getServer(long identifier) {

    }

    public ServerWrapper createServer(String type, int templateId, int maxPlayers) {
        long identifier = new Random().nextLong() + System.currentTimeMillis();

        byte[] data;
        try {
            InsaneByteArrayOutputStream output = new InsaneByteArrayOutputStream();
            output.writeUTF(type);
            output.writeInt(templateId);
            output.writeLong(identifier);
            output.close();
            data = output.toByteArray();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        Result<byte[]> result = this.serverStartRequest.requestSync(data, 90000);
        if (result instanceof Result.Problematic<byte[]> problem) {
            LOGGER.error("Cannot request start of server " + identifier, problem.getProblem().getError().orElse(new RuntimeException("Unknown error")));
            return null;
        }

        byte[] bytes = result.get();
        if (bytes == null) {
            return null;
        }

        try {
            InsaneByteArrayInputStream input = new InsaneByteArrayInputStream(bytes);
            boolean ack = input.readBoolean();
            input.close();
            if (!ack) return null;
            ServerWrapper wrapper = new ServerWrapper(this.serverClient, identifier);
            JsonObject object = wrapper.getObject();
            object.addProperty("templateId", templateId);
            object.addProperty("type", type);
            object.addProperty("maxPlayerCount", maxPlayers);
            object.addProperty("status", ApplicationStatus.STARTING.getID());
            return wrapper;
        } catch (Exception exception) {
            LOGGER.error("Cannot read start ack of server " + identifier, exception);
            return null;
        }
    }
}
