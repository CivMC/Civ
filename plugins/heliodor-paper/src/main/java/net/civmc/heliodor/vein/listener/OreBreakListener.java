package net.civmc.heliodor.vein.listener;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.civmc.heliodor.vein.MeteoricIron;
import net.civmc.heliodor.vein.VeinCache;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class OreBreakListener implements Listener {

    private final NamespacedKey oreLocationsKey;

    public OreBreakListener(NamespacedKey oreLocationsKey) {
        this.oreLocationsKey = oreLocationsKey;
    }

    @EventHandler
    public void on(BlockBreakEvent event) {
        Block block = event.getBlock();

        PersistentDataContainer chunkPdc = block.getChunk().getPersistentDataContainer();
        int[] ints = chunkPdc.get(oreLocationsKey, PersistentDataType.INTEGER_ARRAY);
        if (ints == null) {
            return;
        }

        IntList list = new IntArrayList(ints);
        int index = -1;
        for (int i = 0; i < list.size(); i += 3) {
            if (list.getInt(i) == block.getX() && list.getInt(i + 1) == block.getY() && list.getInt(i + 2) == block.getZ()) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            return;
        }

        list.removeElements(index, index + 3);
        chunkPdc.set(oreLocationsKey, PersistentDataType.INTEGER_ARRAY, list.toIntArray());

        if (block.getType() != Material.RAW_IRON_BLOCK) {
            return;
        }

        event.setDropItems(false);
        event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), MeteoricIron.createMeteoricIronNugget());
    }
}
