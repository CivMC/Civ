package net.civmc.kitpvp.warp.handler;

import net.civmc.kitpvp.warp.database.DatabaseManager;
import net.civmc.kitpvp.warp.util.Cache;
import net.civmc.kitpvp.warp.util.Warp;
import java.util.List;
import net.civmc.kitpvp.KitPvpPlugin;
import net.civmc.kitpvp.warp.util.EnumUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarpsHandler {

    private final KitPvpPlugin plugin;
    private final DatabaseManager databaseManager;
    private final Cache cache;

    public WarpsHandler(KitPvpPlugin plugin, DatabaseManager databaseManager, Cache cache) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.cache = cache;
    }

    public List<String> getWarps() {
        return cache.getWarpIndex();
    }

    public boolean addWarp(CommandSender sender, String[] args) {
        if (args.length != 3) {
            return false;
        }

        if (!sender.hasPermission("warps.manage")) {
            sender.sendMessage("You do not have permission to execute this command");
            return true;
        }

        if (cache.getWarpIndex().contains(args[1].toLowerCase())) {
            sender.sendMessage("Warp %s already exists".formatted(args[1]));
            return true;
        }

        if (EnumUtil.getGamemode(args[2]) == null) {
            sender.sendMessage("Unknown gamemode");
            return false;
        }

        Location loc = ((Player) sender).getLocation();
        Warp warp = new Warp(
            args[1],
            loc.getWorld().getName(),
            loc.getX(),
            loc.getY(),
            loc.getZ(),
            loc.getPitch(),
            loc.getYaw(),
            args[2]
        );

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                if (databaseManager.addWarp(warp)) {
                    cache.addWarp(warp);
                    sender.sendMessage("Successfully saved warp %s".formatted(warp.name()));
                }
            }
        });
        return true;
    }

    public boolean listWarps(CommandSender sender, String[] args) {
        if (args.length != 1) {
            return false;
        }

        if (cache.getWarpIndex().isEmpty()) {
            sender.sendMessage("There are no warps");
            return true;
        }

        StringBuilder builder = new StringBuilder();
        for (String s : cache.getWarpIndex()) {
            builder.append("%s, ".formatted(s));
        }
        builder.deleteCharAt(builder.lastIndexOf(","));

        sender.sendMessage(builder.toString());

        return true;
    }

    public boolean deleteWarp(CommandSender sender, String[] args) {
        if (args.length != 2) {
            return false;
        }

        if (!sender.hasPermission("warps.manage")) {
            sender.sendMessage("You do not have permission to execute this command");
            return true;
        }

        if (!cache.getWarpIndex().contains(args[1])) {
            sender.sendMessage("Warp %s does not exist".formatted(args[1]));
            return true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                if (databaseManager.deleteWarp(args[1])) {
                    cache.deleteWarp(args[1]);
                    sender.sendMessage("Successfully deleted warp %s".formatted(args[1]));
                }
            }
        });
        return true;
    }
}
