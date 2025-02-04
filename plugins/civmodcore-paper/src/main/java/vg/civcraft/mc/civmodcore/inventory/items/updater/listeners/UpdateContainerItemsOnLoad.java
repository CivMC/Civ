package vg.civcraft.mc.civmodcore.inventory.items.updater.listeners;

import io.papermc.paper.block.TileStateInventoryHolder;
import org.bukkit.Chunk;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.items.updater.ItemUpdater;

public interface UpdateContainerItemsOnLoad extends ItemUpdater, Listener {
    @EventHandler(
        priority = EventPriority.LOWEST,
        ignoreCancelled = true
    )
    default void updateContainerItemsOnLoad(
        final @NotNull ChunkLoadEvent event
    ) {
        final Chunk chunk = event.getChunk();
        for (final BlockState state : chunk.getTileEntities()) {
            if (state instanceof final TileStateInventoryHolder holder) {
                ItemUpdater.updateInventory(this, holder.getInventory());
                holder.update();
            }
        }
    }
}
