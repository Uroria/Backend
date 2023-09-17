package com.uroria.backend.impl;

import org.jetbrains.annotations.Nullable;

public abstract class AbstractBackendObject {

    public abstract Object getObject(int key, Object defVal);

    public abstract String getString(int key, String defVal);

    public abstract int getInt(int key, int defVal);

    public abstract long getLong(int key, int defVal);

    public abstract boolean getBoolean(int key, boolean defVal);

    public abstract float getFloat(int key, float defVal);

    public abstract double getDouble(int key, double defVal);

    public abstract void checkObject(int key, @Nullable Object value);

    public abstract void checkString(int key, @Nullable String value);

    public abstract void checkInt(int key, int value);

    public abstract void checkLong(int key, int value);

    public abstract void checkBoolean(int key, boolean value);

    public abstract void checkFloat(int key, float value);

    public abstract void checkDouble(int key, double value);

    public abstract void updateObject(int key, @Nullable Object value);

    public abstract void updateString(int key, @Nullable String value);

    public abstract int updateInt(int key, int value);

    public abstract long updateLong(int key, long value);

    public abstract boolean updateBoolean(int key, boolean value);

    public abstract float updateFloat(int key, float value);

    public abstract double updateDouble(int key, double value);
}
