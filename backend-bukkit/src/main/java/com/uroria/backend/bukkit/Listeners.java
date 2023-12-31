package com.uroria.backend.bukkit;

import com.uroria.backend.Backend;
import com.uroria.backend.impl.AbstractBackendWrapper;
import com.uroria.backend.impl.utils.TranslationUtils;
import com.uroria.backend.user.User;
import com.uroria.base.lang.Language;
import com.uroria.base.lang.Translation;
import com.uroria.problemo.result.Result;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.lang.reflect.Field;
import java.util.UUID;

public final class Listeners implements Listener {
    private final Field permField;
    private final AbstractBackendWrapper wrapper;

    public Listeners(AbstractBackendWrapper wrapper) {
        this.wrapper = wrapper;
        this.permField = getPermField();
        if (permField == null) return;
        permField.setAccessible(true);
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent loginEvent) {
        Player player = loginEvent.getPlayer();
        try {
            if (permField == null) throw new NullPointerException("PermField is somehow null");
            User user = Backend.user(player.getUniqueId()).get();
            if (user == null) throw new IllegalStateException("Cannot get Backend user");
            permField.set(player, new BackendPermissible(user));
        } catch (Exception exception) {
            wrapper.getLogger().error("Cannot apply permField to player", exception);
            loginEvent.disallow(PlayerLoginEvent.Result.KICK_OTHER, TranslationUtils.disconnect(
                    Translation.component(Language.DEFAULT, "backend.perm.applyError"),
                    exception.getMessage()
            ));
        }
    }

    @SuppressWarnings("SafetyWarnings")
    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerLogin(AsyncPlayerPreLoginEvent loginEvent) {
        try {
            UUID uuid = loginEvent.getUniqueId();
            Result<User> userResult = wrapper.getUser(uuid);
            if (userResult instanceof Result.Problematic<User> problematic) {
                throw new RuntimeException(problematic.getProblem().getCause());
            }
            User user = userResult.get();
            if (user == null) {
                loginEvent.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, TranslationUtils.disconnect(
                        Translation.component(Language.DEFAULT, "backend.user.unfetchable")
                ));
                return;
            }
            String username = loginEvent.getName();
            String current = user.getUsername();
            if (!username.equals(current)) {
                user.setUsername(username);
            }
            user.setLanguage(Language.ENGLISH);
            user.setLastJoin(0);
            user.setPlaytime(2005);
            user.getClan(); // Just to verify the clan may be loaded
        } catch (Exception exception) {
            loginEvent.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, TranslationUtils.disconnect(
                    Translation.component(Language.DEFAULT, "backend.user.joinError"),
                    exception.getMessage()
            ));
        }
    }

    private Field getPermField() {
        try {
            return Class.forName("org.bukkit.craftbukkit.v1_20_R1.entity.CraftHumanEntity").getDeclaredField("perm");
        } catch (Exception exception) {
            wrapper.getLogger().error("Cannot fetch permField", exception);
            Bukkit.shutdown();
            return null;
        }
    }
}
