package com.programmerdan.minecraft.simpleadminhacks.hacks;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import io.papermc.paper.event.block.BlockBreakBlockEvent;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

public final class WaterMushroomDropHack extends BasicHack {

    @AutoLoad
    private double dropChance;
    @AutoLoad
    private int dropMultiplier;

    public WaterMushroomDropHack(final SimpleAdminHacks plugin, final BasicHackConfig config) {
        super(plugin, config);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreakBlock(final BlockBreakBlockEvent event) {
        final Material blockType = event.getBlock().getType();
        if (event.getSource().getType() != Material.WATER
            || (blockType != Material.RED_MUSHROOM && blockType != Material.BROWN_MUSHROOM)) {
            return;
        }

        if (ThreadLocalRandom.current().nextDouble() >= dropChance) {
            event.getDrops().clear();
            return;
        }
        for (final ItemStack drop : event.getDrops()) {
            drop.setAmount(drop.getAmount() * dropMultiplier);
        }
    }

}
