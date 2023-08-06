package com.uroria.backend.velocity.twitch;

import com.uroria.backend.twitch.Streamer;
import lombok.Getter;;

public class StreamerUpdateEvent {
    private @Getter final Streamer streamer;

    public StreamerUpdateEvent(Streamer streamer) {
        this.streamer = streamer;
    }
}
