package com.uroria.backend.impl.pulsar;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.uroria.base.io.InsaneByteArrayInputStream;
import com.uroria.base.io.InsaneByteArrayOutputStream;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.PulsarClient;

public abstract class PulsarObjectResponses implements AutoCloseable {
    private final PulsarResponseChannel response;
    private final PulsarUpdateChannel update;

    public PulsarObjectResponses(PulsarClient pulsarClient, CryptoKeyReader keyReader, String name, String requestTopic, String updateTopic) {
        this.response = new PulsarResponseChannel(pulsarClient, keyReader, name, requestTopic) {
            @Override
            public void onRequest(InsaneByteArrayInputStream input, InsaneByteArrayOutputStream output) {
                try {
                    String key = input.readUTF();
                    output.writeUTF(key);
                    output.writeUTF(request(key).toString());
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            }
        };
        this.update = new PulsarUpdateChannel(pulsarClient, keyReader, name, updateTopic) {
            @Override
            public void onUpdate(InsaneByteArrayInputStream input) {
                try {
                    String key = input.readUTF();
                    String object = input.readUTF();
                    checkObject(key, JsonParser.parseString(object));
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            }
        };
    }

    public abstract JsonElement request(String key);

    public abstract void checkObject(String key, JsonElement value);

    @Override
    public void close() throws Exception {
        this.response.close();
        this.update.close();
    }
}
