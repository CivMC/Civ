package sh.okx.railswitch.switches;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import sh.okx.railswitch.RailSwitchPlugin;
import sh.okx.railswitch.storage.RailSwitchKey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages display entries for a specific player.
 */
public final class PlayerDisplays {
    private final RailSwitchPlugin plugin;
    private final Map<RailSwitchKey, DisplayEntry> displays = new HashMap<>();

    /**
     * Creates a new player displays manager.
     *
     * @param plugin The plugin instance
     */
    public PlayerDisplays(RailSwitchPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Synchronizes the displays with the given targets.
     *
     * @param targets The new targets to display
     * @param player The player
     */
    public void sync(Map<RailSwitchKey, List<DisplayTarget>> targets, Player player) {
        displays.entrySet().removeIf(entry -> {
            if (!targets.containsKey(entry.getKey())) {
                entry.getValue().destroy(player);
                return true;
            }
            return false;
        });

        for (Map.Entry<RailSwitchKey, List<DisplayTarget>> entry : targets.entrySet()) {
            DisplayEntry existing = displays.get(entry.getKey());
            if (existing != null && existing.matches(entry.getValue())) continue;

            if (existing != null) existing.destroy(player);
            DisplayEntry replacement = DisplayEntry.spawn(plugin, player, entry.getValue());
            if (replacement != null) {
                displays.put(entry.getKey(), replacement);
            }
        }
    }

    /**
     * Clears all displays for the player.
     *
     * @param player The player, or null if offline
     */
    public void clear(@Nullable Player player) {
        for (DisplayEntry entry : displays.values()) {
            entry.destroy(player);
        }
        displays.clear();
    }
}
