package com.uroria.backend.impl.permission;

import com.uroria.backend.impl.AbstractManager;
import com.uroria.backend.permission.PermGroup;
import com.uroria.backend.permission.PermHolder;
import com.uroria.backend.permission.PermManager;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

public abstract class AbstractPermManager extends AbstractManager implements PermManager {
    protected final ObjectArraySet<PermGroup> groups;
    protected final ObjectArraySet<PermHolder> holders;

    public AbstractPermManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient, logger);
        this.groups = new ObjectArraySet<>();
        this.holders = new ObjectArraySet<>();
    }

    abstract protected void checkGroup(@NonNull PermGroup group);

    abstract protected void checkHolder(@NonNull PermHolder holder);
}
