package com.uroria.backend.message;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import lombok.Getter;
import lombok.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;

public final class Message implements Serializable {
    @Serial private static final long serialVersionUID = 1;

    private @Getter final String channel;
    private final Object2ObjectMap<String, Object> mapping;

    public Message(@NonNull String channel, @NonNull Object2ObjectMap<String, Object> mapping) {
        this.channel = channel;
        this.mapping = new Object2ObjectArrayMap<>(mapping);
    }

    public void set(@NonNull String key, @NonNull Serializable object) {
        this.mapping.put(key, object);
    }

    public <T> Optional<T> get(String key, Class<T> tClass) {
        if (key == null) return Optional.empty();
        if (tClass == null) return Optional.empty();
        return Optional.ofNullable((T) this.mapping.get(key));
    }

    public Object2ObjectMap<String, Object> getData() {
        return Object2ObjectMaps.unmodifiable(this.mapping);
    }
}
