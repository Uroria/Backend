package com.uroria.backend.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ValidUtils {

    public void notNull(Object o, String message) {
        if (o != null) return;
        throw new NullPointerException(message);
    }
}
