package com.uroria.backend.impl.permission;

import com.uroria.backend.impl.AbstractManager;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.PulsarClient;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPermManager extends AbstractManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("Perms");

    protected final ObjectSet<PermGroupWrapper> groups;

    public AbstractPermManager(PulsarClient pulsarClient, @Nullable CryptoKeyReader cryptoKeyReader) {
        super(pulsarClient, LOGGER, "perm/request", "perm/update", cryptoKeyReader);
        this.groups = new ObjectArraySet<>();
    }

    @Override
    public final void checkObject(Object identifier, int channel, Object object) {

    }

    @Override
    public final void checkString(Object identifier, int channel, @Nullable String value) {

    }

    @Override
    public final void checkInt(Object identifier, int channel, int value) {

    }

    @Override
    public final void checkLong(Object identifier, int channel, long value) {

    }

    @Override
    public final void checkBoolean(Object identifier, int channel, boolean value) {

    }

    @Override
    public final void checkFloat(Object identifier, int channel, float value) {

    }

    @Override
    public final void checkDouble(Object identifier, int channel, double value) {

    }

    public @Nullable final PermGroupWrapper getWrapper(String name) {

    }
}
