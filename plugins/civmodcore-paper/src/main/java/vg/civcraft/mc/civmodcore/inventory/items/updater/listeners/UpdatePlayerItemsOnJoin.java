package vg.civcraft.mc.civmodcore.inventory.items.updater.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.items.updater.ItemUpdater;

public interface UpdatePlayerItemsOnJoin extends ItemUpdater, Listener {
    @EventHandler(
        priority = EventPriority.LOWEST,
        ignoreCancelled = true
    )
    default void updatePlayerItemsOnJoin(
        final @NotNull PlayerJoinEvent event
    ) {
        final Player player = event.getPlayer();
        if (player.hasPermission("cmc.debug")) {
            // Do not auto-upgrade items if they have this debug permission as there may be reasons why they are
            // carrying legacy items.
            return;
        }
        ItemUpdater.updateInventory(this, player.getInventory());
        ItemUpdater.updateInventory(this, player.getEnderChest());
    }
}
