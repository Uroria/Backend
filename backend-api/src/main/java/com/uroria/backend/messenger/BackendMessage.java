package com.uroria.backend.messenger;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class BackendMessage implements Serializable {
    @Serial private static final long serialVersionUID = 1;

    private final String action;
    private final Map<String, Object> entries;

    public BackendMessage(String action, Map<String, Object> entries) {
        this.action = action;
        this.entries = entries;
    }

    public String getAction() {
        return action;
    }

    public void setEntry(String key, Serializable object) {
        this.entries.put(key, object);
    }

    public <T> Optional<T> getEntry(String key, Class<T> clazz) {
        return Optional.ofNullable((T) this.entries.get(key));
    }

    public Map<String, Object> getEntries() {
        return new HashMap<>(this.entries);
    }
}
