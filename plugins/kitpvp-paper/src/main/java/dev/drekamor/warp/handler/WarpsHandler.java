package dev.drekamor.warp.handler;

import dev.drekamor.warp.WarpMain;
import static dev.drekamor.warp.util.EnumUtil.getGamemode;
import dev.drekamor.warp.util.Cache;
import dev.drekamor.warp.util.Warp;
import java.util.List;
import net.civmc.kitpvp.KitPvpPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarpsHandler {
    private final WarpMain main;
    private final KitPvpPlugin plugin;

    public WarpsHandler (WarpMain main, KitPvpPlugin plugin) {
        this.main = main;
        this.plugin = plugin;
    }

    public List<String> getWarps() {
        return Cache.getWarpIndex();
    }

    public boolean addWarp(CommandSender sender, String[] args) {
        if(args.length != 3) {
            return false;
        }

        if(!sender.hasPermission("warps.manage")) {
            sender.sendMessage("You do not have permission to execute this command");
            return true;
        }

        if(Cache.getWarpIndex().contains(args[1])) {
            sender.sendMessage("Warp %s already exists".formatted(args[1]));
            return true;
        }

        if(getGamemode(args[2]) == null) {
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
                if(main.getDatabaseManager().addWarp(warp)) {
                    Cache.addWarp(warp);
                    sender.sendMessage("Successfully saved warp %s".formatted(warp.name()));
                }
            }
        });
        return true;
    }

    public boolean listWarps(CommandSender sender, String[] args) {
        if(args.length != 1) {
            return false;
        }

        if(Cache.getWarpIndex().isEmpty()) {
            sender.sendMessage("There are no warps");
            return true;
        }

        StringBuilder builder = new StringBuilder();
        for(String s : Cache.getWarpIndex()) {
            builder.append("%s, ".formatted(s));
        }
        builder.deleteCharAt(builder.lastIndexOf(","));

        sender.sendMessage(builder.toString());

        return true;
    }

    public boolean deleteWarp(CommandSender sender, String[] args) {
        if(args.length != 2) {
            return false;
        }

        if(!sender.hasPermission("warps.manage")) {
            sender.sendMessage("You do not have permission to execute this command");
            return true;
        }

        if(!Cache.getWarpIndex().contains(args[1])) {
            sender.sendMessage("Warp %s does not exist".formatted(args[1]));
            return true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                if(main.getDatabaseManager().deleteWarp(args[1])) {
                    Cache.deleteWarp(args[1]);
                    sender.sendMessage("Successfully deleted warp %s".formatted(args[1]));
                }
            }
        });
        return true;
    }
}
