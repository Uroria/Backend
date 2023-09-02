package com.uroria.backend.wrapper.message;

import com.uroria.backend.message.Message;
import lombok.Getter;

public class MessageReceiveEvent {

    private @Getter final Message message;

    public MessageReceiveEvent(Message message) {
        this.message = message;
    }
}

