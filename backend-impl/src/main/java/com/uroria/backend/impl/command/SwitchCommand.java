package com.uroria.backend.impl.command;

import com.uroria.backend.impl.command.user.UserCommand;
import com.uroria.base.command.Command;
import com.uroria.base.command.Commander;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public final class SwitchCommand extends Command {
    private final List<String> options;

    SwitchCommand() {
        super("switch", "command.backend.control", List.of(), new UserCommand());
        this.options = new ObjectArrayList<>();
        options.add("user");
        options.add("server");
        options.add("server-group");
        options.add("proxy");
        options.add("perm-group");
    }

    @Override
    protected boolean execute(@NonNull Commander commander, @NotNull @NonNull String[] args, @NonNull Map<String, String> arguments) {
        return false;
    }

    @Override
    protected void offer(@NonNull Commander commander, @NonNull List<String> offers, @Nullable String last, @Nullable String current) {
        offers.clear();
        if (current == null) {
            offers.addAll(options);
            return;
        }
        for (String option : options) {
            if (option.startsWith(current)) offers.add(option);
        }
    }
}
