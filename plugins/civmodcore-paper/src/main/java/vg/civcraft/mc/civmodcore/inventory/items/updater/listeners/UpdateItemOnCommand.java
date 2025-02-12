package vg.civcraft.mc.civmodcore.inventory.items.updater.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.items.updater.ItemUpdater;
import vg.civcraft.mc.civmodcore.inventory.items.updater.impl.CommandedUpdateItemEvent;

public interface UpdateItemOnCommand extends ItemUpdater, Listener {
    @EventHandler(
        priority = EventPriority.NORMAL,
        ignoreCancelled = true
    )
    default void updateItemOnCommand(
        final @NotNull CommandedUpdateItemEvent event
    ) {
        event.updateItemUsing(this);
    }
}
