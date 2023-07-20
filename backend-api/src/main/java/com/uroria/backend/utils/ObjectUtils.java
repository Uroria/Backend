package com.uroria.backend.utils;

import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public <T> void overrideCollection(Set<T> target, Set<T> source) {
        target.retainAll(source);
        source.forEach(obj -> {
            if (target.contains(obj)) return;
            target.add(obj);
        });
    }

    public <T> Set<T> newSet() {
        return Collections.synchronizedSet(new HashSet<>());
    }
}
