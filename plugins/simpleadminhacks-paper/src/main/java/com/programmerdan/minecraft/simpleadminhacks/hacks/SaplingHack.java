package com.programmerdan.minecraft.simpleadminhacks.hacks;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.configs.SaplingConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHack;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.inventory.ItemStack;

public class SaplingHack extends SimpleHack<SaplingConfig> implements Listener {

    private Random random = new Random();

    public SaplingHack(SimpleAdminHacks plugin, SaplingConfig config) {
        super(plugin, config);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.plugin.registerListener(this);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block brokenBlock = event.getBlock();
        if (!checkIfLeafBlock(brokenBlock)) {
            return;
        }
        if (!this.config.isAllowFortune()) {
            if (checkIfItemHasFortune(event.getPlayer().getActiveItem())) {
                return;
            }
        }
        double nextDouble = random.nextDouble();
        if (nextDouble <= this.config.getChanceMap().getOrDefault(brokenBlock.getType(), 0.0D)) {
            brokenBlock.getWorld().dropItemNaturally(brokenBlock.getLocation(), new ItemStack(getSaplingMaterial(brokenBlock.getType()), 1));
            logger.info("Dropped extra " + getSaplingMaterial(brokenBlock.getType()) + " from being broken at " + brokenBlock.getLocation());
        }
    }

    @EventHandler
    public void onLeafDecay(LeavesDecayEvent event) {
        Block decayedBlock = event.getBlock();
        if (!checkIfLeafBlock(decayedBlock)) {
            return;
        }
        double nextDouble = random.nextDouble();
        if (nextDouble <= this.config.getChanceMap().getOrDefault(decayedBlock.getType(), 0.0D)) {
            decayedBlock.getWorld().dropItemNaturally(decayedBlock.getLocation(), new ItemStack(getSaplingMaterial(decayedBlock.getType()), 1));
            logger.info("Dropped extra " + getSaplingMaterial(decayedBlock.getType()) + " from being broken at " + decayedBlock.getLocation());
        }
    }

    private boolean checkIfLeafBlock(Block block) {
        return Tag.LEAVES.isTagged(block.getType());
    }

    private boolean checkIfItemHasFortune(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        return itemStack.getEnchantments().containsKey(Enchantment.FORTUNE);
    }

    private Material getSaplingMaterial(Material material) {
        return switch (material) {
            case OAK_LEAVES -> Material.OAK_SAPLING;
            case BIRCH_LEAVES -> Material.BIRCH_SAPLING;
            case SPRUCE_LEAVES -> Material.SPRUCE_SAPLING;
            case JUNGLE_LEAVES -> Material.JUNGLE_SAPLING;
            case ACACIA_LEAVES -> Material.ACACIA_SAPLING;
            case DARK_OAK_LEAVES -> Material.DARK_OAK_SAPLING;
            case AZALEA_LEAVES -> Material.AZALEA;
            case MANGROVE_LEAVES -> Material.MANGROVE_PROPAGULE;
            case CHERRY_LEAVES -> Material.CHERRY_SAPLING;
            //We set this to air so nothing will drop if it comes to erroring
            //This needs to be updated any time a new tree is added, can we make it better?
            default -> Material.AIR;
        };
    }

    public static SaplingConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
        return new SaplingConfig(plugin, config);
    }
}
