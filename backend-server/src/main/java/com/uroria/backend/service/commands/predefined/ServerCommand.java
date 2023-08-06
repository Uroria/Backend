package com.uroria.backend.service.commands.predefined;

import com.uroria.backend.service.commands.Command;

import java.util.List;

public final class ServerCommand extends Command {

    @Override
    protected void run(String... args) {

    }

    @Override
    protected List<String> tabComplete(String... args) {
        return null;
    }
}
