package com.uroria.backend.communication.request;

import com.google.gson.JsonElement;
import com.uroria.backend.communication.Communicator;

public abstract class Request {

    JsonElement toElement() {
        return Communicator.getGson().toJsonTree(this);
    }
}
