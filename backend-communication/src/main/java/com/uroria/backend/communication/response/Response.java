package com.uroria.backend.communication.response;

import com.google.gson.JsonElement;
import com.uroria.backend.communication.Communicator;

public abstract class Response {

    JsonElement toElement() {
        return Communicator.getGson().toJsonTree(this);
    }
}
