package com.uroria.backend.impl.io;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.uroria.base.io.InsaneByteArrayInputStream;

import java.io.IOException;

public final class BackendInputStream extends InsaneByteArrayInputStream {

    public BackendInputStream(byte[] data) throws IOException {
        super(data);
    }

    public JsonElement readJsonElement() throws IOException, JsonParseException {
        String jsonString = readUTF();
        JsonObject object = JsonParser.parseString(jsonString).getAsJsonObject();
        return object.get("content");
    }
}
