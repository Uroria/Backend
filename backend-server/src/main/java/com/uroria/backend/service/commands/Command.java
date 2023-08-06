package com.uroria.backend.service.commands;

import java.util.List;

public abstract class Command {

    protected abstract void run(String... args);

    protected abstract List<String> tabComplete(String... args);

    protected final String getStringAt(String[] args, int index) {
        try {
            return args[index];
        } catch (Exception exception) {
            return null;
        }
    }
}
