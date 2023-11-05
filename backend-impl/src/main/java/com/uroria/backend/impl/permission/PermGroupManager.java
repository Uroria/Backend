package com.uroria.backend.impl.permission;

import com.uroria.backend.cache.BackendObject;
import com.uroria.backend.cache.Wrapper;
import com.uroria.backend.cache.communication.controls.UnavailableException;
import com.uroria.backend.cache.communication.permgroup.GetAllGroupRequest;
import com.uroria.backend.cache.communication.permgroup.GetAllGroupResponse;
import com.uroria.backend.cache.communication.permgroup.GetGroupRequest;
import com.uroria.backend.cache.communication.permgroup.GetGroupResponse;
import com.uroria.backend.communication.request.Requester;
import com.uroria.backend.impl.BackendWrapperImpl;
import com.uroria.backend.impl.StatedManager;
import com.uroria.backend.permission.PermGroup;
import com.uroria.backend.permission.events.GroupDeletedEvent;
import com.uroria.backend.permission.events.GroupUpdatedEvent;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public final class PermGroupManager extends StatedManager<GroupWrapper> {
    private static final Logger logger = LoggerFactory.getLogger("PermGroups");

    private final Requester<GetGroupRequest, GetGroupResponse> nameCheck;
    private final Requester<GetAllGroupRequest, GetAllGroupResponse> allGet;

    public PermGroupManager(BackendWrapperImpl wrapper) {
        super(logger, wrapper, "perm_group");
        this.nameCheck = requestPoint.registerRequester(GetGroupRequest.class, GetGroupResponse.class, "CheckName");
        this.allGet = requestPoint.registerRequester(GetAllGroupRequest.class, GetAllGroupResponse.class, "GetAll");
    }

    public Collection<PermGroup> getAll() {
        if (!wrapper.isAvailable()) throw new UnavailableException();
        Result<GetAllGroupResponse> result = this.allGet.request(new GetAllGroupRequest(true), 5000);
        GetAllGroupResponse response = result.get();
        if (response == null) {
            logger.warn("Response of getting all perm-groups is null");
            return ObjectSets.emptySet();
        }
        ObjectSet<PermGroup> wrappers = new ObjectArraySet<>();
        response.getGroups().forEach(name -> {
            GroupWrapper wrapper = getGroupWrapper(name);
            if (wrapper == null) {
                logger.error("Cannot get group-wrapper with name " + name + " while getting all");
                return;
            }
            wrappers.add(wrapper);
        });
        return wrappers;
    }

    public GroupWrapper getGroupWrapper(String name) {
        for (GroupWrapper wrapper : this.wrappers) {
            if (wrapper.getName().equals(name)) return wrapper;
        }

        if (!wrapper.isAvailable()) throw new UnavailableException();

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

        if (!wrapper.isAvailable()) throw new UnavailableException();

        Result<GetGroupResponse> result = this.nameCheck.request(new GetGroupRequest(name, true), 5000);
        GetGroupResponse response = result.get();
        if (response == null) return null;
        if (!(response.isExistent())) return null;
        GroupWrapper wrapper = new GroupWrapper(this, name);
        BackendObject<? extends Wrapper> object = wrapper.getBackendObject();
        object.set("name", name);
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
