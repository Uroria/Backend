package com.uroria.backend.impl;

import java.io.Serializable;

public interface UpdateManager {

    void updateObject(Serializable identifier, int channel, Object object);

    void updateString(Serializable identifier, int channel, String string);

    void updateInt(Serializable identifier, int channel, int i);

    void updateLong(Serializable identifier, int channel, long l);

    void updateBoolean(Serializable identifier, int channel, boolean bool);

    void updateFloat(Serializable identifier, int channel, float f);

    void updateDouble(Serializable identifier, int channel, double d);
}
