package com.untamedears.realisticbiomes.breaker;

import com.untamedears.realisticbiomes.growthconfig.BreakerConfig;
import com.untamedears.realisticbiomes.growthconfig.PlantGrowthConfig;
import com.untamedears.realisticbiomes.noise.BiomeConfiguration;
import com.untamedears.realisticbiomes.noise.Climate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

/*

      JUNGLE_LEAVES: 0.025
      DARK_OAK_LEAVES: 0.05
 */
public class BreakManager {

    private static final Map<Material, Material> PRIMARY_ITEM_TYPE = Map.ofEntries(
        Map.entry(Material.BEETROOTS, Material.BEETROOT),
        Map.entry(Material.WHEAT, Material.WHEAT),
        Map.entry(Material.POTATOES, Material.POTATO),
        Map.entry(Material.CARROTS, Material.CARROT),
        Map.entry(Material.COCOA, Material.COCOA_BEANS),
        Map.entry(Material.PUMPKIN, Material.PUMPKIN),
        Map.entry(Material.MELON, Material.MELON),
        Map.entry(Material.KELP_PLANT, Material.KELP),
        Map.entry(Material.OAK_LEAVES, Material.OAK_SAPLING),
        Map.entry(Material.BIRCH_LEAVES, Material.BIRCH_SAPLING),
        Map.entry(Material.ACACIA_LEAVES, Material.ACACIA_SAPLING),
        Map.entry(Material.DARK_OAK_LEAVES, Material.DARK_OAK_SAPLING),
        Map.entry(Material.MANGROVE_LEAVES, Material.MANGROVE_PROPAGULE),
        Map.entry(Material.CHERRY_LEAVES, Material.CHERRY_SAPLING),
        Map.entry(Material.SPRUCE_LEAVES, Material.SPRUCE_SAPLING),
        Map.entry(Material.JUNGLE_LEAVES, Material.JUNGLE_SAPLING),
        Map.entry(Material.PALE_OAK_LEAVES, Material.PALE_OAK_SAPLING),
        Map.entry(Material.SWEET_BERRY_BUSH, Material.SWEET_BERRIES),
        Map.entry(Material.CACTUS, Material.CACTUS),
        Map.entry(Material.WEEPING_VINES_PLANT, Material.WEEPING_VINES),
        Map.entry(Material.WEEPING_VINES, Material.WEEPING_VINES),
        Map.entry(Material.TWISTING_VINES_PLANT, Material.TWISTING_VINES),
        Map.entry(Material.TWISTING_VINES, Material.TWISTING_VINES),
        Map.entry(Material.SUGAR_CANE, Material.SUGAR_CANE),
        Map.entry(Material.BAMBOO, Material.BAMBOO),
        Map.entry(Material.RED_MUSHROOM_BLOCK, Material.RED_MUSHROOM),
        Map.entry(Material.BROWN_MUSHROOM_BLOCK, Material.BROWN_MUSHROOM),
        Map.entry(Material.NETHER_WART, Material.NETHER_WART)
    );

    private final BiomeConfiguration biomes;
    private final Map<Material, Integer> maxYield;
    private final Map<Material, Climate> climates;

    public BreakManager(BiomeConfiguration biomes, Map<Material, Integer> maxYield, Map<Material, Climate> climates) {
        this.biomes = biomes;
        this.maxYield = maxYield;
        this.climates = climates;
    }

    public static BreakManager fromPlantConfigs(BiomeConfiguration biomes, Set<PlantGrowthConfig> configs) {
        Map<Material, Integer> maxYield = new HashMap<>();
        Map<Material, Climate> climates = new HashMap<>();
        for (PlantGrowthConfig config : configs) {
            BreakerConfig breaker = config.getBreaker();
            if (breaker != null) {
                maxYield.put(breaker.type(), breaker.maxYield());
                climates.put(breaker.type(), config.getClimate());
                if (breaker.type2() != null) {
                    maxYield.put(breaker.type2(), breaker.maxYield());
                    climates.put(breaker.type2(), config.getClimate());
                }
            }
        }
        return new BreakManager(biomes, maxYield, climates);
    }

    public Map<Material, Climate> getClimates() {
        return this.climates;
    }

    public Map<Material, Integer> getMaxYield() {
        return maxYield;
    }

    public boolean isControlledCrop(Block block) {
        Climate climate = this.climates.get(block.getType());
        if (climate == null) {
            return false;
        }
        if (!this.maxYield.containsKey(block.getType())) {
            return false;
        }
        return true;
    }

    public List<ItemStack> calculateDrops(Block block, Collection<ItemStack> dropsCollection) {
        List<ItemStack> drops = new ArrayList<>(dropsCollection);
        if (!maxYield.containsKey(block.getType())) {
            return drops;
        }

        int multiplier = maxYield.get(block.getType());
        double yield = this.biomes.getYield(block, this.climates.get(block.getType()), block.getType(), multiplier);

        Material primary = PRIMARY_ITEM_TYPE.get(block.getType());
        if (primary == null) {
            return drops;
        }

        for (Iterator<ItemStack> iterator = drops.iterator(); iterator.hasNext(); ) {
            ItemStack drop = iterator.next();
            if (drop.getType() == primary) {
                int amount = (int) ((double) multiplier * yield * drop.getAmount());
                if (amount == 0) {
                    iterator.remove();
                } else {
                    drop.setAmount(amount);
                    HandPicked.markHandPicked(drop);
                }
            }
        }

        return drops;
    }
}
