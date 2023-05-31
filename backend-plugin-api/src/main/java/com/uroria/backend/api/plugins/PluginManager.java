package com.uroria.backend.api.plugins;

import java.util.Collection;
import java.util.Optional;

public interface PluginManager {
    Collection<BackendPlugin> getPlugins();

    Optional<BackendPlugin> getPlugin(String name);


}
