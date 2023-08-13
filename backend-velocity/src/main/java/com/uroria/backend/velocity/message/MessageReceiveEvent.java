package com.uroria.backend.velocity.message;

import com.uroria.backend.message.Message;
import com.uroria.backend.permission.PermHolder;
import lombok.Getter;

public class MessageReceiveEvent {

    private @Getter final Message message;

    public MessageReceiveEvent(Message message) {
        this.message = message;
    }
}

