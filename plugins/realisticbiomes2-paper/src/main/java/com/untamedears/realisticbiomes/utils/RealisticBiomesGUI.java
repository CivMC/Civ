package com.untamedears.realisticbiomes.utils;

import com.untamedears.realisticbiomes.RealisticBiomes;
import com.untamedears.realisticbiomes.growthconfig.PlantGrowthConfig;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.gui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;
import vg.civcraft.mc.civmodcore.inventory.gui.components.ComponableInventory;
import vg.civcraft.mc.civmodcore.inventory.gui.components.ContentAligners;
import vg.civcraft.mc.civmodcore.inventory.gui.components.Scrollbar;
import vg.civcraft.mc.civmodcore.inventory.gui.components.SlotPredicates;
import vg.civcraft.mc.civmodcore.inventory.gui.components.StaticDisplaySection;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.utilities.TextUtil;

public class RealisticBiomesGUI {

    private final Player player;
    private ComponableInventory inventory;
    private static final DecimalFormat decimalFormat = new DecimalFormat("0.00");

    public RealisticBiomesGUI(Player player) {
        this.player = player;
    }

    /**
     * Show GUI containing all RB effected crops, sorted by their biome multiplier in current biome.
     */
    public void showRBOverview(Biome biome) {
        Biome currentBiome;
        if (biome == null) {
            currentBiome = player.getLocation().getBlock().getBiome();
        } else {
            currentBiome = biome;
        }
        if (inventory == null) {
            String biomeText = (currentBiome.toString().toLowerCase()).replace("_", " ");
            biomeText = StringUtils.capitalize(biomeText);
            biomeText = StringUtils.abbreviate(biomeText, 30);
            inventory = new ComponableInventory(ChatColor.DARK_GRAY + biomeText, 6, player);
        } else {
            inventory.clear();
        }
        List<IClickable> clicks = new LinkedList<>();
        List<PlantGrowthConfig> plantConfigs = new ArrayList<>(
            RealisticBiomes.getInstance().getConfigManager().getPlantGrowthConfigs());
        plantConfigs.sort(Comparator.<PlantGrowthConfig>comparingDouble(p -> {
            if (p.getBreaker() == null) {
                return -1;
            } else {
                int maxYield = p.getBreaker().maxYield();
                return maxYield * RealisticBiomes.getInstance().getConfigManager().getBiomeConfiguration().getYield(player.getLocation().getBlock(), p.getClimate(), p.getBreaker().type(), maxYield);
            }
        }).reversed().thenComparing(p -> p.getItem().getType()));
        for (PlantGrowthConfig plant : plantConfigs) {
            Material representation = plant.getItem().getType();
            if (representation == Material.COCOA) {
                representation = Material.COCOA_BEANS;
            } else if (!representation.isItem()) {
                representation = Material.BARRIER;
            }
            ItemStack is = new ItemStack(representation);
            ItemUtils.setDisplayName(is, ChatColor.DARK_GREEN + plant.getName());
            List<String> lore = new ArrayList<>();
            long timeNeeded = plant.getGrowthTime();
            lore.add(ChatColor.DARK_AQUA + "Time: "
                + ChatColor.GRAY + TextUtil.formatDuration(timeNeeded, TimeUnit.MILLISECONDS));
            if (plant.getBreaker() != null) {
                DecimalFormat format = new DecimalFormat("#.##");
                format.setRoundingMode(RoundingMode.DOWN);
                int maxYield = plant.getBreaker().maxYield();
                double yield = RealisticBiomes.getInstance().getConfigManager().getBiomeConfiguration().getYield(player.getLocation().getBlock(), plant.getClimate(), plant.getBreaker().type(), maxYield);
                lore.add(ChatColor.DARK_AQUA + "Yield: " + ChatColor.GRAY + format.format(yield * maxYield) + "x");
            }

            is.editMeta(itemMeta -> itemMeta.setEnchantmentGlintOverride(true));
            if (plant.getAllowBoneMeal() || !plant.getNeedsLight()
                || !plant.getSoilBoniPerLevel().isEmpty() || plant.getMaximumSoilLayers() != 0
                || plant.getMaximumSoilBonus() != Integer.MAX_VALUE || !plant.getGreenHouseRates().isEmpty()) {
                lore.add(ChatColor.DARK_GREEN + "---");
            }
            if (plant.getAllowBoneMeal()) {
                lore.add(ChatColor.DARK_AQUA + "Allow Bone Meal: " + ChatColor.GRAY + "true");
            }
            if (!plant.getNeedsLight()) {
                lore.add(ChatColor.DARK_AQUA + "Needs Light: " + ChatColor.GRAY + "false");
            }
            for (Map.Entry<Material, Double> entry : plant.getSoilBoniPerLevel().entrySet()) {
                lore.add(String.format("%sSoil Bonus: %s%s (%.2f)", ChatColor.DARK_AQUA,
                    ChatColor.GRAY, ItemUtils.getItemName(entry.getKey()), entry.getValue()));
            }
            if (plant.getMaximumSoilLayers() != 0) {
                lore.add(String.format("%sMax Soil Layers: %s%d", ChatColor.DARK_AQUA,
                    ChatColor.GRAY, plant.getMaximumSoilLayers()));
            }
            if (plant.getMaximumSoilBonus() < Integer.MAX_VALUE) {
                lore.add(String.format("%sMax Soil Bonus: %s%.2f", ChatColor.DARK_AQUA,
                    ChatColor.GRAY, plant.getMaximumSoilBonus()));
            }
            for (Map.Entry<Material, Double> entry : plant.getGreenHouseRates().entrySet()) {
                lore.add(String.format("%sGreen House Rate: %s%s (%.2f)", ChatColor.DARK_AQUA,
                    ChatColor.GRAY, ItemUtils.getItemName(entry.getKey()), entry.getValue()));
            }
            ItemUtils.addLore(is, lore);
            IClickable click = new DecorationStack(is);
            clicks.add(click);
        }
        Scrollbar middleBar = new Scrollbar(
            clicks, 45, 5, ContentAligners.getCenteredInOrder(clicks.size(), 45, 9));
        inventory.addComponent(middleBar, SlotPredicates.rows(5));
        StaticDisplaySection bottomLine = new StaticDisplaySection(9);
        inventory.addComponent(bottomLine, SlotPredicates.rows(1));
        inventory.show();
    }
}
