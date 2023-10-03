package com.uroria.backend.service.commands.predefined;

import com.uroria.backend.service.commands.Command;

import java.util.List;

public final class StopCommand extends Command {
    private final BackendServerOld server;

    public StopCommand(BackendServerOld server) {
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
