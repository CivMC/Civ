package net.civmc.kitpvp.warp.handler;

import net.civmc.kitpvp.warp.util.Cache;
import net.civmc.kitpvp.warp.util.Warp;
import java.util.List;
import net.civmc.kitpvp.KitPvpPlugin;
import net.civmc.kitpvp.warp.util.EnumUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarpHandler {

    private final KitPvpPlugin plugin;
    private final Cache cache;

    public WarpHandler(KitPvpPlugin plugin, Cache cache) {
        this.plugin = plugin;
        this.cache = cache;
    }


    public List<String> getWarps() {
        return cache.getWarpIndex();
    }

    public boolean warp(CommandSender sender, String name) {
        if (!cache.getWarpIndex().contains(name.toLowerCase())) {
            sender.sendMessage("Warp %s does not exist".formatted(name));
            return true;
        }

        Warp warp = cache.getWarp(name);
        Player player = (Player) sender;

        if (warp == null) {
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
        GameMode gameMode = EnumUtil.getGamemode(warp.gamemode());

        player.teleport(location);
        player.setGameMode(gameMode);
        cache.setPlayerLocation(player, warp);

        return true;
    }
}
