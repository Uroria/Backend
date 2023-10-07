package com.uroria.backend.service.commands.predefined;

import com.uroria.backend.service.BackendServer;
import com.uroria.backend.service.commands.Command;

import java.util.List;

public final class StopCommand extends Command {
    private final BackendServer server;

    public StopCommand(BackendServer server) {
        this.server = server;
    }

    @Override
    protected void run(String... args) {
        this.server.explicitShutdown();
    }

    @Override
    protected List<String> tabComplete(String... args) {
        return null;
    }
}
