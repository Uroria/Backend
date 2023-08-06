package com.uroria.backend.bukkit.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;

import java.util.concurrent.CompletableFuture;

@UtilityClass
public class BukkitUtils {

    public void callAsyncEvent(Event event) {
        CompletableFuture.runAsync(() -> Bukkit.getPluginManager().callEvent(event));
    }
}
