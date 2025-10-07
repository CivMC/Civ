package net.civmc.kitpvp.spawn;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class SpawnListener implements Listener {

    private final SpawnProvider provider;

    public SpawnListener(SpawnProvider provider) {
        this.provider = provider;
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        if (event.getPlayer().getWorld().getName().equals("world")) {
            Location spawn = this.provider.getSpawn();
            if (spawn != null) {
                event.getPlayer().teleport(spawn);
            }
        }
    }

    @EventHandler
    public void on(PlayerSpawnLocationEvent event) {
        // handles players joining after their arena was deleted
        if (event.getSpawnLocation().getWorld().getName().equals("world")) {
            Location spawn = this.provider.getSpawn();
            if (spawn != null) {
                if (event.getPlayer().getGameMode() == GameMode.SPECTATOR && !event.getPlayer().hasPermission("kitpvp.admin")) {
                    event.getPlayer().setGameMode(GameMode.SURVIVAL);
                }
                event.setSpawnLocation(spawn);
            }
        }
    }

    @EventHandler
    public void on(PlayerRespawnEvent event) {
        if (event.getPlayer().getWorld().getName().equals("world") || event.getPlayer().getWorld().getName().startsWith("rankedarena.")) {
            Location spawn = this.provider.getSpawn();
            if (spawn != null) {
                event.setRespawnLocation(spawn);
            }
        }
    }

    @EventHandler
    public void on(PlayerPostRespawnEvent event) {
        if (event.getRespawnedLocation().getWorld().getName().equals("world")
            && event.getPlayer().getGameMode() == GameMode.SPECTATOR
            && !event.getPlayer().hasPermission("kitpvp.admin")) {
            event.getPlayer().setGameMode(GameMode.SURVIVAL);
        }
    }
}
