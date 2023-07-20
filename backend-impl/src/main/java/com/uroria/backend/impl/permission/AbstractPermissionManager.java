package com.uroria.backend.impl.permission;

import com.uroria.backend.impl.AbstractManager;
import com.uroria.backend.permission.PermissionGroup;
import com.uroria.backend.permission.PermissionHolder;
import com.uroria.backend.permission.PermissionManager;
import com.uroria.backend.utils.ObjectUtils;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public abstract class AbstractPermissionManager extends AbstractManager implements PermissionManager {
    protected final Set<PermissionHolder> holders;
    protected final Set<PermissionGroup> groups;
    public AbstractPermissionManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient, logger);
        this.holders = ObjectUtils.newSet();
        this.groups = ObjectUtils.newSet();
    }

    abstract protected void checkPermissionHolder(PermissionHolder holder);

    abstract protected void checkPermissionGroup(PermissionGroup group);

    @Override
    public Optional<PermissionHolder> getHolder(@NonNull UUID uuid) {
        return getHolder(uuid, 3000);
    }

    @Override
    public Optional<PermissionGroup> getGroup(@NonNull String name) {
        return getGroup(name, 3000);
    }
}
