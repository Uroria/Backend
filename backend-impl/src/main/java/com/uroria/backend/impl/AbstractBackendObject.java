package com.uroria.backend.impl;

import org.jetbrains.annotations.Nullable;

public abstract class AbstractBackendObject {

    public abstract Object getObject(int channel, Object defVal);

    public abstract String getString(int channel, String defVal);

    public abstract int getInt(int channel, int defVal);

    public abstract long getLong(int channel, int defVal);

    public abstract boolean getBoolean(int channel, boolean defVal);

    public abstract float getFloat(int channel, float defVal);

    public abstract double getDouble(int channel, double defVal);

    public abstract void checkObject(int channel, @Nullable Object value);

    public abstract void checkString(int channel, @Nullable String value);

    public abstract void checkInt(int channel, int value);

    public abstract void checkLong(int channel, long value);

    public abstract void checkBoolean(int channel, boolean value);

    public abstract void checkFloat(int channel, float value);

    public abstract void checkDouble(int channel, double value);

    public abstract void updateObject(int channel, @Nullable Object value);

    public abstract void updateString(int channel, @Nullable String value);

    public abstract void updateInt(int channel, int value);

    public abstract void updateLong(int channel, long value);

    public abstract void updateBoolean(int channel, boolean value);

    public abstract void updateFloat(int channel, float value);

    public abstract void updateDouble(int channel, double value);
}
