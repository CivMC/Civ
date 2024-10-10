package dev.drekamor.warp.command;

import dev.drekamor.warp.handler.WarpHandler;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WarpCommand implements TabExecutor {
    private final WarpHandler handler;

    public WarpCommand(WarpHandler handler) {
        this.handler = handler;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("Only a player can execute this command");
            return true;
        }

        if(args.length != 1) {
            return false;
        }

        return handler.warp(sender, args[0]);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return switch (args.length) {
            case 1 -> handler.getWarps();
            default -> null;
        };
    }
}
