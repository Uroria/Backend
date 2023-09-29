package com.uroria.backend.impl.pulsar;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.uroria.base.io.InsaneByteArrayInputStream;
import com.uroria.problemo.result.Result;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;


public final class PulsarObject implements AutoCloseable {
    private final PulsarRequestChannel request;
    private final PulsarUpdateChannel update;

    private final JsonObject object;

    public PulsarObject(PulsarClient pulsarClient, CryptoKeyReader keyReader, String name, String requestTopic, String updateTopic) {
        this.request = new PulsarRequestChannel(pulsarClient, keyReader, name, requestTopic);
        this.update = new PulsarUpdateChannel(pulsarClient, keyReader, name, updateTopic) {
            @Override
            public void onUpdate(InsaneByteArrayInputStream input) {
                try {
                    String key = input.readUTF();
                    String object = input.readUTF();
                    checkObject(key, object);
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            }
        };
        this.object = new JsonObject();
    }

    @Override
    public void close() throws PulsarClientException {
        this.request.close();
        this.update.close();
    }

    public void remove(String key) {
        this.object.remove(key);
    }

    private void update(String key, JsonElement value) {
        this.update.update(key, value);
    }

    private void checkObject(String key, String value) {
        JsonElement cachedElement = this.object.get(key);
        if (cachedElement == null) return;
        JsonElement element = JsonParser.parseString(value);
        this.object.remove(key);
        this.object.add(key, element);
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

        Result<JsonElement> result = this.request.request(key, 2000);
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
