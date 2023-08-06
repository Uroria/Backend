package com.uroria.backend.property;

import com.uroria.backend.BackendObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import lombok.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public abstract class PropertyObject<T> extends BackendObject<T> implements Serializable {
    @Serial private static final long serialVersionUID = 1;

    protected final Object2ObjectMap<String, Object> properties;

    public PropertyObject() {
        this.properties = new Object2ObjectArrayMap<>();
    }

    public final void setProperty(@NonNull String key, @NonNull String value) {
        this.properties.put(key, value);
    }

    public final void setProperty(@NonNull String key, boolean value) {
        this.properties.put(key, value);
    }

    public final void setProperty(@NonNull String key, int value) {
        this.properties.put(key, value);
    }

    public final void setProperty(@NonNull String key, long value) {
        this.properties.put(key, value);
    }

    public final void unsetProperty(@NonNull String key) {
        this.properties.remove(key);
    }

    public final Optional<String> getPropertyString(@NonNull String key) {
        Object o = this.properties.get(key);
        if (o == null) return Optional.empty();
        return Optional.of((String) o);
    }

    public final Optional<Integer> getPropertyInt(@NonNull String key) {
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

    public final boolean getPropertyBoolean(@NonNull String key) {
        return (boolean) this.properties.getOrDefault(key, false);
    }

    public final Optional<Long> getPropertyLong(@NonNull String key) {
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

    public final Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(this.properties);
    }

    protected final void setProperties(@NonNull Map<String, Object> properties) {
        this.properties.putAll(properties);
    }
}
