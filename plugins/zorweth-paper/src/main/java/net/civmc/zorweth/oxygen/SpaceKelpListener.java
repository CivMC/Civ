package net.civmc.zorweth.oxygen;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;

public final class SpaceKelpListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDropItem(final BlockDropItemEvent event) {
        if (!isKelp(event.getBlockState().getType())) {
            return;
        }

        event.getItems().forEach(item -> {
            final ItemStack stack = item.getItemStack();
            if (stack.getType() == Material.KELP) {
                item.setItemStack(SpaceKelp.create(stack.getAmount()));
            }
        });
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockDestroy(final BlockDestroyEvent event) {
        final Block block = event.getBlock();
        if (!isKelp(block.getType()) || !event.willDrop()) {
            return;
        }

        event.setWillDrop(false);
        dropSpaceKelp(block, block.getDrops());
    }

    private void dropSpaceKelp(final Block block, final Iterable<ItemStack> drops) {
        int amount = 0;
        for (final ItemStack drop : drops) {
            if (drop.getType() == Material.KELP) {
                amount += drop.getAmount();
            }
        }
        if (amount > 0) {
            block.getWorld().dropItemNaturally(block.getLocation().toCenterLocation(), SpaceKelp.create(amount));
        }
    }

    private boolean isKelp(final Material material) {
        return material == Material.KELP || material == Material.KELP_PLANT;
    }
}
