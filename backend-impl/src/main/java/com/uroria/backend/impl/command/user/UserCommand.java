package com.uroria.backend.impl.command.user;

import com.uroria.backend.impl.BackendWrapperImpl;
import com.uroria.backend.impl.user.UserManager;
import com.uroria.backend.impl.user.UserWrapper;
import com.uroria.base.command.Command;
import com.uroria.base.command.Commander;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public final class UserCommand extends Command {
    public UserCommand() {
        super("user", "command.backend.control.user", List.of(), new UserInfoCommand());
    }

    @Override
    protected boolean execute(@NonNull Commander commander, @NonNull String[] args, @NonNull Map<String, String> arguments) {
        return false;
    }

    @Override
    protected void offer(@NonNull Commander commander, @NonNull List<String> offers, @Nullable String last, @Nullable String current) {
        if (last == null || !last.equalsIgnoreCase("user")) return;
        offers.clear();
        BackendWrapperImpl instance = BackendWrapperImpl.getInstance();
        if (instance == null) return;
        UserManager userManager = instance.getUserManager();
        if (current == null) {
            for (UserWrapper wrapper : userManager.getWrappers()) {
                offers.add(wrapper.getUsername());
                offers.add(wrapper.getUniqueId().toString());
            }
            return;
        }
        for (UserWrapper wrapper : userManager.getWrappers()) {
            String uuid = wrapper.getUniqueId().toString();
            String username = wrapper.getUsername();
            if (uuid.startsWith(current)) offers.add(uuid);
            if (username.startsWith(current)) offers.add(username);
        }
    }
}
