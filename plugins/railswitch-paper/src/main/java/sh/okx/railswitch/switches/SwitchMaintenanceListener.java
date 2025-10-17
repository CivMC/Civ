package sh.okx.railswitch.switches;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import sh.okx.railswitch.RailSwitchPlugin;
import sh.okx.railswitch.storage.RailSwitchKey;
import sh.okx.railswitch.storage.RailSwitchStorage;

/**
 * Handles cleanup of stored switch data when detector rails are destroyed.
 */
public record SwitchMaintenanceListener(RailSwitchPlugin plugin) implements Listener {

    /**
     * Handles block break events to clean up switch data.
     *
     * @param event The block break event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        removeSwitch(event.getBlock());
    }

    /**
     * Handles block explosion events to clean up switch data.
     *
     * @param event The block explode event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        cleanupBlocks(event.blockList());
    }

    /**
     * Handles entity explosion events to clean up switch data.
     *
     * @param event The entity explode event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        cleanupBlocks(event.blockList());
    }

    private void cleanupBlocks(List<Block> blocks) {
        for (Block block : blocks) {
            removeSwitch(block);
        }
    }

    private void removeSwitch(Block block) {
        if (block == null || block.getType() != Material.DETECTOR_RAIL) {
            return;
        }
        RailSwitchStorage storage = plugin.getRailSwitchStorage();
        if (storage == null) {
            return;
        }
        RailSwitchKey key = RailSwitchKey.from(block);
        if (storage.remove(key).isPresent()) {
            SwitchConfigurationSessionManager sessionManager = plugin.getConfigurationSessionManager();
            if (sessionManager != null) {
                sessionManager.cancelSessionsFor(key, "Detector rail removed; editing session cancelled.");
            }
        }
    }
}
