package com.uroria.backend.common.utils;

import com.uroria.backend.common.player.BackendPlayer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.UUID;

public final class BackendInputStream extends ObjectInputStream implements AutoCloseable {
    private final ByteArrayInputStream inputBuffer;
    private BackendInputStream(ByteArrayInputStream inputBuffer) throws IOException {
        super(inputBuffer);
        this.inputBuffer = inputBuffer;
    }

    public BackendInputStream(byte[] data) throws IOException {
        this(new ByteArrayInputStream(data));
    }

    public UUID readUUID() throws IOException {
        try {
            return (UUID) readObject();
        } catch (ClassNotFoundException exception) {
            return null;
        }
    }

    public BackendPlayer readPlayer() throws IOException {
        try {
            return (BackendPlayer) readObject();
        } catch (ClassNotFoundException exception) {
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        this.inputBuffer.close();
        super.close();
    }
}
