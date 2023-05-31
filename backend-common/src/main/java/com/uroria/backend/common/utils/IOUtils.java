package com.uroria.backend.common.utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Optional;

public final class IOUtils {

    public static void writeObject(ObjectOutputStream output, Serializable object) throws IOException {
        if (object == null) {
            output.writeBoolean(false);
            return;
        }
        output.writeBoolean(true);
        output.writeObject(object);
    }

    public static <T> Optional<T> readObject(ObjectInputStream input, Class<T> clazz) throws IOException, ClassNotFoundException {
        if (input.readBoolean()) {
            return Optional.of((T) input.readObject());
        }
        return Optional.empty();
    }
}
