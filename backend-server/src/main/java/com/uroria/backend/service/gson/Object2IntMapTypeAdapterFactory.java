package com.uroria.backend.service.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.io.IOException;
import java.util.Map;

public final class Object2IntMapTypeAdapterFactory implements TypeAdapterFactory {

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (!Object2IntMap.class.isAssignableFrom(type.getRawType())) return null;
        return (TypeAdapter<T>) new MapTypeAdapter(gson.getDelegateAdapter(this, type));
    }

    private static class MapTypeAdapter<K> extends TypeAdapter<Object2IntMap<K>> {
        private final TypeAdapter<Object2IntMap<K>> delegate;

        private MapTypeAdapter(TypeAdapter<Object2IntMap<K>> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void write(JsonWriter jsonWriter, Object2IntMap<K> kvMap) throws IOException {
            delegate.write(jsonWriter, kvMap);
        }

        @Override
        public Object2IntMap<K> read(JsonReader in) throws IOException {
            return new Object2IntArrayMap<>((Map<? extends K, Integer>) delegate.read(in));
        }
    }
}