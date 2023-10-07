package com.uroria.backend.impl.communication;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.uroria.problemo.result.Result;
import lombok.Getter;
import lombok.NonNull;

public final class CommunicationWrapper implements AutoCloseable {
    @Getter
    private final String identifier;
    private final CommunicationClient client;

    private final JsonObject object;

    public CommunicationWrapper(@NonNull String identifier, @NonNull CommunicationClient client) {
        this.identifier = identifier;
        this.client = client;
        this.object = client.newObject(identifier);
    }

    @Override
    public void close() throws Exception {
        this.client.removeObject(this.identifier);
    }

    public JsonObject getObject() {
        return this.object;
    }

    public void remove(String key) {
        this.object.remove(key);
    }

    private void update(String key, JsonElement value) {
        this.client.update(this, key, value);
    }

    public void set(String key, JsonElement value) {
        JsonElement element = this.object.get(key);
        if (element == null) return;
        object.remove(key);
        object.add(key, value);
        update(key, this.object.get(key));
    }

    public void set(String key, String value) {
        JsonElement element = this.object.get(key);
        if (element == null) return;
        object.remove(key);
        object.addProperty(key, value);
        update(key, this.object.get(key));
    }

    public void set(String key, Number value) {
        JsonElement element = this.object.get(key);
        if (element == null) return;
        object.remove(key);
        object.addProperty(key, value);
        update(key, this.object.get(key));
    }

    public void set(String key, boolean value) {
        JsonElement element = this.object.get(key);
        if (element == null) return;
        object.remove(key);
        object.addProperty(key, value);
        update(key, this.object.get(key));
    }

    public void set(String key, Character value) {
        JsonElement element = this.object.get(key);
        if (element == null) return;
        object.remove(key);
        object.addProperty(key, value);
        update(key, this.object.get(key));
    }

    public Result<JsonElement> get(String key) {
        JsonElement cachedElement = this.object.get(key);
        if (cachedElement != null) {
            if (cachedElement.isJsonNull()) return Result.none();
            return Result.some(cachedElement);
        }

        Result<JsonElement> result = client.request(this, key, 2000);
        if (result instanceof Result.Problematic<JsonElement> error) {
            return error;
        }

        JsonElement element = result.get();
        if (element == null) {
            this.object.remove(key);
            return Result.none();
        }
        if (element.isJsonNull()) {
            this.object.add(key, element);
            return Result.none();
        }

        return Result.of(element);
    }
}
