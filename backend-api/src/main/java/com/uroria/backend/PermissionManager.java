package com.uroria.backend;

import com.uroria.backend.common.PermissionGroup;
import com.uroria.backend.common.PermissionHolder;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class PermissionManager extends AbstractManager {
    protected final Logger logger;
    protected final Collection<PermissionHolder> holders;
    protected final Collection<PermissionGroup> groups;
    public PermissionManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient);
        this.logger = logger;
        this.holders = new CopyOnWriteArrayList<>();
        this.groups = new CopyOnWriteArrayList<>();
    }


    @Override
    abstract protected void start(String identifier);

    @Override
    abstract protected void shutdown();

    abstract public Optional<PermissionHolder> getPermissionHolder(UUID uuid);

    abstract public Optional<PermissionGroup> getPermissionGroup(String name);

    abstract public void updatePermissionHolder(PermissionHolder permissionHolder);

    abstract public void updatePermissionGroup(PermissionGroup permissionGroup);
}
