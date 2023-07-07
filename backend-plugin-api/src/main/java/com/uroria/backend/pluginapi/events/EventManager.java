package com.uroria.backend.pluginapi.events;

import java.util.concurrent.CompletableFuture;

public interface EventManager {
    <T extends Event> void callEvent(T event);

    <T extends Event> CompletableFuture<Void> callEventAsync(T event);

    void registerListener(Object listener);

    void unregisterListeners(Class<?> clazz);
}
