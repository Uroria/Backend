package com.uroria.backend.cache;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.uroria.backend.cache.communication.PartRequest;
import com.uroria.backend.cache.communication.PartResponse;
import com.uroria.backend.cache.communication.UpdateBroadcast;
import com.uroria.backend.cache.utils.GsonUtils;
import com.uroria.backend.communication.Communicator;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public final class BackendObject<T extends Wrapper> {
    private final Logger logger;
    private final Wrapper wrapper;
    private final WrapperManager<? extends Wrapper> wrapperManager;
    private final ObjectSet<String> cachedKeys;
    private final Object2ObjectMap<String, String> strings;
    private final Object2ObjectMap<String, Number> numbers;
    private final Object2BooleanMap<String> booleans;
    private final Object2ObjectMap<String, ObjectSet<?>> sets;
    private final Object2ObjectMap<String, Object2ObjectMap<String, ?>> maps;

    public BackendObject(T wrapper) {
        this.logger = wrapper.getWrapperManager().logger;
        this.wrapper = wrapper;
        this.wrapperManager = wrapper.getWrapperManager();
        this.cachedKeys = new ObjectArraySet<>();
        this.strings = new Object2ObjectArrayMap<>();
        this.numbers = new Object2ObjectArrayMap<>();
        this.booleans = new Object2BooleanArrayMap<>();
        this.sets = new Object2ObjectArrayMap<>();
        this.maps = new Object2ObjectArrayMap<>();
    }

    private JsonElement getElement(String key) {
        if (wrapper.isDeleted()) {
            return null;
        }
        Result<JsonElement> result = request(key);
        JsonElement element = result.get();
        this.cachedKeys.add(key);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        return element;
    }

    public Result<JsonElement> request(@NonNull String key) {
        PartRequest request = new PartRequest(wrapper.getIdentifier(), key);
        Result<PartResponse> result = this.wrapperManager.partRequester.request(request, 5000);
        if (result instanceof Result.Problematic<PartResponse> problematic) {
            this.logger.error("Cannot request " + key + " for wrapper", problematic.getProblem().getError()
                    .orElse(new RuntimeException("Unknown exception.")));
            return Result.problem(problematic.getProblem());
        }
        PartResponse response = result.get();
        if (response == null) return Result.none();
        return Result.some(response.getValue());
    }

    @SuppressWarnings("unchecked")
    public <O> ObjectSet<O> getSet(@NonNull String key, Class<O> tClass) {
        try {
            if (cachedKeys.contains(key)) return new ObjectArraySet<>((ObjectSet<O>) sets.get(key));
            JsonElement element = getElement(key);
            if (element == null) return ObjectSets.emptySet();
            JsonArray array = element.getAsJsonArray();
            ObjectSet<Object> set = GsonUtils.toSet(array);
            this.sets.put(key, set);
            return new ObjectArraySet<>((ObjectSet<O>) set);
        } catch (Exception exception) {
            this.logger.error("Cannot get set " + key + " with type " + tClass.getName(), exception);
            return ObjectSets.emptySet();
        }
    }

    @SuppressWarnings("unchecked")
    public <V> Object2ObjectMap<String, V> getMap(String key, Class<V> valueClass) {
        try {
            if (this.cachedKeys.contains(key)) return new Object2ObjectArrayMap<>(((Object2ObjectMap<String, V>) this.maps.get(key)));
            JsonElement element = getElement(key);
            if (element == null) return Object2ObjectMaps.emptyMap();
            Object2ObjectMap<String, V> map = new Object2ObjectArrayMap<>();
            JsonObject jsonObject = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                String valueKey = entry.getKey();
                JsonElement value = entry.getValue();
                map.put(valueKey, (V) GsonUtils.toObject(value));
            }
            this.maps.put(key, map);
            return map;
        } catch (Exception exception) {
            this.logger.error("Cannot get map " + key + " with value-type " + valueClass.getName(), exception);
            return Object2ObjectMaps.emptyMap();
        }
    }

    public ObjectSet<?> getSet(@NonNull String key) {
        try {
            if (cachedKeys.contains(key)) return sets.get(key);
            JsonElement element = getElement(key);
            if (element == null) return ObjectSets.emptySet();
            JsonArray array = element.getAsJsonArray();
            ObjectSet<Object> set = GsonUtils.toSet(array);
            this.sets.put(key, set);
            return  set;
        } catch (Exception exception) {
            this.logger.error("Cannot get set " + key, exception);
            return ObjectSets.emptySet();
        }
    }

    public @Nullable String getString(@NonNull String key) {
        if (cachedKeys.contains(key)) return strings.get(key);
        JsonElement element = getElement(key);
        if (element == null) return null;
        String string = element.getAsString();
        this.strings.put(key, string);
        return string;
    }

    public @Nullable Long getLong(@NonNull String key) {
        if (cachedKeys.contains(key)) return (Long) numbers.get(key);
        JsonElement element = getElement(key);
        if (element == null) return null;
        long aLong = element.getAsLong();
        this.numbers.put(key, aLong);
        return aLong;
    }

    public @Nullable Integer getInt(@NonNull String key) {
        if (cachedKeys.contains(key)) return (Integer) numbers.get(key);
        JsonElement element = getElement(key);
        if (element == null) return null;
        int anInt = element.getAsInt();
        this.numbers.put(key, anInt);
        return anInt;
    }

    public @Nullable Double getDouble(@NonNull String key) {
        if (cachedKeys.contains(key)) return (Double) numbers.get(key);
        JsonElement element = getElement(key);
        if (element == null) return null;
        double aDouble = element.getAsDouble();
        this.numbers.put(key, aDouble);
        return aDouble;
    }

    public @Nullable Float getFloat(@NonNull String key) {
        if (cachedKeys.contains(key)) return (Float) numbers.get(key);
        JsonElement element = getElement(key);
        if (element == null) return null;
        float aFloat = element.getAsFloat();
        this.numbers.put(key, aFloat);
        return aFloat;
    }

    public @Nullable Boolean getBoolean(@NonNull String key) {
        if (cachedKeys.contains(key)) return booleans.getBoolean(key);
        JsonElement element = getElement(key);
        if (element == null) return null;
        boolean aBoolean = element.getAsBoolean();
        this.booleans.put(key, aBoolean);
        return aBoolean;
    }

    public String getStringOrElse(@NonNull String key, String another) {
        String string = getString(key);
        if (string == null) return another;
        return string;
    }

    public long getLongOrElse(@NonNull String key, long another) {
        Long aLong = getLong(key);
        if (aLong == null) return another;
        return aLong;
    }

    public int getIntOrElse(@NonNull String key, int another) {
        Integer anInt = getInt(key);
        if (anInt == null) return another;
        return anInt;
    }

    public double getDoubleOrElse(@NonNull String key, double another) {
        Double aDouble = getDouble(key);
        if (aDouble == null) return another;
        return aDouble;
    }

    public float getFloatOrElse(@NonNull String key, float another) {
        Float aFloat = getFloat(key);
        if (aFloat == null) return another;
        return aFloat;
    }

    public boolean getBooleanOrElse(@NonNull String key, boolean another) {
        Boolean aBoolean = getBoolean(key);
        if (aBoolean == null) return another;
        return aBoolean;
    }

    public void set(@NonNull String key, @NonNull String value) {
        set(key, new JsonPrimitive(value));
    }

    public void set(@NonNull String key, @NonNull Number value) {
        set(key, new JsonPrimitive(value));
    }

    public void set(@NonNull String key, boolean value) {
        set(key, new JsonPrimitive(value));
    }

    public void set(@NonNull String key, Set<?> set) {
        JsonElement element = Communicator.getGson().toJsonTree(new ArrayList<>(set));
        set(key, element);
    }

    public void set(@NonNull String key, Map<String, ?> map) {
        JsonElement element = Communicator.getGson().toJsonTree(map);
        set(key, element);
    }

    public void unset(@NonNull String key) {
        set(key, JsonNull.INSTANCE);
    }

    private void set(@NonNull String key, @NonNull JsonElement value) {
        this.cachedKeys.add(key);
        updateObject(key, value);
        UpdateBroadcast broadcast = new UpdateBroadcast(wrapper.getIdentifier(), key, value);
        this.wrapperManager.updateBroadcaster.broadcast(broadcast);
    }

    void updateObject(@NonNull String key, @NonNull JsonElement value) {
        if (!this.cachedKeys.contains(key)) return;
        this.wrapperManager.update(this.wrapper);
        if (value.isJsonPrimitive()) {
            JsonPrimitive primitive = value.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                this.booleans.put(key, primitive.getAsBoolean());
                return;
            }
            if (primitive.isString()) {
                this.strings.put(key, primitive.getAsString());
                return;
            }
            if (primitive.isNumber()) {
                this.numbers.put(key, primitive.getAsNumber());
                return;
            }
            return;
        }
        if (value.isJsonNull()) {
            this.numbers.remove(key);
            this.strings.remove(key);
            this.booleans.removeBoolean(key);
            this.maps.remove(key);
            this.sets.remove(key);
            return;
        }
        if (value.isJsonArray()) {
            JsonArray jsonArray = value.getAsJsonArray();
            ObjectSet<?> set = this.sets.get(key);
            if (set == null) return;
            set = new ObjectArraySet<>(set);
            set.clear();
            for (JsonElement element : jsonArray) {
                addToSet(element, set);
            }
            this.sets.put(key, set);
            return;
        }
        if (value.isJsonObject()) {
            JsonObject jsonObject = value.getAsJsonObject();
            Object2ObjectMap<String, ?> map = this.maps.get(key);
            if (map == null) return;
            map = new Object2ObjectArrayMap<>(map);
            map.clear();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                addToMap(entry.getValue(), map, entry.getKey());
            }
            this.maps.put(key, map);
        }
    }

    @SuppressWarnings("unchecked")
    private <V> void addToMap(JsonElement element, Object2ObjectMap<String, V> map, String key) {
        try {
            V value = (V) GsonUtils.toObject(element);
            if (value == null) return;
            map.put(key, value);
        } catch (Exception exception) {
            logger.error("Cannot put " + element + " with key " + key + " to map", exception);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void addToSet(JsonElement element, ObjectSet<T> set) {
        try {
            T object = (T) GsonUtils.toObject(element);
            if (object == null) return;
            set.add(object);
        } catch (Exception exception) {
            logger.error("Cannot add " + element + " to set", exception);
        }
    }
}
