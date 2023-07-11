package com.uroria.backend.permission;

import com.uroria.backend.AbstractManager;
import com.uroria.backend.common.permission.PermissionGroup;
import com.uroria.backend.common.permission.PermissionHolder;
import com.uroria.backend.common.permission.PermissionManager;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractPermissionManager extends AbstractManager implements PermissionManager {
    protected final Logger logger;
    protected final Collection<PermissionHolder> holders;
    protected final Collection<PermissionGroup> groups;
    public AbstractPermissionManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient);
        this.logger = logger;
        this.holders = new CopyOnWriteArrayList<>();
        this.groups = new CopyOnWriteArrayList<>();
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
