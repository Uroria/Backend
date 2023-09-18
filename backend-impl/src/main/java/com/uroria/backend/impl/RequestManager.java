package com.uroria.backend.impl;

import java.io.Serializable;

public interface RequestManager {
    Object getObject(Serializable identifier, int channel, Object defVal);
    
    String getString(Serializable identifier, int channel, String defVal);
    
    int getInt(Serializable identifier, int channel, int defVal);
    
    long getLong(Serializable identifier, int channel, int defVal);
    
    boolean getBoolean(Serializable identifier, int channel, boolean defVal);
    
    float getFloat(Serializable identifier, int channel, float defVal);
    
    double getDouble(Serializable identifier, int channel, double defVal);    
}
