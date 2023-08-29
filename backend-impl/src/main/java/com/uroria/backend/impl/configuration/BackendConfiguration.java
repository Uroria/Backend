package com.uroria.backend.impl.configuration;

import com.uroria.backend.impl.AbstractBackend;
import de.leonhard.storage.Json;
import de.leonhard.storage.internal.settings.ReloadSettings;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BackendConfiguration {
    private @Getter final Json config = new Json("config.json", "./backend", AbstractBackend.class.getClassLoader().getResourceAsStream("backend.json"), ReloadSettings.MANUALLY);

    static {
        reload();
    }

    private @Setter String pulsarURL;
    private @Getter @Setter boolean offline;

    public void reload() {
        config.forceReload();
        pulsarURL = config.getOrSetDefault("pulsar.url", "pulsar://localhost:6650");
        offline = config.getOrSetDefault("offline", false);
    }

    public String getPulsarURL() {
        if (pulsarURL == null) {
            setOffline(true);
            return null;
        }
        if (pulsarURL.isEmpty()) {
            setOffline(true);
            return null;
        }
        if (offline) {
            return null;
        }
        return pulsarURL;
    }

    public String getString(String key) {
        if (key == null) return null;
        return config.getOrSetDefault(key, key);
    }

    public boolean getBoolean(String key) {
        if (key == null) return false;
        return config.getOrSetDefault(key, false);
    }
}
