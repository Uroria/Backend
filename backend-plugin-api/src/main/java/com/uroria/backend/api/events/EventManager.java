package com.uroria.backend.api.events;

public interface EventManager {
    <T extends Event> void callEvent(T event);
    void registerListener(Object listener);
    void unregisterListeners(Class<?> clazz);
}
