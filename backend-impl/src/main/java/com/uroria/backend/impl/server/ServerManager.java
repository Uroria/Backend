package com.uroria.backend.impl.server;

import com.rabbitmq.client.Connection;
import com.uroria.backend.app.ApplicationStatus;
import com.uroria.backend.impl.communication.CommunicationWrapper;
import com.uroria.backend.impl.communication.request.RabbitRequestChannel;
import com.uroria.backend.impl.communication.request.RequestChannel;
import com.uroria.backend.impl.io.BackendOutputStream;
import com.uroria.backend.impl.wrapper.WrapperManager;
import com.uroria.backend.server.Server;
import com.uroria.backend.server.events.ServerDeletedEvent;
import com.uroria.backend.server.events.ServerUpdatedEvent;
import com.uroria.base.io.InsaneByteArrayInputStream;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Random;

public final class ServerManager extends WrapperManager<ServerWrapper> {
    private final RequestChannel requestAll;

    public ServerManager(Connection rabbit) {
        super(rabbit, LoggerFactory.getLogger("Servers"), "server", "identifier");
        this.requestAll = new RabbitRequestChannel(rabbit, "server-requestall") ;
    }

    @Override
    protected void onUpdate(ServerWrapper wrapper) {
        if (wrapper.isDeleted()) {
            eventManager.callAndForget(new ServerDeletedEvent(wrapper));
        }
        eventManager.callAndForget(new ServerUpdatedEvent(wrapper));
    }

    public Collection<Server> getServers() {
        Result<byte[]> result = this.requestAll.requestSync(() -> {
            try {
                BackendOutputStream output = new BackendOutputStream();
                output.writeBoolean(true);
                output.close();
                return output.toByteArray();
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }, 2000);
        if (result instanceof Result.Problematic<byte[]> problematic) {
            logger.error("Cannot request all perm-groups", problematic.getProblem().getError().orElse(new RuntimeException("Unknown Exception")));
            return ObjectSets.emptySet();
        }
        try {
            byte[] bytes = result.get();
            if (bytes == null) return ObjectSets.emptySet();
            Collection<Long> wrappers = new ObjectArraySet<>();
            InsaneByteArrayInputStream input = new InsaneByteArrayInputStream(bytes);
            int length = input.readInt();
            int index = 0;
            while (index < length) {
                wrappers.add(input.readLong());
                index++;
            }
            input.close();
            return wrappers.stream()
                    .map(name -> (Server) getServerWrapper(name))
                    .toList();
        } catch (Exception exception) {
            logger.error("Cannot read get all perm-groups response", exception);
            return ObjectSets.emptySet();
        }
    }

    @Override
    protected ServerWrapper createWrapper(String identifier) {
        return new ServerWrapper(client, Long.parseLong(identifier));
    }

    public ServerWrapper getServerWrapper(long identifier) {
        return getWrapper(String.valueOf(identifier), false);
    }

    public ServerWrapper createServerWrapper(int templateId, String type, int maxPlayers) {
        long identifier = new Random().nextLong() + System.currentTimeMillis();
        ServerWrapper wrapper = new ServerWrapper(this.client, identifier);
        this.wrappers.add(wrapper);
        CommunicationWrapper object = wrapper.getObjectWrapper();
        object.set("identifier", identifier);
        object.set("templateId", templateId);
        object.set("type", type);
        object.set("maxPlayerCount", maxPlayers);
        object.set("status", ApplicationStatus.STARTING.getID());
        return getWrapper(String.valueOf(identifier), true);
    }
}
