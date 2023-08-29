package com.uroria.backend.bukkit;

import com.uroria.backend.bukkit.permission.listeners.PlayerLogin;
import com.uroria.backend.impl.configuration.BackendConfiguration;
import com.uroria.backend.bukkit.listeners.PlayerJoin;
import com.uroria.backend.bukkit.listeners.PlayerPreLogin;
import com.uroria.backend.bukkit.listeners.PlayerQuit;
import com.uroria.backend.impl.scheduler.BackendScheduler;
import com.uroria.nutshell.plugin.utils.BukkitScheduler;
import org.bukkit.Bukkit;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public final class BackendBukkitPlugin extends JavaPlugin {
    private final Logger logger;
    private final BackendImpl backend;
    private final boolean timeoutEnabled;
    private final int timeout;

    public BackendBukkitPlugin() {
        this.logger = LoggerFactory.getLogger("Backend");
        this.timeoutEnabled = BackendConfiguration.getConfig().getOrSetDefault("autostop.enabled", true);
        this.timeout = BackendConfiguration.getConfig().getOrSetDefault("autostop.timeoutMin", 5);
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
        pluginManager.registerEvents(new PlayerQuit(this.backend, this.logger, this), this);
        checkTimeout();

        if (!BackendConfiguration.getBoolean("permissionIncluded")) return;
        pluginManager.registerEvents(new PlayerLogin(), this);
    }

    public void checkTimeout() {
        if (!timeoutEnabled) return;
        logger.info("Running timeout check in " + this.timeout + " minutes");
        BackendScheduler.runTaskLater(() -> {
            logger.info("Timeout check now");
            return null;
        }, this.timeout, TimeUnit.MINUTES).run((nil, throwable) -> {
            if (throwable == null) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.shutdown();
                    }
                }.runTask(this);
                return;
            }
            int size = Bukkit.getOnlinePlayers().size();
            if (size < 1) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.shutdown();
                    }
                }.runTask(this);
            }
        });
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
