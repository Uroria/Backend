package com.uroria.backend.velocity.configuration;

import com.uroria.backend.velocity.BackendVelocityPlugin;
import de.leonhard.storage.Json;
import de.leonhard.storage.internal.settings.ReloadSettings;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class ServerConfiguration {
    private final Json config = new Json("server.json", "./backend", BackendVelocityPlugin.class.getClassLoader().getResourceAsStream("server.json"), ReloadSettings.MANUALLY);

    static {
        reload();
    }

    private @Getter Map<String, Object> properties;

    public void reload() {
        config.forceReload();
        properties = config.getOrSetDefault("properties", new HashMap<>());
    }
}
