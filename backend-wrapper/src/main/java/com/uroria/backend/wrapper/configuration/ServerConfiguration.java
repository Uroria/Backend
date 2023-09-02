package com.uroria.backend.wrapper.configuration;

import com.uroria.fastconfig.Json;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class ServerConfiguration {
    private @Getter final Json config = new Json("server.json", "./backend");

    static {
        reload();
    }

    private @Getter Map<String, Object> properties;

    public void reload() {
        config.reload();
        properties = config.getOrSetDefault("properties", new HashMap<>());
    }
}
