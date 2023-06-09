package com.uroria.backend.pluginapi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public final class BackendRegistry {
    private static final Collection<Object> OBJECTS;
    static {
        OBJECTS = new ArrayList<>();
    }

    public static <T>Optional<T> get(Class<T> clazz) {
        for (Object object : OBJECTS) {
            if (clazz.equals(object.getClass())) return Optional.of((T) object);
        }
        return Optional.empty();
    }

    public static void register(Object object) {
        OBJECTS.add(object);
    }
}
