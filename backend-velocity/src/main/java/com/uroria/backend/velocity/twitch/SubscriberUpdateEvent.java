package com.uroria.backend.velocity.twitch;

import com.uroria.backend.twitch.Subscriber;
import lombok.Getter;

public final class SubscriberUpdateEvent {

    private @Getter final Subscriber subscriber;

    public SubscriberUpdateEvent(Subscriber subscriber) {
        this.subscriber = subscriber;
    }
}
