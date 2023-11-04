package com.uroria.backend.impl.server;

import com.uroria.backend.cache.BackendObject;
import com.uroria.backend.cache.Wrapper;
import com.uroria.backend.cache.WrapperManager;
import com.uroria.backend.cache.communication.server.GetAllServersRequest;
import com.uroria.backend.cache.communication.server.GetAllServersResponse;
import com.uroria.backend.cache.communication.server.GetServerRequest;
import com.uroria.backend.cache.communication.server.GetServerResponse;
import com.uroria.backend.communication.Communicator;
import com.uroria.backend.communication.request.Requester;
import com.uroria.backend.impl.server.group.ServerGroupWrapper;
import com.uroria.backend.server.Server;
import com.uroria.backend.server.events.ServerDeletedEvent;
import com.uroria.backend.server.events.ServerUpdatedEvent;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Random;

public final class ServerManager extends WrapperManager<ServerWrapper> {
    private static final Logger logger = LoggerFactory.getLogger("Servers");

    private final Requester<GetServerRequest, GetServerResponse> idCheck;
    private final Requester<GetAllServersRequest, GetAllServersResponse> allGet;

    public ServerManager(Communicator communicator) {
        super(logger, communicator, "server", "server", "server");
        this.idCheck = requestPoint.registerRequester(GetServerRequest.class, GetServerResponse.class, "CheckId");
        this.allGet = requestPoint.registerRequester(GetAllServersRequest.class, GetAllServersResponse.class, "GetAll");
    }

    public ServerWrapper getServerWrapper(long id) {
        for (ServerWrapper wrapper : this.wrappers) {
            if (wrapper.getId() == id) return wrapper;
        }

        Result<GetServerResponse> result = this.idCheck.request(new GetServerRequest(id, false), 2000);
        GetServerResponse response = result.get();
        if (response == null) return null;
        if (!response.isExistent()) return null;
        ServerWrapper wrapper = new ServerWrapper(this, id, response.getName());
        this.wrappers.add(wrapper);
        return wrapper;
    }

    public ServerWrapper createServerWrapper(ServerGroupWrapper group, int templateId) {
        long id = System.currentTimeMillis() + new Random().nextLong(10000) - templateId;
        Result<GetServerResponse> result = this.idCheck.request(new GetServerRequest(id, true), 2000);
        GetServerResponse response = result.get();
        if (response == null) throw new IllegalStateException("No response from server creation call");
        if (!response.isExistent()) throw new IllegalStateException("We wanted to create a server, but it's non-existent?!");
        ServerWrapper wrapper = new ServerWrapper(this, id, group.getName());
        BackendObject<? extends Wrapper> object = wrapper.getBackendObject();
        object.set("id", id);
        object.set("templateId", templateId);
        object.set("group", group.getName());
        group.addServer(id);
        this.wrappers.add(wrapper);
        return wrapper;
    }

    public Collection<Server> getAll() {
        Result<GetAllServersResponse> result = this.allGet.request(new GetAllServersRequest(true), 5000);
        GetAllServersResponse response = result.get();
        if (response == null) return ObjectSets.emptySet();
        ObjectSet<Server> wrappers = new ObjectArraySet<>();
        response.getServers().forEach(id -> {
            ServerWrapper wrapper = getServerWrapper(id);
            if (wrapper == null) {
                logger.error("Cannot get server with id " + id + " while getting all");
                return;
            }
            wrappers.add(wrapper);
        });
        return wrappers;
    }

    @Override
    protected void onUpdate(ServerWrapper wrapper) {
        if (wrapper.isDeleted()) this.eventManager.callAndForget(new ServerDeletedEvent(wrapper));
        else this.eventManager.callAndForget(new ServerUpdatedEvent(wrapper));
    }
}
