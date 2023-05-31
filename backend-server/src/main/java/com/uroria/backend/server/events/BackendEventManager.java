package com.uroria.backend.server.events;

import com.uroria.backend.api.BackendRegistry;
import com.uroria.backend.api.events.Event;
import com.uroria.backend.api.events.EventManager;
import com.uroria.backend.api.events.Listener;
import com.uroria.backend.server.Uroria;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class BackendEventManager implements EventManager {
    private final Collection<RegisteredListener> registeredListeners;

    public BackendEventManager() {
        BackendRegistry.register(this);
        this.registeredListeners = new CopyOnWriteArrayList<>();
    }

    @Override
    public <T extends Event> void callEvent(T event) {
        try {
            for (RegisteredListener registeredListener : this.registeredListeners) {
                for (Method method : registeredListener.getListeners(event.getClass())) {
                    method.invoke(registeredListener.listenerObject, event);
                }
            }
        } catch (Exception exception) {
            Uroria.getLogger().error("Cannot call event " + event.getClass().getSimpleName(), exception);
        }
    }

    @Override
    public void registerListener(Object listener) {
        this.registeredListeners.add(new RegisteredListener(listener));
    }

    @Override
    public void unregisterListeners(Class<?> clazz) {
        this.registeredListeners.removeIf(registeredListener -> registeredListener.listenerObject.getClass().equals(clazz));
    }

    private static final class RegisteredListener {
        private final Object listenerObject;
        private final Map<Class<? extends Event>, List<Method>> listeners;
        private RegisteredListener(Object listenerObject) {
            this.listenerObject = listenerObject;
            this.listeners = new ConcurrentHashMap<>();
            for (Method method : listenerObject.getClass().getMethods()) {
                if (!method.isAnnotationPresent(Listener.class)) continue;
                Class<?> parameterClass = method.getParameterTypes()[0];
                if (parameterClass == null) continue;
                try {
                    @SuppressWarnings("unchecked") Class<? extends Event> eventClass = (Class<? extends Event>) parameterClass;
                    this.listeners.putIfAbsent(eventClass, new ArrayList<>());
                    List<Method> listenersMethods = this.listeners.get(eventClass);
                    listenersMethods.add(method);
                } catch (Exception exception) {
                    Uroria.getLogger().error("Error while registering listener", exception);
                }
            }
        }

        public List<Method> getListeners(Class<? extends Event> eventClass) {
            List<Method> methods = this.listeners.get(eventClass);
            if (methods == null) return Collections.emptyList();
            return methods;
        }
    }
}
