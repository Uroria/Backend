package com.uroria.backend.impl.server.group;

import com.uroria.backend.cache.BackendObject;
import com.uroria.backend.cache.Wrapper;
import com.uroria.backend.cache.WrapperManager;
import com.uroria.backend.cache.communication.server.group.GetAllServersGroupsRequest;
import com.uroria.backend.cache.communication.server.group.GetAllServersGroupsResponse;
import com.uroria.backend.cache.communication.server.group.GetServerGroupRequest;
import com.uroria.backend.cache.communication.server.group.GetServerGroupResponse;
import com.uroria.backend.communication.Communicator;
import com.uroria.backend.communication.request.Requester;
import com.uroria.backend.impl.server.ServerManager;
import com.uroria.backend.server.ServerGroup;
import com.uroria.backend.server.events.ServerGroupDeletedEvent;
import com.uroria.backend.server.events.ServerGroupUpdatedEvent;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public final class ServerGroupManager extends WrapperManager<ServerGroupWrapper> {
    private static final Logger logger = LoggerFactory.getLogger("ServerGroups");

    private final ServerManager serverManager;
    private final Requester<GetServerGroupRequest, GetServerGroupResponse> nameCheck;
    private final Requester<GetAllServersGroupsRequest, GetAllServersGroupsResponse> allGet;

    public ServerGroupManager(ServerManager serverManager, Communicator communicator) {
        super(logger, communicator, "server_group", "server_group", "server_group");
        this.serverManager = serverManager;
        this.nameCheck = requestPoint.registerRequester(GetServerGroupRequest.class, GetServerGroupResponse.class, "CheckName");
        this.allGet = requestPoint.registerRequester(GetAllServersGroupsRequest.class, GetAllServersGroupsResponse.class, "GetAll");
    }

    public Collection<ServerGroup> getAll() {
        Result<GetAllServersGroupsResponse> result = this.allGet.request(new GetAllServersGroupsRequest(true), 5000);
        GetAllServersGroupsResponse response = result.get();
        if (response == null) return ObjectSets.emptySet();
        ObjectSet<ServerGroup> groups = new ObjectArraySet<>();
        response.getGroups().forEach(name -> {
            ServerGroupWrapper wrapper = getServerGroupWrapper(name);
            if (wrapper == null) {
                logger.error("Cannot get server-group with name " + name + " while getting all");
                return;
            }
            groups.add(wrapper);
        });
        return groups;
    }

    public ServerGroupWrapper getServerGroupWrapper(String name) {
        for (ServerGroupWrapper wrapper : this.wrappers) {
            if (wrapper.getName().equals(name)) return wrapper;
        }

        Result<GetServerGroupResponse> result = this.nameCheck.request(new GetServerGroupRequest(name, false), 2000);
        GetServerGroupResponse response = result.get();
        if (response == null) return null;
        if (!response.isExistent()) return null;
        ServerGroupWrapper wrapper = new ServerGroupWrapper(this, this.serverManager, name);
        this.wrappers.add(wrapper);
        return wrapper;
    }

    public ServerGroupWrapper createServerGroupWrapper(String name, int maxPlayers) {
        Result<GetServerGroupResponse> result = this.nameCheck.request(new GetServerGroupRequest(name, true), 2000);
        GetServerGroupResponse response = result.get();
        if (response == null) return null;
        if (!response.isExistent()) return null;
        ServerGroupWrapper wrapper = new ServerGroupWrapper(this, this.serverManager, name);
        BackendObject<? extends Wrapper> object = wrapper.getBackendObject();
        object.set("name", name);
        object.set("maxPlayerCount", maxPlayers);
        this.wrappers.add(wrapper);
        return wrapper;
    }

    @Override
    protected void onUpdate(ServerGroupWrapper wrapper) {
        if (wrapper.isDeleted()) eventManager.callAndForget(new ServerGroupDeletedEvent(wrapper));
        else eventManager.callAndForget(new ServerGroupUpdatedEvent(wrapper));
    }
}
