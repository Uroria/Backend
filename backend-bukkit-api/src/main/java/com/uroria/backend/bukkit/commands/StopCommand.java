package com.uroria.backend.bukkit.commands;

import com.uroria.backend.bukkit.BackendAPIImpl;
import com.uroria.backend.common.server.BackendServer;
import com.uroria.backend.common.server.ServerStatus;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

public final class StopCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                player.kickPlayer("Closed");
            });
            try {
                BackendServer server = BackendAPIImpl.getAPI().getServerManager().getThisServer();
                server.setStatus(ServerStatus.CLOSED);
                BackendAPIImpl.getAPI().getServerManager().updateServer(server);
            } catch (Exception exception) {
                Bukkit.shutdown();
            }
        }
        return false;
    }
}
