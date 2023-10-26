package com.uroria.backend.bukkit;

import com.uroria.backend.impl.AbstractBackendWrapper;
import com.uroria.backend.user.User;
import com.uroria.problemo.result.Result;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.UUID;

public final class Listeners implements Listener {
    private final AbstractBackendWrapper wrapper;

    public Listeners(AbstractBackendWrapper wrapper) {
        this.wrapper = wrapper;
    }

    @SuppressWarnings("SafetyWarnings")
    @EventHandler
    public void onPlayerLogin(AsyncPlayerPreLoginEvent loginEvent) {
        UUID uuid = loginEvent.getUniqueId();
        Result<User> userResult = wrapper.getUser(uuid);
        if (userResult instanceof Result.Problematic<User> problematic) {
            disallow(loginEvent, problematic.getProblem().getCause());
            return;
        }
        User user = userResult.get();
        if (user == null) {
            disallow(loginEvent, "Unable to fetch user. Please try again later.");
            return;
        }
        user.getClan(); // Just to verify the clan may be loaded
    }

    private void disallow(AsyncPlayerPreLoginEvent loginEvent, String reason) {
        wrapper.getLogger().warn(loginEvent.getUniqueId() + " kicked with reason: " + reason);
        loginEvent.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text(reason, NamedTextColor.RED));
    }
}
