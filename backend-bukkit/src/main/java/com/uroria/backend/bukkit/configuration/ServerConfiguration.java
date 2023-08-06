package com.uroria.backend.bukkit.configuration;

import com.uroria.backend.bukkit.BackendBukkitPlugin;
import de.leonhard.storage.Json;
import de.leonhard.storage.internal.settings.ReloadSettings;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class ServerConfiguration {
    private final Json config = new Json("server.json", "./backend", BackendBukkitPlugin.class.getClassLoader().getResourceAsStream("server.json"), ReloadSettings.MANUALLY);

    static {
        reload();
    }

    private @Getter Map<String, Object> properties;

    public void reload() {
        config.forceReload();
        properties = config.getOrSetDefault("properties", new HashMap<>());
    }
}
