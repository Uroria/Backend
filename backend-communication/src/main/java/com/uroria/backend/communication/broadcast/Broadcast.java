package com.uroria.backend.communication.broadcast;

import com.google.gson.JsonElement;
import com.uroria.backend.communication.Communicator;

public abstract class Broadcast {

    JsonElement toElement() {
        return Communicator.getGson().toJsonTree(this);
    }
}
