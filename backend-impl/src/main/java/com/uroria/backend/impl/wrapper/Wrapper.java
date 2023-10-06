package com.uroria.backend.impl.wrapper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.uroria.backend.impl.communication.CommunicationWrapper;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.ObjectLists;

import java.util.Collection;

public abstract class Wrapper {

    public abstract void refresh();

    public abstract JsonObject getObject();

    public abstract CommunicationWrapper getObjectWrapper();

    public abstract String getIdentifierKey();

    public abstract String getStringIdentifier();

    public boolean getBoolean(String key) {
        Result<JsonElement> result = getObjectWrapper().get(key);
        JsonElement element = result.get();
        if (element == null) return false;
        return element.getAsBoolean();
    }

    public boolean getBoolean(String key, boolean orElse) {
        Result<JsonElement> result = getObjectWrapper().get(key);
        JsonElement element = result.get();
        if (element == null) return orElse;
        return element.getAsBoolean();
    }

    public void addToLongList(String key, long value) {
        Result<JsonElement> result = getObjectWrapper().get(key);
        JsonElement element = result.get();
        if (element == null) return;
        JsonArray array = element.getAsJsonArray();
        array.add(value);
        getObjectWrapper().set(key, element);
    }

    public void removeFromLongList(String key, long value) {
        Result<JsonElement> result = getObjectWrapper().get(key);
        JsonElement element = result.get();
        if (element == null) return;
        JsonArray array = element.getAsJsonArray();
        for (JsonElement el : array) {
            if (el.getAsLong() != value) continue;
            array.remove(el);
            break;
        }
        getObjectWrapper().set("proxies", element);
    }

    public Collection<String> getStrings(String key) {
        return getArray(key).stream().map(JsonElement::getAsString).toList();
    }

    public Collection<Long> getLongs(String key) {
        return getArray(key).stream().map(JsonElement::getAsLong).toList();
    }

    public Collection<JsonElement> getArray(String key) {
        Result<JsonElement> result = getObjectWrapper().get(key);
        JsonElement element = result.get();
        if (element == null) return ObjectLists.emptyList();
        JsonArray array = element.getAsJsonArray();
        return array.asList();
    }

    public final int getInt(String key) {
        Result<JsonElement> result = getObjectWrapper().get(key);
        JsonElement element = result.get();
        if (element == null) return 0;
        return element.getAsInt();
    }

    public final int getInt(String key, int orElse) {
        Result<JsonElement> result = getObjectWrapper().get(key);
        JsonElement element = result.get();
        if (element == null) return orElse;
        return element.getAsInt();
    }

    public final long getLong(String key, long orElse) {
        Result<JsonElement> result = getObjectWrapper().get(key);
        JsonElement element = result.get();
        if (element == null) return orElse;
        return element.getAsLong();
    }

    public final Result<String> getString(String key) {
        Result<JsonElement> result = getObjectWrapper().get(key);
        JsonElement element = result.get();
        if (element == null) return Result.none();
        return Result.of(element.getAsString());
    }

    public final String getString(String key, String orElse) {
        Result<JsonElement> result = getObjectWrapper().get(key);
        JsonElement element = result.get();
        if (element == null) return orElse;
        return element.getAsString();
    }
}
