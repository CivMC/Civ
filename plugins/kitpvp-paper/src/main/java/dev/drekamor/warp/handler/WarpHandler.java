package dev.drekamor.warp.handler;

import dev.drekamor.warp.WarpMain;
import static dev.drekamor.warp.util.EnumUtil.getGamemode;
import dev.drekamor.warp.util.Cache;
import dev.drekamor.warp.util.Warp;
import java.util.List;
import net.civmc.kitpvp.KitPvpPlugin;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarpHandler {
    private final WarpMain main;
    private final KitPvpPlugin plugin;

    public WarpHandler (WarpMain main, KitPvpPlugin plugin) {
        this.main = main;
        this.plugin = plugin;
    }


    public List<String> getWarps() {
        return Cache.getWarpIndex();
    }

    public boolean warp(CommandSender sender, String name) {
        if(!Cache.getWarpIndex().contains(name)) {
            sender.sendMessage("Warp %s does not exist".formatted(name));
            return true;
        }

        Warp warp = Cache.getWarp(name);
        Player player = (Player) sender;

        if(warp == null) {
            plugin.getLogger().warning("Failed to retrieve a warp assumed to be available");
            sender.sendMessage("Failed to warp to %s".formatted(name));
            return true;
        }

        Location location = new Location(
                Bukkit.getWorld(warp.world()),
                warp.x(),
                warp.y(),
                warp.z(),
                warp.yaw(),
                warp.pitch()
        );
        GameMode gameMode = getGamemode(warp.gamemode());

        player.teleport(location);
        player.setGameMode(gameMode);
        Cache.setPlayerLocation(player, warp);

        return true;
    }
}
