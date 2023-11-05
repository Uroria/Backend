package com.uroria.backend.service.modules;

public interface ControllableModule {

    default void enable() {

    }

    default void disable() {

    }

    String getModuleName();
}
