package sh.okx.railswitch.switches;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.jetbrains.annotations.Nullable;
import sh.okx.railswitch.RailSwitchPlugin;
import org.bukkit.entity.Display;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a collection of text display entities for a set of display targets.
 */
public record DisplayEntry(RailSwitchPlugin plugin, List<DisplayTarget> targets, List<TextDisplay> entities) {

    /**
     * Spawns display entities for the given targets.
     *
     * @param plugin  The plugin instance
     * @param player  The player to show the entities to
     * @param targets The display targets
     * @return A new display entry, or null if spawning failed
     */
    public static DisplayEntry spawn(RailSwitchPlugin plugin, Player player, List<DisplayTarget> targets) {
        if (targets.isEmpty()) return null;
        List<TextDisplay> spawned = new ArrayList<>(targets.size());
        for (DisplayTarget target : targets) {
            Location spawnLocation = target.location();
            TextDisplay entity = spawnDisplay(plugin, player, spawnLocation, target.text());
            spawned.add(entity);
        }
        return new DisplayEntry(plugin, targets, spawned);
    }

    /**
     * Checks if this entry matches the given targets.
     *
     * @param other The targets to compare
     * @return True if they match
     */
    public boolean matches(List<DisplayTarget> other) {
        if (targets.size() != other.size()) return false;
        for (int i = 0; i < targets.size(); i++) {
            if (!targets.get(i).equals(other.get(i))) return false;
        }
        return true;
    }

    /**
     * Destroys all display entities.
     *
     * @param player The player to hide entities from, or null
     */
    public void destroy(@Nullable Player player) {
        for (TextDisplay entity : entities) {
            if (entity == null || !entity.isValid()) continue;
            if (player != null) player.hideEntity(plugin, entity);
            entity.remove();
        }
        entities.clear();
    }

    /**
     * Spawns a single text display entity.
     *
     * @param plugin   The plugin instance
     * @param player   The player to show the entity to
     * @param location The spawn location
     * @param text     The text to display
     * @return The spawned text display, or null if failed
     */
    private static TextDisplay spawnDisplay(RailSwitchPlugin plugin, Player player, Location location, Component text) {
        return location.getWorld().spawn(location, TextDisplay.class, display -> {
            display.text(text);
            display.setBillboard(Display.Billboard.CENTER);
            display.setShadowed(false);
            display.setGravity(false);
            display.setPersistent(false);
            display.setSeeThrough(true);
            display.setViewRange((float) plugin.getSwitchConfiguration().getDisplayRange() + 2.0F);
            display.setTeleportDuration(0);
            display.setVisibleByDefault(false);
            display.setAlignment(TextDisplay.TextAlignment.CENTER);
            player.showEntity(plugin, display);
        });
    }
}
