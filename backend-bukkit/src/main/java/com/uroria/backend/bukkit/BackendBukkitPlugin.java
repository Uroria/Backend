package com.uroria.backend.bukkit;

import com.uroria.backend.bukkit.listeners.PlayerLogin;
import com.uroria.backend.bukkit.server.ServerManager;
import com.uroria.backend.impl.configuration.BackendConfiguration;
import com.uroria.backend.bukkit.listeners.PlayerJoin;
import com.uroria.backend.bukkit.listeners.PlayerPreLogin;
import com.uroria.backend.bukkit.listeners.PlayerQuit;
import com.uroria.backend.impl.scheduler.BackendScheduler;
import com.uroria.backend.wrapper.configuration.ServerConfiguration;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class BackendBukkitPlugin extends JavaPlugin {
    private final Logger logger;
    private final boolean timeoutEnabled;
    private final int timeout;
    private @Getter final ServerManager serverManager;
    private final BackendWrapper wrapper;

    public BackendBukkitPlugin() {
        this.logger = LoggerFactory.getLogger("Backend");
        this.timeoutEnabled = ServerConfiguration.getConfig().getOrSetDefault("autostop.enabled", true);
        this.timeout = ServerConfiguration.getConfig().getOrSetDefault("autostop.timeoutMin", 5);
        try {
            this.wrapper = new BackendWrapper(BackendConfiguration.getPulsarURL(), this.logger, BackendConfiguration.isOffline(), () -> {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.getOnlinePlayers().forEach(Player::kick);
                        Bukkit.shutdown();
                    }
                }.runTask(this);
            }, uuid -> Bukkit.getPlayer(uuid) != null);
            this.serverManager = new ServerManager(this.logger, BackendWrapper.getAPI().getEventManager());
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
            this.wrapper.start();
            this.serverManager.start(UUID.randomUUID().toString());
        } catch (Exception exception) {
            this.logger.error("Cannot start backend", exception);
            Bukkit.shutdown();
            return;
        }

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerPreLogin(this.wrapper), this);
        pluginManager.registerEvents(new PlayerJoin(this, this.logger), this);
        pluginManager.registerEvents(new PlayerQuit(this, this.logger), this);
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
            this.serverManager.shutdown();
            this.wrapper.shutdown();
        } catch (Exception exception) {
            this.logger.error("Couldn't shutdown backend connections properly", exception);
        }
    }

    public static boolean isOffline() {
        return BackendConfiguration.isOffline();
    }
}
