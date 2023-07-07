package com.uroria.backend.common;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Unsafe {
    public void setIdOfServer(@NonNull BackendServer server, int id) {
        server.setId(id);
    }
}
