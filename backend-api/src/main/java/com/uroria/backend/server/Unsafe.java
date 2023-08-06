package com.uroria.backend.server;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Unsafe {

    public void setServerID(Server server, int id) {
        server.setID(id);
    }
}
