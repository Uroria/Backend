package com.uroria.backend.bukkit;

import com.uroria.backend.bukkit.commands.StopCommand;
import com.uroria.backend.bukkit.listeners.PlayerJoin;
import com.uroria.backend.bukkit.listeners.PlayerPreLogin;
import com.uroria.backend.bukkit.listeners.PlayerQuit;
import de.leonhard.storage.Json;
import de.leonhard.storage.internal.settings.ReloadSettings;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BackendBukkitPlugin extends JavaPlugin {
    private static final Json CONFIG = new Json("backend.json", "./", BackendBukkitPlugin.class.getClassLoader().getResourceAsStream("backend.json"), ReloadSettings.MANUALLY);;

    private final Logger logger;
    private final BackendAPI backendAPI;
    public BackendBukkitPlugin() {
        this.logger = LoggerFactory.getLogger("BukkitAPI");
        BackendAPI backendAPI;
        try {
            backendAPI = new BackendAPI(CONFIG.getString("pulsar.url"), CONFIG.getOrSetDefault("sentry.enabled", false), logger);
        } catch (Exception exception) {
            logger.error("Cannot connect to backend! Exiting...", exception);
            this.backendAPI = null;
            return;
        }
        this.backendAPI = backendAPI;
    }

    @Override
    public void onEnable() {
        this.backendAPI.start();
        
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerPreLogin(this.backendAPI), this);
        pluginManager.registerEvents(new PlayerJoin(), this);
        pluginManager.registerEvents(new PlayerQuit(), this);

        PluginCommand stop = getCommand("stop");
        if (stop == null) return;
        stop.setExecutor(new StopCommand());
    }

    @Override
    public void onDisable() {
        this.backendAPI.shutdown();
        HandlerList.unregisterAll(this);
    }

    static Json config() {
        return CONFIG;
    }
}
