package com.uroria.backend.impl.io;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.uroria.base.io.InsaneByteArrayOutputStream;

import java.io.IOException;

public final class BackendOutputStream extends InsaneByteArrayOutputStream {

    public BackendOutputStream() throws IOException {}

    public void writeJsonElement(JsonElement element) throws IOException {
        JsonObject object = new JsonObject();
        object.add("content", element);
        writeUTF(object.toString());
    }
}
