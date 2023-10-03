package com.uroria.backend.service.commands;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import java.util.Arrays;

public final class CommandManager {
    private final Object2ObjectArrayMap<String, Command> commands;

    public CommandManager() {
        this.commands = new Object2ObjectArrayMap<>();
    }

    public void register(Command command, String commandString) {
        if (this.commands.containsKey(commandString)) return;
        this.commands.put(commandString, command);
    }

    public void runCommand(String full) {
        String[] split = full.split(" ");
        String commandString = split[0].toLowerCase();
        Command command = this.commands.get(commandString);
        if (command != null) {
            command.run(Arrays.copyOfRange(split, 1, split.length));
            return;
        }
        BackendServerOld.getLogger().info("Unknown command. Please type \"help\".");
    }
}
