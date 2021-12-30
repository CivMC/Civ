package net.minelink.ctplus.task;

import net.minelink.ctplus.CombatTagPlus;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ForceFieldTask extends BukkitRunnable {
    private final CombatTagPlus plugin;

    private final Map<UUID, Location> validLocations = new HashMap<>();

    private ForceFieldTask(CombatTagPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // Do nothing if anti-safezoning is disabled.
        if (!plugin.getSettings().denySafezone()) return;

        for (Player player : plugin.getPlayerCache().getPlayers()) {
            UUID playerId = player.getUniqueId();

            // Do nothing if player isn't even tagged.
            if (!plugin.getTagManager().isTagged(playerId)) continue;

            Location loc = player.getLocation();
            if (plugin.getHookManager().isPvpEnabledAt(loc)) {
                // Track the last PVP-enabled location that the player was in.
                validLocations.put(playerId, loc);
            } else if (validLocations.containsKey(playerId)) {
                // Teleport the player to the last valid PVP-enabled location.
                player.teleport(validLocations.get(playerId));
            }
        }
    }

    public static void run(CombatTagPlus plugin) {
        new ForceFieldTask(plugin).runTaskTimer(plugin, 1, 1);
    }
}
