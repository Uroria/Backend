package com.uroria.backend.cache.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.experimental.UtilityClass;

import java.util.Map;

@UtilityClass
public class GsonUtils {

    public Object toObject(JsonElement element) {
        if (element.isJsonNull()) {
            return null;
        }
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return primitive.getAsBoolean();
            }
            if (primitive.isNumber()) {
                return primitive.getAsNumber();
            }
            if (primitive.isString()) {
                return primitive.getAsString();
            }
            return null;
        }
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            return toSet(array);
        }
        if (element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();
            return toMap(jsonObject);
        }
        return null;
    }

    public Object2ObjectMap<String, Object> toMap(JsonObject object) {
        Object2ObjectMap<String, Object> map = new Object2ObjectArrayMap<>();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            map.put(entry.getKey(), toObject(entry.getValue()));
        }
        return map;
    }

    public ObjectSet<Object> toSet(JsonArray array) {
        ObjectSet<Object> set = new ObjectArraySet<>();
        array.forEach(element -> {
            Object object = toObject(element);
            set.add(object);
        });
        return set;
    }
}
