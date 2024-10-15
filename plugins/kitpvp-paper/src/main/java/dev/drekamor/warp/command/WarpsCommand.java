package dev.drekamor.warp.command;

import dev.drekamor.warp.handler.WarpsHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WarpsCommand implements TabExecutor {
    private final WarpsHandler handler;

    public WarpsCommand(WarpsHandler handler) {
        this.handler = handler;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("Only a player can execute this command");
            return true;
        }

        if(args.length < 1 || args.length > 3) {
            return false;
        }

        return switch (args[0]) {
            case "add" -> handler.addWarp(sender, args);
            case "list" -> handler.listWarps(sender, args);
            case "delete" -> handler.deleteWarp(sender, args);
            default -> false;
        };
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(!sender.hasPermission("warps.manage")) {
            return switch (args.length) {
                case 1 -> List.of("list");
                default -> null;
            };
        }
        return switch (args.length) {
            case 1 -> List.of("add", "list", "delete");
            case 2 -> handler.getWarps();
            case 3 -> List.of("survival", "creative", "spectator", "adventure");
            default -> null;
        };
    }
}
