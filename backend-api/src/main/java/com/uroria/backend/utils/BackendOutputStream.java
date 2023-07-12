package com.uroria.backend.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.UUID;

public final class BackendOutputStream extends ObjectOutputStream implements AutoCloseable {
    private final ByteArrayOutputStream outputBuffer;

    private BackendOutputStream(ByteArrayOutputStream outputStream) throws IOException {
        super(outputStream);
        this.outputBuffer = outputStream;
    }

    public BackendOutputStream() throws IOException {
        this(new ByteArrayOutputStream());
    }

    public void writePlayerRequest(UUID uuid) throws IOException {
        writeBoolean(true);
        writeObject(uuid);
    }

    public void writePlayerRequest(String name) throws IOException {
        writeBoolean(false);
        writeUTF(name);
    }

    @Override
    public void close() throws IOException {
        this.outputBuffer.close();
        super.close();
    }

    public byte[] toByteArray() {
        return this.outputBuffer.toByteArray();
    }
}
