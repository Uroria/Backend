package com.uroria.backend.impl.permission;

import com.rabbitmq.client.Connection;
import com.uroria.backend.impl.communication.request.RabbitRequestChannel;
import com.uroria.backend.impl.communication.request.RequestChannel;
import com.uroria.backend.impl.io.BackendOutputStream;
import com.uroria.backend.impl.wrapper.WrapperManager;
import com.uroria.backend.permission.PermGroup;
import com.uroria.backend.permission.events.GroupDeletedEvent;
import com.uroria.backend.permission.events.GroupUpdatedEvent;
import com.uroria.base.io.InsaneByteArrayInputStream;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public final class PermGroupManager extends WrapperManager<GroupWrapper> {
    private final RequestChannel requestAll;

    public PermGroupManager(Connection rabbit) {
        super(rabbit, LoggerFactory.getLogger("PermGroups"), "permgroup", "name");
        this.requestAll = new RabbitRequestChannel(rabbit, "permgroup-requestall");
    }

    @Override
    protected void onUpdate(GroupWrapper wrapper) {
        if (wrapper.isDeleted()) {
            eventManager.callAndForget(new GroupDeletedEvent(wrapper));
        } else wrapper.refreshPermissions();
        eventManager.callAndForget(new GroupUpdatedEvent(wrapper));
    }

    public Collection<PermGroup> getGroups() {
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
                    .map(name -> (PermGroup) getGroup(name))
                    .toList();
        } catch (Exception exception) {
            logger.error("Cannot read get all perm-groups response", exception);
            return ObjectSets.emptySet();
        }
    }

    public GroupWrapper getGroup(String name) {
        return getWrapper(name, false);
    }

    public GroupWrapper createGroup(String name) {
        return getWrapper(name, true);
    }

    @Override
    protected GroupWrapper createWrapper(String identifier) {
        return new GroupWrapper(this.client, identifier);
    }
}
