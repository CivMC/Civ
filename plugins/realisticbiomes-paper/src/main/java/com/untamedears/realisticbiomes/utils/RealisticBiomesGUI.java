package com.untamedears.realisticbiomes.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.untamedears.realisticbiomes.RealisticBiomes;
import com.untamedears.realisticbiomes.growthconfig.PlantGrowthConfig;
import com.untamedears.realisticbiomes.growthconfig.inner.BiomeGrowthConfig;
import com.untamedears.realisticbiomes.growthconfig.inner.PersistentGrowthConfig;

import org.apache.commons.lang.StringUtils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.api.ItemNames;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.civmodcore.inventorygui.components.ComponableInventory;
import vg.civcraft.mc.civmodcore.inventorygui.components.ContentAligners;
import vg.civcraft.mc.civmodcore.inventorygui.components.Scrollbar;
import vg.civcraft.mc.civmodcore.inventorygui.components.SlotPredicates;
import vg.civcraft.mc.civmodcore.inventorygui.components.StaticDisplaySection;
import vg.civcraft.mc.civmodcore.util.TextUtil;

public class RealisticBiomesGUI {
	private final Player player;
	private ComponableInventory inventory;
	private static final DecimalFormat decimalFormat = new DecimalFormat("0.00");

	public RealisticBiomesGUI(Player player) {
		this.player = player;
	}

	public void showRBOverview() {
		Biome currentBiome = player.getLocation().getBlock().getBiome();
		if (inventory == null) {
			String biomeText = (currentBiome.toString().toLowerCase()).replace("_", " ");
			biomeText = StringUtils.capitalize(biomeText);
			biomeText = StringUtils.abbreviate(biomeText, 30);
			inventory = new ComponableInventory(ChatColor.DARK_GRAY + biomeText, 6, player);
		} else {
			inventory.clear();
		}
		List<IClickable> clicks = new LinkedList<>();
		List<PlantGrowthConfig> plantConfigs = new ArrayList<>(RealisticBiomes.getInstance().getConfigManager().getPlantGrowthConfigs());
		plantConfigs.sort((p1, p2) -> {
			int comparision = Double.compare(p2.getBiomeGrowthConfig().getBiomeMultiplier(currentBiome)
					, p1.getBiomeGrowthConfig().getBiomeMultiplier(currentBiome)); //reverse order
			if (comparision == 0) {
				return p1.getMaterial().compareTo(p2.getMaterial());
			} else {
				return comparision;
			}
		});
		for (PlantGrowthConfig plant : plantConfigs) {
			Material representation = plant.getMaterial();
			if (representation == Material.COCOA) {
				representation = Material.COCOA_BEANS;
			} else if (!representation.isItem()) {
				representation = Material.BARRIER;
			}
			ItemStack is = new ItemStack(representation);
			ItemAPI.setDisplayName(is, ChatColor.DARK_GREEN + ItemNames.getItemName(plant.getMaterial()));
			List<String> lore = new ArrayList<>();
			BiomeGrowthConfig config = plant.getBiomeGrowthConfig();
			double biomeMultiplier = config.getBiomeMultiplier(currentBiome);
			if (config instanceof PersistentGrowthConfig) {
				long timeNeeded = ((PersistentGrowthConfig) config).getTotalGrowthTimeNeeded(currentBiome);
				if (biomeMultiplier == 0) {
					lore.add(ChatColor.DARK_AQUA + "Time: " + ChatColor.RED + "∞");
				} else {
					lore.add(ChatColor.DARK_AQUA + "Time: " + ChatColor.GRAY + TextUtil.formatDuration(timeNeeded, TimeUnit.MILLISECONDS));
					ItemAPI.addGlow(is);
				}
			} else {
				double baseMultiplier = config.getNaturalProgressChance(currentBiome);
				if (baseMultiplier == 0) {
					lore.add(ChatColor.DARK_AQUA + "% chance: " + ChatColor.RED + "0.0");
				} else {
					lore.add(ChatColor.DARK_AQUA + "% chance: " + ChatColor.GRAY + decimalFormat.format(baseMultiplier * 100));
					ItemAPI.addGlow(is);
				}
			}
			lore.add(ChatColor.DARK_AQUA + "Biome Multiplier: " + ChatColor.GRAY + biomeMultiplier);
			ItemAPI.addLore(is, lore);
			IClickable click = new DecorationStack(is);
			clicks.add(click);
		}
		Scrollbar middleBar = new Scrollbar(clicks, 45, 5, ContentAligners.getCenteredInOrder(clicks.size(), 45, 9));
		inventory.addComponent(middleBar, SlotPredicates.rows(5));
		StaticDisplaySection bottomLine = new StaticDisplaySection(9);
		inventory.addComponent(bottomLine, SlotPredicates.rows(1));
		inventory.show();
	}
}
