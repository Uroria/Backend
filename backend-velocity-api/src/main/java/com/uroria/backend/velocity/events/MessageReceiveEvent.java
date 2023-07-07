package com.uroria.backend.velocity.events;

import com.uroria.backend.common.BackendMessage;

public final class MessageReceiveEvent {
    private final BackendMessage message;

    public MessageReceiveEvent(BackendMessage message) {
        this.message = message;
    }

    public BackendMessage getMessage() {
        return message;
    }
}
