package com.uroria.backend.cache.communication.controls;

public final class UnavailableException extends RuntimeException {

    public UnavailableException(String message) {
        super(message);
    }

    public UnavailableException() {
        this("Service not available");
    }
}
