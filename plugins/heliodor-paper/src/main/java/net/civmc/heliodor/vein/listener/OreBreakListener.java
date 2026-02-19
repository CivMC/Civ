package net.civmc.heliodor.vein.listener;

import net.civmc.heliodor.meteoriciron.MeteoricIron;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import vg.civcraft.mc.civmodcore.utilities.BlockPosPdc;

public class OreBreakListener implements Listener {

    private final NamespacedKey oreLocationsKey;

    public OreBreakListener(NamespacedKey oreLocationsKey) {
        this.oreLocationsKey = oreLocationsKey;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(BlockBreakEvent event) {
        Block block = event.getBlock();

        if (!BlockPosPdc.removeBlock(block.getChunk(), oreLocationsKey, block.getX(), block.getY(), block.getZ())) {
            return;
        }

        if (block.getType() != Material.RAW_IRON_BLOCK) {
            return;
        }

        event.setDropItems(false);
        event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation().add(0.5, 0.5, 0.5), MeteoricIron.createMeteoricIronNugget());
    }
}
