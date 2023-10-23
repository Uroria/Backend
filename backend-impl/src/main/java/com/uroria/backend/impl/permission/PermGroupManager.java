package com.uroria.backend.impl.permission;

import com.uroria.backend.cache.WrapperManager;
import com.uroria.backend.cache.communication.permgroup.GetGroupRequest;
import com.uroria.backend.cache.communication.permgroup.GetGroupResponse;
import com.uroria.backend.communication.Communicator;
import com.uroria.backend.communication.request.Requester;
import com.uroria.backend.permission.events.GroupDeletedEvent;
import com.uroria.backend.permission.events.GroupUpdatedEvent;
import com.uroria.problemo.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PermGroupManager extends WrapperManager<GroupWrapper> {
    private static final Logger logger = LoggerFactory.getLogger("PermGroups");

    private final Requester<GetGroupRequest, GetGroupResponse> nameCheck;

    public PermGroupManager(Communicator communicator) {
        super(logger, communicator, "perm_groups", "perm_groups", "perm_groups");
        this.nameCheck = requestPoint.registerRequester(GetGroupRequest.class, GetGroupResponse.class, "CheckName");
    }

    public GroupWrapper getGroupWrapper(String name) {
        for (GroupWrapper wrapper : this.wrappers) {
            if (wrapper.getName().equals(name)) return wrapper;
        }

        Result<GetGroupResponse> result = this.nameCheck.request(new GetGroupRequest(name, false), 2000);
        GetGroupResponse response = result.get();
        if (response == null) return null;
        if (!response.isExistent()) return null;
        GroupWrapper wrapper = new GroupWrapper(this, name);
        this.wrappers.add(wrapper);
        return wrapper;
    }

    public GroupWrapper createGroupWrapper(String name, int priority) {
        for (GroupWrapper wrapper : this.wrappers) {
            if (wrapper.getName().equals(name)) return wrapper;
        }

        Result<GetGroupResponse> result = this.nameCheck.request(new GetGroupRequest(name, true), 5000);
        GetGroupResponse response = result.get();
        if (response == null) return null;
        if (!(response.isExistent())) return null;
        GroupWrapper wrapper = new GroupWrapper(this, name);
        wrapper.setPriority(priority);
        this.wrappers.add(wrapper);
        return wrapper;
    }

    @Override
    protected void onUpdate(GroupWrapper wrapper) {
        if (wrapper.isDeleted()) this.eventManager.callAndForget(new GroupDeletedEvent(wrapper));
        else this.eventManager.callAndForget(new GroupUpdatedEvent(wrapper));
    }
}
