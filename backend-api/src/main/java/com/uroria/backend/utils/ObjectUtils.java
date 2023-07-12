package com.uroria.backend.utils;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;

@UtilityClass
public class ObjectUtils {

    public <K, V> void overrideMap(Map<K, V> target, Map<K, V> source) {
        target.keySet().forEach(key -> {
            if (!source.containsKey(key)) target.remove(key);
        });
        target.putAll(source);
    }

    public <T> void overrideCollection(List<T> target, List<T> source) {
        target.retainAll(source);
        source.forEach(obj -> {
            if (target.contains(obj)) return;
            target.add(obj);
        });
    }
}
