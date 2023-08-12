package com.uroria.backend.bukkit;

import com.uroria.backend.bukkit.permission.listeners.PlayerLogin;
import com.uroria.backend.impl.configuration.BackendConfiguration;
import com.uroria.backend.bukkit.listeners.PlayerJoin;
import com.uroria.backend.bukkit.listeners.PlayerPreLogin;
import com.uroria.backend.bukkit.listeners.PlayerQuit;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BackendBukkitPlugin extends JavaPlugin {
    private final Logger logger;
    private final BackendImpl backend;

    public BackendBukkitPlugin() {
        this.logger = LoggerFactory.getLogger("Backend");
        try {
            this.backend = new BackendImpl(BackendConfiguration.getPulsarURL(), this.logger);
        } catch (Exception exception) {
            Bukkit.shutdown();
            throw new RuntimeException("Unexpected exception", exception);
        }
        if (isOffline()) {
            logger.warn("Running in offline mode!");
        }
    }

    @Override
    public void onEnable() {
        try {
            this.backend.start();
        } catch (Exception exception) {
            this.logger.error("Cannot start backend", exception);
            Bukkit.shutdown();
            return;
        }

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerPreLogin(this.backend), this);
        pluginManager.registerEvents(new PlayerJoin(this.backend, this.logger), this);
        pluginManager.registerEvents(new PlayerQuit(this.backend, this.logger), this);

        if (!BackendConfiguration.getBoolean("permissionIncluded")) return;
        pluginManager.registerEvents(new PlayerLogin(), this);
    }

    @Override
    public void onDisable() {
        try {
            HandlerList.unregisterAll(this);
            this.backend.shutdown();
        } catch (Exception exception) {
            this.logger.error("Couldn't shutdown backend connections properly", exception);
        }
    }

    public static boolean isOffline() {
        return BackendConfiguration.isOffline();
    }
}
