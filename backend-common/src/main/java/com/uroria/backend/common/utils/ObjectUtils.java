package com.uroria.backend.common.utils;

import java.util.List;
import java.util.Map;

public final class ObjectUtils {

    public static <K, V> void overrideMap(Map<K, V> target, Map<K, V> source) {
        target.keySet().forEach(key -> {
            if (!source.containsKey(key)) target.remove(key);
        });
        target.putAll(source);
    }

    public static <T> void overrideCollection(List<T> target, List<T> source) {
        target.retainAll(source);
        source.forEach(obj -> {
            if (target.contains(obj)) return;
            target.add(obj);
        });
    }
}
