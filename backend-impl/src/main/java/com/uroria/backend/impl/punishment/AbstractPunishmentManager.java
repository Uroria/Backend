package com.uroria.backend.impl.punishment;

import com.uroria.backend.impl.AbstractManager;
import com.uroria.backend.punishment.Punished;
import com.uroria.backend.punishment.PunishmentManager;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

public abstract class AbstractPunishmentManager extends AbstractManager implements PunishmentManager {
    protected final ObjectArraySet<Punished> punisheds;

    public AbstractPunishmentManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient, logger);
        this.punisheds = new ObjectArraySet<>();
    }

    abstract protected void checkPunished(@NonNull Punished punished);
}
