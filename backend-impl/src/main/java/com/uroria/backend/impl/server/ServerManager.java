package com.uroria.backend.impl.server;

import com.uroria.backend.cache.BackendObject;
import com.uroria.backend.cache.Wrapper;
import com.uroria.backend.cache.WrapperManager;
import com.uroria.backend.cache.communication.server.GetServerRequest;
import com.uroria.backend.cache.communication.server.GetServerResponse;
import com.uroria.backend.communication.Communicator;
import com.uroria.backend.communication.request.Requester;
import com.uroria.backend.server.events.ServerDeletedEvent;
import com.uroria.backend.server.events.ServerUpdatedEvent;
import com.uroria.problemo.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public final class ServerManager extends WrapperManager<ServerWrapper> {
    private static final Logger logger = LoggerFactory.getLogger("Servers");

    private final Requester<GetServerRequest, GetServerResponse> idCheck;

    public ServerManager(Communicator communicator) {
        super(logger, communicator, "servers", "servers", "servers");
        this.idCheck = requestPoint.registerRequester(GetServerRequest.class, GetServerResponse.class, "CheckId");
    }

    public ServerWrapper getServerWrapper(long id) {
        for (ServerWrapper wrapper : this.wrappers) {
            if (wrapper.getId() == id) return wrapper;
        }

        Result<GetServerResponse> result = this.idCheck.request(new GetServerRequest(id, false), 2000);
        GetServerResponse response = result.get();
        if (response == null) return null;
        if (!response.isExistent()) return null;
        ServerWrapper wrapper = new ServerWrapper(this, id);
        this.wrappers.add(wrapper);
        return wrapper;
    }

    public ServerWrapper createServerWrapper(long groupId, int templateId) {
        long id = System.currentTimeMillis() + new Random().nextLong(10000) - groupId - templateId;
        Result<GetServerResponse> result = this.idCheck.request(new GetServerRequest(id, true), 2000);
        GetServerResponse response = result.get();
        if (response == null) return null;
        if (!response.isExistent()) return null;
        ServerWrapper wrapper = new ServerWrapper(this, id);
        BackendObject<? extends Wrapper> object = wrapper.getBackendObject();
        object.set("templateId", templateId);
        object.set("group", groupId);
        this.wrappers.add(wrapper);
        return wrapper;
    }

    @Override
    protected void onUpdate(ServerWrapper wrapper) {
        if (wrapper.isDeleted()) this.eventManager.callAndForget(new ServerDeletedEvent(wrapper));
        else this.eventManager.callAndForget(new ServerUpdatedEvent(wrapper));
    }
}
