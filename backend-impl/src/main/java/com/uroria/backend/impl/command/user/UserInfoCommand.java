package com.uroria.backend.impl.command.user;

import com.uroria.base.command.Command;
import com.uroria.base.command.Commander;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public final class UserInfoCommand extends Command {
    public UserInfoCommand() {
        super("info", "command.backend.control.user.info", List.of());
    }

    @Override
    protected boolean execute(@NonNull Commander commander, @NotNull @NonNull String[] args, @NonNull Map<String, String> arguments) {
        String string = arguments.get("user");
        if (string == null) return false;
        commander.sendMessage(Component.text("Here should be an info about " + string));
        return true;
    }

    @Override
    protected void offer(@NonNull Commander commander, @NonNull List<String> offers, @Nullable String last, @Nullable String current) {
        if (current != null) {
            if ("info".startsWith(current)) offers.add("info");
            return;
        }
        offers.add("info");
    }
}
