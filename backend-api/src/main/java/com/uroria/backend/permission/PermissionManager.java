package com.uroria.backend.permission;

import com.uroria.backend.AbstractManager;
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

    abstract protected void checkPermissionHolder(PermissionHolder holder);

    abstract protected void checkPermissionGroup(PermissionGroup group);

    /**
     * Gets the permissionholder from backend. If not registered a new one gets created and registered.
     * @param timeout Request timeout in ms
     * @throws NullPointerException If UUID is null
     */
    abstract public Optional<PermissionHolder> getPermissionHolder(UUID uuid, int timeout);

    /**
     * @param timeout Request timeout in ms
     * @throws NullPointerException If name is null
     */
    abstract public Optional<PermissionGroup> getPermissionGroup(String name, int timeout);

    /**
     * @throws NullPointerException If PermissionHolder is null
     */
    abstract public void updatePermissionHolder(PermissionHolder permissionHolder);

    /**
     * @throws NullPointerException If PermissionGroup is null
     */
    abstract public void updatePermissionGroup(PermissionGroup permissionGroup);
}
