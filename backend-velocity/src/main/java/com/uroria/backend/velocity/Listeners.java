package com.uroria.backend.velocity;

import com.uroria.backend.cache.BackendObject;
import com.uroria.backend.cache.Wrapper;
import com.uroria.backend.impl.AbstractBackendWrapper;
import com.uroria.backend.impl.user.UserWrapper;
import com.uroria.backend.impl.utils.TranslationUtils;
import com.uroria.backend.user.User;
import com.uroria.base.lang.Language;
import com.uroria.base.lang.Translation;
import com.uroria.problemo.result.Result;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.permission.PermissionsSetupEvent;
import com.velocitypowered.api.proxy.Player;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.UUID;

@AllArgsConstructor
public final class Listeners {
    private final AbstractBackendWrapper wrapper;
    private final BackendPlugin plugin;

    @Subscribe
    public void onPermissionsSetupEvent(PermissionsSetupEvent setupEvent) {
        setupEvent.setProvider(plugin.getPermissionProvider());
    }

    @Subscribe(order = PostOrder.FIRST)
    public EventTask onLogin(LoginEvent loginEvent) {
        return EventTask.async(() -> {
            try {
                if (!loginEvent.getResult().isAllowed()) return;
                Player player = loginEvent.getPlayer();
                UUID uuid = player.getUniqueId();
                Result<User> userResult = this.wrapper.getUser(uuid);
                if (userResult instanceof Result.Problematic<User> problematic) {
                    throw new RuntimeException(problematic.getProblem().getCause());
                }
                UserWrapper user = (UserWrapper) userResult.get();
                if (user == null) {
                    loginEvent.setResult(ResultedEvent.ComponentResult.denied(TranslationUtils.disconnect(
                            Translation.component(Language.DEFAULT, "backend.user.unfetchable")
                    )));
                    return;
                }
                String username = player.getUsername();
                String current = user.getUsername();
                if (!username.equals(current)) {
                    user.setUsername(username);
                }
                BackendObject<? extends Wrapper> object = user.getBackendObject();
                long firstJoin = user.getFirstJoin();
                long currentMs = System.currentTimeMillis();
                if (firstJoin == 0) {
                    object.set("firstJoin", currentMs);
                }
                user.setLastJoin(currentMs);
                user.getClan();
            } catch (Exception exception) {
                loginEvent.setResult(ResultedEvent.ComponentResult.denied(TranslationUtils.disconnect(
                        Translation.component(Language.DEFAULT, "backend.user.joinError"),
                        exception.getMessage()
                )));
                wrapper.getLogger().error("Unexpected exception while login", exception);
            }
        });
    }
}
