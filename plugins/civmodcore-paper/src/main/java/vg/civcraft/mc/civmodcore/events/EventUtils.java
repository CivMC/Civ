package vg.civcraft.mc.civmodcore.events;

import java.util.Objects;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class EventUtils {
    /** Convenience shortcut */
    public static void registerListener(
        final @NotNull JavaPlugin plugin,
        final @NotNull Listener listener
    ) {
        Objects.requireNonNull(listener, "Cannot register a listener if it's null, you dummy");
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    /**
     * Updates the item used in the given interact event to the given item. This utility assumes that the interaction
     * was done with the player's hands.
     *
     * @param event The interact event.
     * @param item  The item to set.
     */
    public static void setPlayerInteractItem(@NotNull final PlayerInteractEvent event,
                                             final ItemStack item) {
        event.getPlayer().getInventory().setItem(Objects.requireNonNull(event.getHand()), item);
    }

}
