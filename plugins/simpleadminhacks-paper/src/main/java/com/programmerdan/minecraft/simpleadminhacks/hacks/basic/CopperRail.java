package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.destroystokyo.paper.MaterialTags;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CopperRail extends BasicHack {

    @AutoLoad
    private boolean deoxidise;

    @AutoLoad
    private double damage;

    private boolean formingBlock = false;

    public CopperRail(SimpleAdminHacks plugin, BasicHackConfig config) {
        super(plugin, config);
    }

    private enum CopperStage {
        UNAFFECTED(Material.COPPER_BLOCK, Material.WAXED_COPPER_BLOCK, 1.0f),
        EXPOSED(Material.EXPOSED_COPPER, Material.WAXED_EXPOSED_COPPER, 1.0f),
        WEATHERED(Material.WEATHERED_COPPER, Material.WAXED_WEATHERED_COPPER, 0.75f),
        OXIDIZED(Material.OXIDIZED_COPPER, Material.WAXED_OXIDIZED_COPPER, 0.0f);

        private final Material unwaxed;
        private final Material waxed;
        private final float chance;

        CopperStage(Material unwaxed, Material waxed, float chance) {
            this.unwaxed = unwaxed;
            this.waxed = waxed;
            this.chance = chance;
        }

        public static CopperStage from(Material mat) {
            for (CopperStage stage : values()) {
                if (stage.unwaxed == mat || stage.waxed == mat) return stage;
            }
            return null;
        }
    }

    private boolean isWaxed(Material material) {
        CopperStage stage = CopperStage.from(material);
        return stage != null && stage.waxed == material;
    }

    private Material getNextStage(Material material) {
        CopperStage current = CopperStage.from(material);
        if (current == null) return null;
        
        int nextIndex = current.ordinal() + 1;
        if (nextIndex >= CopperStage.values().length) return null;
        
        CopperStage nextStage = CopperStage.values()[nextIndex];
        return isWaxed(material) ? nextStage.waxed : nextStage.unwaxed;
    }

    private Material resetStage(Material material) {
        CopperStage current = CopperStage.from(material);
        if (current == null) return null;

        if (current == CopperStage.UNAFFECTED) {
            return isWaxed(material) ? current.unwaxed : null;
        }

        return isWaxed(material) ? CopperStage.UNAFFECTED.waxed : CopperStage.UNAFFECTED.unwaxed;
    }

    @EventHandler
    public void on(VehicleMoveEvent event) {
        if (this.damage <= 0 || !(event.getVehicle() instanceof Minecart minecart)) {
            return;
        }

        boolean hasPlayer = false;
        for (Entity entity : minecart.getPassengers()) {
            if (entity instanceof Player) {
                hasPlayer = true;
                break;
            }
        }

        if (!hasPlayer) {
            return;
        }

        Location to = event.getTo();
        Location from = event.getFrom();
        if (to.getBlockX() == from.getBlockX() && to.getBlockY() == from.getBlockY() && to.getBlockZ() == from.getBlockZ()) {
            return;
        }

        int signX = from.getBlockX() > to.getBlockX() ? 1 : -1;
        int signZ = from.getBlockZ() > to.getBlockZ() ? 1 : -1;
        boolean firstBlock = true;

        List<Block> copperBlocks = new ArrayList<>(4);
        for (int x = to.getBlockX(); x != to.getBlockX() + (from.getBlockX() - to.getBlockX()) + signX; x += signX) {
            for (int z = to.getBlockZ(); z != to.getBlockZ() + (from.getBlockZ() - to.getBlockZ()) + signZ; z += signZ) {
                if (firstBlock) {
                    firstBlock = false;
                    continue;
                }
                Location location = new Location(minecart.getWorld(), x, from.getY(), z);
                Block topCopperBlock = location.getBlock().getRelative(BlockFace.DOWN);
                if (getNextStage(topCopperBlock.getType()) != null) {
                    copperBlocks.add(topCopperBlock);
                }
                Block belowCopperBlock = topCopperBlock.getRelative(BlockFace.DOWN);
                if (getNextStage(belowCopperBlock.getType()) != null) {
                    copperBlocks.add(belowCopperBlock);
                }
            }
        }

        for (Block copperBlock : copperBlocks) {
            Material currentMaterial = copperBlock.getType();
            Material nextMaterial = getNextStage(currentMaterial);

            if (nextMaterial == null) {
                continue;
            }

            CopperStage stage = CopperStage.from(currentMaterial);
            float chanceModifier = (stage != null) ? stage.chance : 0.0f;

            if (this.damage * chanceModifier > ThreadLocalRandom.current().nextFloat()) {
                org.bukkit.block.BlockState newState = copperBlock.getState();
                newState.setType(nextMaterial);
                BlockFormEvent formEvent = new BlockFormEvent(copperBlock, newState);

                try {
                    formingBlock = true;
                    Bukkit.getPluginManager().callEvent(formEvent);
                    if (!formEvent.isCancelled()) {
                        newState.update(true);
                    }
                } finally {
                    formingBlock = false;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void on(PlayerInteractEvent event) {
        if (!this.deoxidise) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || !MaterialTags.AXES.isTagged(item)) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null || !MaterialTags.RAILS.isTagged(block)) {
            return;
        }

        boolean damaged = false;
        Player player = event.getPlayer();

        // First copper block directly underneath the rail
        Block topCopperBlock = block.getRelative(BlockFace.DOWN);
        Material previousTop = resetStage(topCopperBlock.getType());

        if (previousTop != null) {
            topCopperBlock.setType(previousTop);
            damaged = true;
            item.damage(1, player);
        }

        // Second copper block two spaces underneath the rail
        Block belowCopperBlock = topCopperBlock.getRelative(BlockFace.DOWN);
        Material previousBelow = resetStage(belowCopperBlock.getType());

        if (previousBelow != null && item.getType() != Material.AIR) {
            belowCopperBlock.setType(previousBelow);
            damaged = true;
            item.damage(1, player);
        }

        if (!damaged) {
            return;
        }

        block.getWorld().playSound(block.getLocation(), Sound.ITEM_AXE_SCRAPE, SoundCategory.BLOCKS, 1, 1);
        block.getWorld().playEffect(block.getLocation(), Effect.OXIDISED_COPPER_SCRAPE, 0);

        event.setCancelled(true);
    }

    @EventHandler
    public void on(BlockFormEvent event) {
        if (formingBlock) {
            return;
        }

        Block block = event.getBlock();
        if (getNextStage(block.getType()) == null) {
            return;
        }

        Block railAbove = block.getRelative(BlockFace.UP);
        if (!MaterialTags.RAILS.isTagged(railAbove)) {
            railAbove = railAbove.getRelative(BlockFace.UP);
        }

        if (!MaterialTags.RAILS.isTagged(railAbove)) {
            return;
        }

        event.setCancelled(true);
    }
}