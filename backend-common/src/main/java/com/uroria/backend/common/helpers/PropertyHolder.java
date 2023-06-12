package com.uroria.backend.common.helpers;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class PropertyHolder implements Serializable {
    @Serial private static final long serialVersionUID = 1;
    protected final Map<String, Object> properties;
    public PropertyHolder() {
        this.properties = new HashMap<>();
    }

    public void setProperty(String key, String value) {
        this.properties.put(key, value);
    }

    public void setProperty(String key, boolean value) {
        this.properties.put(key, value);
    }

    public void setProperty(String key, Integer value) {
        this.properties.put(key, value);
    }

    public void setProperty(String key, long value) {
        this.properties.put(key, value);
    }

    public void unsetProperty(String key) {
        this.properties.remove(key);
    }

    public Optional<String> getPropertyString(String key) {
        Object o = this.properties.get(key);
        if (o == null) return Optional.empty();
        return Optional.of((String) o);
    }

    public Optional<Integer> getPropertyInt(String key) {
        Object o = this.properties.getOrDefault(key, 0);
        if (o == null) return Optional.empty();
        if (o instanceof Integer i) {
            return Optional.of(i);
        }
        if (o instanceof Float f) {
            int i = f.intValue();
            return Optional.of(i);
        }
        if (o instanceof Short s) {
            int i = s;
            return Optional.of(i);
        }
        return Optional.empty();
    }

    public boolean getPropertyBoolean(String key) {
        return (boolean) this.properties.getOrDefault(key, false);
    }

    public Optional<Long> getPropertyLong(String key) {
        Object o = this.properties.get(key);
        if (o == null) return Optional.empty();
        if (o instanceof Long l) {
            return Optional.of(l);
        }
        if (o instanceof Integer i) {
            long l = i;
            return Optional.of(l);
        }
        if (o instanceof Double d) {
            long l = d.longValue();
            return Optional.of(l);
        }
        if (o instanceof Float f) {
            long l = f.longValue();
            return Optional.of(l);
        }
        if (o instanceof Short s) {
            long l = s;
            return Optional.of(l);
        }
        return Optional.empty();
    }

    public Map<String, Object> getProperties() {
        return new HashMap<>(this.properties);
    }

    protected void setProperties(Map<String, Object> properties) {
        this.properties.putAll(properties);
    }
}
