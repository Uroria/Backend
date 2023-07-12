package com.uroria.backend.utils;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Optional;

@UtilityClass
public class IOUtils {

    public void writeObject(ObjectOutputStream output, Serializable object) throws IOException {
        if (object == null) {
            output.writeBoolean(false);
            return;
        }
        output.writeBoolean(true);
        output.writeObject(object);
    }

    public <T> Optional<T> readObject(ObjectInputStream input, Class<T> clazz) throws IOException, ClassNotFoundException {
        if (input.readBoolean()) {
            return Optional.of((T) input.readObject());
        }
        return Optional.empty();
    }
}
