package net.civmc.kitpvp.arena;

import net.civmc.kitpvp.spawn.SpawnProvider;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class RespawnListener implements Listener {

    private final ArenaManager manager;
    private final SpawnProvider provider;

    public RespawnListener(ArenaManager manager, SpawnProvider provider) {
        this.manager = manager;
        this.provider = provider;
    }


    @EventHandler
    public void on(PlayerRespawnEvent event) {
        String worldName = event.getPlayer().getWorld().getName();
        if (manager.isArena(worldName)) {
            for (LoadedArena arena : manager.getArenas()) {
                if (manager.getArenaName(arena.arena().name(), arena.owner()).equals(worldName)) {
                    event.setRespawnLocation(arena.arena().spawn());
                }
            }
        }
    }
}
