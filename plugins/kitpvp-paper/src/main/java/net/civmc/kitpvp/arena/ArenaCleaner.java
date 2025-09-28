package net.civmc.kitpvp.arena;

import net.civmc.kitpvp.KitPvpPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class ArenaCleaner implements Runnable {
    private static final long EMPTY_ARENA_DURATION = TimeUnit.MINUTES.toMillis(10);

    private final Map<LoadedArena, Long> emptyArenas = new HashMap<>();
    private final ArenaManager arenaManager;

    public ArenaCleaner(ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @Override
    public void run() {
        try {
            for (LoadedArena loadedArena : this.arenaManager.getArenas()) {
                String arenaName = this.arenaManager.getArenaName(loadedArena);
                World world = Bukkit.getWorld(arenaName);
                if (world == null) {
                    continue;
                }
                if (loadedArena.owner() == null) {
                    continue;
                }

                if (world.getPlayerCount() == 0 && Bukkit.getPlayer(loadedArena.owner().getId()) == null) {
                    emptyArenas.putIfAbsent(loadedArena, System.currentTimeMillis());
                } else {
                    emptyArenas.remove(loadedArena);
                }
            }
            Iterator<Map.Entry<LoadedArena, Long>> iterator = emptyArenas.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<LoadedArena, Long> entry = iterator.next();
                if (entry.getValue() + EMPTY_ARENA_DURATION < System.currentTimeMillis()) {
                    if (this.arenaManager.getArenas().contains(entry.getKey())) {
                        this.arenaManager.deleteArena(entry.getKey().owner());
                    }
                    iterator.remove();
                }
            }
        } catch (RuntimeException ex) {
            JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().log(Level.WARNING, "Cleaning arenas", ex);
        }
    }
}
