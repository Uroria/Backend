package com.uroria.backend.impl.server;

import com.rabbitmq.client.Connection;
import com.uroria.backend.impl.communication.CommunicationWrapper;
import com.uroria.backend.impl.communication.request.RabbitRequestChannel;
import com.uroria.backend.impl.communication.request.RequestChannel;
import com.uroria.backend.impl.io.BackendOutputStream;
import com.uroria.backend.impl.wrapper.WrapperManager;
import com.uroria.backend.server.ServerGroup;
import com.uroria.backend.server.events.ServerGroupDeletedEvent;
import com.uroria.backend.server.events.ServerGroupUpdatedEvent;
import com.uroria.base.io.InsaneByteArrayInputStream;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public final class ServerGroupManager extends WrapperManager<ServerGroupWrapper> {
    private final RequestChannel requestAll;

    public ServerGroupManager(Connection rabbit) {
        super(rabbit, LoggerFactory.getLogger("ServerGroups"), "servergroup", "name");
        this.requestAll = new RabbitRequestChannel(rabbit, "servergroup-requestall");
    }

    @Override
    protected void onUpdate(ServerGroupWrapper wrapper) {
        if (wrapper.isDeleted()) {
            eventManager.callAndForget(new ServerGroupDeletedEvent(wrapper));
        }
        eventManager.callAndForget(new ServerGroupUpdatedEvent(wrapper));
    }

    public Collection<ServerGroup> getGroups() {
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
            logger.error("Cannot request all server-groups", problematic.getProblem().getError().orElse(new RuntimeException("Unknown Exception")));
            return ObjectSets.emptySet();
        }
        try {
            byte[] bytes = result.get();
            if (bytes == null) return ObjectSets.emptySet();
            Collection<String> wrappers = new ObjectArraySet<>();
            InsaneByteArrayInputStream input = new InsaneByteArrayInputStream(bytes);
            int length = input.readInt();
            int index = 0;
            while (index < length) {
                wrappers.add(input.readUTF());
                index++;
            }
            input.close();
            return wrappers.stream()
                    .map(name -> (ServerGroup) getGroupWrapper(name))
                    .toList();
        } catch (Exception exception) {
            logger.error("Cannot read get all server-groups response", exception);
            return ObjectSets.emptySet();
        }
    }

    public ServerGroupWrapper getGroupWrapper(String name) {
        return getWrapper(name, false);
    }

    public ServerGroupWrapper createGroupWrapper(String identifier, int maxPlayers) {
        ServerGroupWrapper wrapper = new ServerGroupWrapper(this.client, identifier);
        this.wrappers.add(wrapper);
        CommunicationWrapper object = wrapper.getObjectWrapper();
        object.set("name", identifier);
        object.set("maxPlayerCount", maxPlayers);
        return getWrapper(identifier, true);
    }

    @Override
    protected ServerGroupWrapper createWrapper(String identifier) {
        return new ServerGroupWrapper(client, identifier);
    }
}
