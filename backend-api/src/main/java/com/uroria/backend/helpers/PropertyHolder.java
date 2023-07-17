package com.uroria.backend.helpers;

import com.uroria.backend.BackendObject;
import lombok.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public abstract class PropertyHolder<T> extends BackendObject<T> implements Serializable {
    @Serial private static final long serialVersionUID = 1;
    protected final Map<String, Object> properties;
    public PropertyHolder() {
        this.properties = new ConcurrentHashMap<>();
    }

    public void setProperty(@NonNull String key, @NonNull String value) {
        this.properties.put(key, value);
    }

    public void setProperty(@NonNull String key, boolean value) {
        this.properties.put(key, value);
    }

    public void setProperty(@NonNull String key, int value) {
        this.properties.put(key, value);
    }

    public void setProperty(@NonNull String key, long value) {
        this.properties.put(key, value);
    }

    public void unsetProperty(@NonNull String key) {
        this.properties.remove(key);
    }

    public Optional<String> getPropertyString(@NonNull String key) {
        Object o = this.properties.get(key);
        if (o == null) return Optional.empty();
        return Optional.of((String) o);
    }

    public Optional<Integer> getPropertyInt(@NonNull String key) {
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

    public boolean getPropertyBoolean(@NonNull String key) {
        return (boolean) this.properties.getOrDefault(key, false);
    }

    public Optional<Long> getPropertyLong(@NonNull String key) {
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

    protected void setProperties(@NonNull Map<String, Object> properties) {
        this.properties.putAll(properties);
    }
}
