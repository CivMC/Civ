package dev.drekamor.warp.listener;

import dev.drekamor.warp.util.Cache;
import dev.drekamor.warp.util.Warp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnListener implements Listener {
    private final Cache cache;

    public PlayerRespawnListener(Cache cache) {
        this.cache = cache;
    }

    @EventHandler
    private void onPlayerRespawn(PlayerRespawnEvent event) {
        if(cache.getPlayerLocation(event.getPlayer()) == null) {
            return;
        }

        Warp warp = cache.getPlayerLocation(event.getPlayer());
        Location location = new Location(
                Bukkit.getWorld(warp.world()),
                warp.x(),
                warp.y(),
                warp.z(),
                warp.yaw(),
                warp.pitch()
        );

        event.setRespawnLocation(location);
    }
}
