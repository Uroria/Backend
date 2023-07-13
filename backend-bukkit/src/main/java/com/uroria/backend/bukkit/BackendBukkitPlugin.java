package com.uroria.backend.bukkit;

import com.uroria.backend.Unsafe;
import com.uroria.backend.bukkit.commands.StopCommand;
import com.uroria.backend.bukkit.listeners.PlayerJoin;
import com.uroria.backend.bukkit.listeners.PlayerPreLogin;
import com.uroria.backend.bukkit.listeners.PlayerQuit;
import de.leonhard.storage.Json;
import de.leonhard.storage.internal.settings.ReloadSettings;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BackendBukkitPlugin extends JavaPlugin {
    private static final boolean OFFLINE_MODE;
    private static final Json SERVER_CONFIG = new Json("server.json", "./", BackendBukkitPlugin.class.getClassLoader().getResourceAsStream("server.json"), ReloadSettings.MANUALLY);
    private static final Json CONFIG = new Json("backend.json", "./", BackendBukkitPlugin.class.getClassLoader().getResourceAsStream("backend.json"), ReloadSettings.MANUALLY);;

    static {
        OFFLINE_MODE = CONFIG.getOrSetDefault("offline", false);
    }

    private final Logger logger;
    private final BackendAPIImpl backendAPI;
    public BackendBukkitPlugin() {
        this.logger = LoggerFactory.getLogger("BukkitAPI");
        BackendAPIImpl backendAPI;
        try {
            String url = CONFIG.getString("pulsar.url");
            if (OFFLINE_MODE) url = null;
            backendAPI = new BackendAPIImpl(url, CONFIG.getOrSetDefault("sentry.enabled", false), logger);
        } catch (Exception exception) {
            logger.error("Cannot connect to backend! Exiting...", exception);
            this.backendAPI = null;
            return;
        }
        Unsafe.setAPI(backendAPI);
        this.backendAPI = backendAPI;
    }

    @Override
    public void onEnable() {
        try {
            this.backendAPI.start();
        } catch (Exception exception) {
            logger.error("Cannot start backend", exception);
            Bukkit.shutdown();
        }
        
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

    static void kickAll() {
        try {
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.kickPlayer("");
                    }
                }
            }.runTask(JavaPlugin.getPlugin(BackendBukkitPlugin.class));
        } catch (Exception exception) {
            JavaPlugin.getPlugin(BackendBukkitPlugin.class).logger.error("Cannot kick all players", exception);
        }
    }

    static Json config() {
        return CONFIG;
    }

    static Json serverConfig() {
        return SERVER_CONFIG;
    }

    static boolean isOffline() {
        return OFFLINE_MODE;
    }
}
