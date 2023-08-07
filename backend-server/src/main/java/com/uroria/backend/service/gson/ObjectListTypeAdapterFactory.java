package com.uroria.backend.service.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.io.IOException;
import java.util.List;

public final class ObjectListTypeAdapterFactory implements TypeAdapterFactory {

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (!ObjectList.class.isAssignableFrom(type.getRawType())) return null;
        return (TypeAdapter<T>) new ListTypeAdapter(gson.getDelegateAdapter(this, type));
    }

    private static class ListTypeAdapter<K> extends TypeAdapter<ObjectList<K>> {
        private final TypeAdapter<ObjectList<K>> delegate;

        private ListTypeAdapter(TypeAdapter<ObjectList<K>> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void write(JsonWriter jsonWriter, ObjectList<K> kvMap) throws IOException {
            delegate.write(jsonWriter, kvMap);
        }

        @Override
        public ObjectList<K> read(JsonReader in) throws IOException {
            return new ObjectArrayList<>((List<K>) delegate.read(in));
        }
    }
}