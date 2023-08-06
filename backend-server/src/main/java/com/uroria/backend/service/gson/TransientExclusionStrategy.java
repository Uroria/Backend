package com.uroria.backend.service.gson;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.uroria.backend.utils.TransientField;

public final class TransientExclusionStrategy implements ExclusionStrategy {

    @Override
    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
        return fieldAttributes.getAnnotation(TransientField.class) != null;
    }

    @Override
    public boolean shouldSkipClass(Class<?> aClass) {
        return false;
    }
}
