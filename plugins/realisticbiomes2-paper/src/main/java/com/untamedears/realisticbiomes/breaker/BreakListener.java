package com.untamedears.realisticbiomes.breaker;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.untamedears.realisticbiomes.utils.RBUtils;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.bukkit.ExplosionResult;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.inventory.ItemStack;

public class BreakListener implements Listener {

    private final BreakManager breakManager;

    private Set<Block> wasColumnLastTick = new HashSet<>();
    private Set<Block> wasColumnThisTick = new HashSet<>();

    public BreakListener(BreakManager breakManager) {
        this.breakManager = breakManager;
    }

    @EventHandler
    public void on(ServerTickEndEvent event) {
        Set<Block> temp = this.wasColumnLastTick;
        this.wasColumnLastTick = this.wasColumnThisTick;
        temp.clear();
        this.wasColumnThisTick = temp;
    }

    @EventHandler
    public void on(PlayerHarvestBlockEvent event) {
        Block block = event.getHarvestedBlock();
        if (!breakManager.isControlledCrop(block)) {
            return;
        }

        List<ItemStack> drops = breakManager.calculateDrops(block, event.getItemsHarvested());
        event.getItemsHarvested().clear();
        event.getItemsHarvested().addAll(drops);
    }

    private boolean isFarmable(Material material) {
        return material == Material.BEETROOTS || material == Material.WHEAT || material == Material.POTATOES || material == Material.CARROTS || material == Material.COCOA  || material == Material.NETHER_WART;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(BlockPlaceEvent event) {
        Material type = event.getBlock().getType();
        BlockFace direction = RBUtils.getGrowthDirection(type);
        if (direction == BlockFace.SELF) {
            return;
        }
        if (!breakManager.isControlledCrop(event.getBlock())) {
            return;
        }

        if (type != event.getBlock().getRelative(direction.getOppositeFace()).getType()) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPumpkinMelon(BlockPlaceEvent event) {
        Material type = event.getBlock().getType();
        if (type == Material.PUMPKIN || type == Material.MELON) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(EntityExplodeEvent event) {
        if (event.getExplosionResult() == ExplosionResult.TRIGGER_BLOCK || event.getExplosionResult() == ExplosionResult.KEEP) {
            return;
        }
        for (Iterator<Block> iterator = event.blockList().iterator(); iterator.hasNext(); ) {
            Block block = iterator.next();
            if (!breakManager.isControlledCrop(block)) {
                continue;
            }

            if (TreeDelegate.isTreeBlock(block.getType()) && !TreeDelegate.remove(block)) {
                continue;
            }
            for (ItemStack item : breakManager.calculateDrops(block, block.getDrops())) {
                block.getWorld().dropItemNaturally(block.getLocation().toCenterLocation(), item);
            }
            iterator.remove();
            block.setType(Material.AIR);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(BlockExplodeEvent event) {
        if (event.getExplosionResult() == ExplosionResult.TRIGGER_BLOCK || event.getExplosionResult() == ExplosionResult.KEEP) {
            return;
        }
        for (Iterator<Block> iterator = event.blockList().iterator(); iterator.hasNext(); ) {
            Block block = iterator.next();
            if (!breakManager.isControlledCrop(block)) {
                continue;
            }

            if (TreeDelegate.isTreeBlock(block.getType()) && !TreeDelegate.remove(block)) {
                continue;
            }
            for (ItemStack item : breakManager.calculateDrops(block, block.getDrops())) {
                block.getWorld().dropItemNaturally(block.getLocation().toCenterLocation(), item);
            }
            iterator.remove();
            block.setType(Material.AIR);
        }
    }

    // todo fix cactus popping off when it grows and having vanilla drops

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(LeavesDecayEvent event) {
        if (!breakManager.isControlledCrop(event.getBlock())) {
            return;
        }

        if (TreeDelegate.isTreeBlock(event.getBlock().getType()) && !TreeDelegate.remove(event.getBlock())) {
            return;
        }

        for (ItemStack item : breakManager.calculateDrops(event.getBlock(), event.getBlock().getDrops())) {
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation().toCenterLocation(), item);
        }
        event.getBlock().setType(Material.AIR);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(BlockDestroyEvent event) {
        if (!event.willDrop()) {
            return;
        }

        Material type = event.getBlock().getType();
        if (!breakManager.isControlledCrop(event.getBlock())) {
            return;
        }

        if (TreeDelegate.isTreeBlock(event.getBlock().getType()) && !TreeDelegate.remove(event.getBlock())) {
            return;
        }

        BlockFace direction = RBUtils.getGrowthDirection(type);
        if (direction != BlockFace.SELF) {
            Block below = event.getBlock().getRelative(direction.getOppositeFace());
            if (!this.wasColumnLastTick.contains(below)) {
                if (below.getType() != type && below.getType() != Material.AIR) {
                    // base blocks have normal drops
                } else {
                    event.setWillDrop(false);
                }
                return;
            }
            this.wasColumnThisTick.add(event.getBlock());
        }

        for (ItemStack item : breakManager.calculateDrops(event.getBlock(), event.getBlock().getDrops())) {
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation().toCenterLocation(), item);
        }
        event.setWillDrop(false);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(BlockBreakEvent event) {
        if (!event.isDropItems()) {
            return;
        }
        Block block = event.getBlock();

        if (!breakManager.isControlledCrop(block)) {
            return;
        }

        if (TreeDelegate.isTreeBlock(block.getType()) && !TreeDelegate.remove(block)) {
            return;
        }

        BlockFace direction = RBUtils.getGrowthDirection(block.getType());
        if (direction != BlockFace.SELF) {
            this.wasColumnLastTick.add(event.getBlock());
            if (event.getBlock().getType() != event.getBlock().getRelative(direction.getOppositeFace()).getType()) {
                // base blocks have normal drops
                return;
            }
        } else if (isFarmable(block.getType())
            && block.getBlockData() instanceof Ageable ageable
            && ageable.getAge() != ageable.getMaximumAge()) {
            return;
        }

        // todo fix cactus
        List<ItemStack> drops = breakManager.calculateDrops(block, block.getDrops(event.getPlayer().getInventory().getItemInMainHand(), event.getPlayer()));
        for (ItemStack item : drops) {
            block.getWorld().dropItemNaturally(block.getLocation().toCenterLocation(), item);
        }
        event.setDropItems(false);
    }
}
