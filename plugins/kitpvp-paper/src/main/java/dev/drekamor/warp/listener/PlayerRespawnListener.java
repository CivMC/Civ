package dev.drekamor.warp.listener;

import dev.drekamor.warp.util.Cache;
import dev.drekamor.warp.util.Warp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnListener implements Listener {

    public PlayerRespawnListener() {
    }

    @EventHandler
    private void onPlayerRespawn(PlayerRespawnEvent event) {
        if(Cache.getPlayerLocation(event.getPlayer()) == null) {
            return;
        }

        Warp warp = Cache.getPlayerLocation(event.getPlayer());
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
