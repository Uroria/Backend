package com.uroria.backend.impl.command;

import com.uroria.base.command.Command;
import com.uroria.base.command.Commander;
import com.uroria.base.utils.ComponentUtils;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public final class ControlCommand extends Command {
    public ControlCommand() {
        super("control", "command.backend.control", List.of("cl"), new SwitchCommand());
    }

    @Override
    protected boolean execute(@NonNull Commander commander, @NonNull String[] args, @NonNull Map<String, String> arguments) {
        if (args.length == 0) {
            commander.sendMessage(ComponentUtils.deserialize("<red>Invalid usage. Missing arguments.</red>"));
            return true;
        }
        return false;
    }

    @Override
    protected void offer(@NonNull Commander commander, @NonNull List<String> offers, @Nullable String last, @Nullable String current) {

    }
}
