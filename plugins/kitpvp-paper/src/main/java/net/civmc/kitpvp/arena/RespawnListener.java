package net.civmc.kitpvp.arena;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class RespawnListener implements Listener {

    private final ArenaManager manager;

    public RespawnListener(ArenaManager manager) {
        this.manager = manager;
    }


    @EventHandler
    public void on(PlayerRespawnEvent event) {
        World world = event.getPlayer().getWorld();
        String worldName = world.getName();
        if (manager.isArena(worldName)) {
            for (LoadedArena arena : manager.getArenas()) {
                if (manager.getArenaName(arena).equals(worldName)) {
                    Location spawn = arena.arena().spawn().clone();
                    spawn.setWorld(world);
                    event.setRespawnLocation(spawn);
                }
            }
        }
    }
}
