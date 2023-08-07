package com.uroria.backend.service.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;

import java.io.IOException;
import java.util.Map;

public final class Object2ObjectMapTypeAdapterFactory implements TypeAdapterFactory {

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (!Object2ObjectMap.class.isAssignableFrom(type.getRawType())) return null;
        return (TypeAdapter<T>) new MapTypeAdapter(gson.getDelegateAdapter(this, type));
    }

    private static class MapTypeAdapter<K, V> extends TypeAdapter<Object2ObjectMap<K, V>> {
        private final TypeAdapter<Object2ObjectMap<K, V>> delegate;

        private MapTypeAdapter(TypeAdapter<Object2ObjectMap<K, V>> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void write(JsonWriter jsonWriter, Object2ObjectMap<K, V> kvMap) throws IOException {
            delegate.write(jsonWriter, kvMap);
        }

        @Override
        public Object2ObjectMap<K, V> read(JsonReader in) throws IOException {
            return new Object2ObjectArrayMap<>((Map<? extends K, ? extends V>) delegate.read(in));
        }
    }
}
