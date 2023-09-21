package com.uroria.backend.impl.permission;

import com.uroria.backend.impl.AbstractBackendObject;
import com.uroria.backend.impl.permission.impl.PermissionImpl;
import com.uroria.backend.permission.PermGroup;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractPermGroup extends AbstractBackendObject implements PermGroup {
    protected final AbstractPermManager permManager;

    protected final String name;
    protected int priority;
    protected ObjectSet<PermissionImpl> permissions;

    AbstractPermGroup(AbstractPermManager permManager, String name) {
        this.permManager = permManager;
        this.name = name;
    }

    @Override
    public Object getObject(int channel, Object defVal) {
        switch (channel) {
            case 1 -> {

            }
        }
        return defVal;
    }

    @Override
    public String getString(int channel, String defVal) {
        return null;
    }

    @Override
    public int getInt(int channel, int defVal) {
        return 0;
    }

    @Override
    public long getLong(int channel, int defVal) {
        return 0;
    }

    @Override
    public boolean getBoolean(int channel, boolean defVal) {
        return false;
    }

    @Override
    public float getFloat(int channel, float defVal) {
        return 0;
    }

    @Override
    public double getDouble(int channel, double defVal) {
        return 0;
    }

    @Override
    public void checkObject(int channel, @Nullable Object value) {

    }

    @Override
    public void checkString(int channel, @Nullable String value) {

    }

    @Override
    public void checkInt(int channel, int value) {

    }

    @Override
    public void checkLong(int channel, long value) {

    }

    @Override
    public void checkBoolean(int channel, boolean value) {

    }

    @Override
    public void checkFloat(int channel, float value) {

    }

    @Override
    public void checkDouble(int channel, double value) {

    }

    @Override
    public void updateObject(int channel, @Nullable Object value) {

    }

    @Override
    public void updateString(int channel, @Nullable String value) {

    }

    @Override
    public void updateInt(int channel, int value) {

    }

    @Override
    public void updateLong(int channel, long value) {

    }

    @Override
    public void updateBoolean(int channel, boolean value) {

    }

    @Override
    public void updateFloat(int channel, float value) {

    }

    @Override
    public void updateDouble(int channel, double value) {

    }
}
