package com.untamedears.realisticbiomes.utils;

import java.util.*;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.untamedears.realisticbiomes.RealisticBiomes;
import com.untamedears.realisticbiomes.growthconfig.PlantGrowthConfig;
import com.untamedears.realisticbiomes.growthconfig.inner.BiomeGrowthConfig;
import com.untamedears.realisticbiomes.growthconfig.inner.PersistentGrowthConfig;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Biome;

import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.api.ItemNames;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.civmodcore.inventorygui.LClickable;
import vg.civcraft.mc.civmodcore.inventorygui.components.ComponableInventory;
import vg.civcraft.mc.civmodcore.inventorygui.components.ContentAligners;
import vg.civcraft.mc.civmodcore.inventorygui.components.InventoryComponent;
import vg.civcraft.mc.civmodcore.inventorygui.components.Scrollbar;
import vg.civcraft.mc.civmodcore.inventorygui.components.SlotPredicates;
import vg.civcraft.mc.civmodcore.inventorygui.components.StaticDisplaySection;
import vg.civcraft.mc.civmodcore.inventorygui.history.HistoryItem;
import vg.civcraft.mc.civmodcore.inventorygui.history.HistoryTracker;
import vg.civcraft.mc.civmodcore.util.TextUtil;


public class RealisticBiomesGUI {
	private final Player player;
	private InventoryComponent topHalfComponent;
	private ComponableInventory inventory;
	private HistoryTracker<RBCHistoryItem> history;
	private static final DecimalFormat decimalFormat = new DecimalFormat("0.00");

	public RealisticBiomesGUI(Player player) {
		this.player = player;
		this.history = new HistoryTracker<>();
	}

	public void showRBOverview(boolean addToHistory) {
		Biome currentBiome = player.getLocation().getBlock().getBiome();
		if (inventory == null) {
			String BiomeText = currentBiome.toString().toLowerCase().replace("_"," ");
			String cleanBiomeText = BiomeText.substring(0, 1).toUpperCase() + BiomeText.substring(1, Math.min(BiomeText.length(), 25)).trim();
			if (cleanBiomeText.length() < BiomeText.length()) cleanBiomeText += "...";
			inventory = new ComponableInventory(ChatColor.DARK_GRAY + cleanBiomeText, 6, player);
		} else {
			inventory.clear();
			topHalfComponent = null;
		}
		if (addToHistory) {
			history.add(new RBCHistoryItem() {

				@Override
				public void setStateTo() {
					showRBOverview(false);
				}

				@Override
				String toText() {
					return "Crop overview";
				}
			});
		}
		List<IClickable> clicks = new ArrayList<>();
		Set<PlantGrowthConfig> plantConfigSet = RealisticBiomes.getInstance().getConfigManager().getPlantGrowthConfigs();

		// Sort crop materials by biome multiplier value (descending)
		HashMap<Material, Double> plantMats = new HashMap<>();
		for (PlantGrowthConfig config: plantConfigSet) {
			double biomeMultiplier = config.getBiomeGrowthConfig().getBiomeMultiplier(currentBiome);
			plantMats.put(config.getMaterial(),biomeMultiplier);
		}
		Map<Material, Double> sortedPlantMats = plantMats.entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		for (Material m : sortedPlantMats.keySet()){
			PlantGrowthConfig plantGrowthConfig = plantConfigSet.stream()
					.filter(pcl -> m.equals(pcl.getMaterial()))
					.findAny()
					.orElse(null);
			assert plantGrowthConfig != null;
			Material representation;
			if (plantGrowthConfig.getMaterial() == Material.COCOA){ // COCOA does not have an item
				representation = Material.COCOA_BEANS;
			} else if(!plantGrowthConfig.getMaterial().isItem()){
				representation = Material.BARRIER;
			} else{
				representation = plantGrowthConfig.getMaterial();
			}
			BiomeGrowthConfig config = plantGrowthConfig.getBiomeGrowthConfig();
			double biomeMultiplier = config.getBiomeMultiplier(currentBiome);
			long timeNeeded = ((PersistentGrowthConfig) config).getTotalGrowthTimeNeeded(currentBiome);
			ItemStack is = new ItemStack(representation);
			ItemAPI.setDisplayName(is, ChatColor.DARK_GREEN + ItemNames.getItemName(plantGrowthConfig.getMaterial()));
			List<String> lore = new ArrayList<>();
			if (config instanceof PersistentGrowthConfig) {
				if (biomeMultiplier == 0) {
					lore.add(ChatColor.DARK_AQUA + "Time: " + ChatColor.RED + "∞");
				}else {
					lore.add(ChatColor.DARK_AQUA + "Time: " + ChatColor.GRAY + TextUtil.formatDuration(timeNeeded, TimeUnit.MILLISECONDS));
					ItemAPI.addGlow(is);
				}
			} else {
				double baseMultiplier = config.getNaturalProgressChance(currentBiome);
				if (baseMultiplier == 0) {
					lore.add(ChatColor.DARK_AQUA + "% chance: " + ChatColor.RED + "0.0");
				}else {
					lore.add(ChatColor.DARK_AQUA + "% chance: " + ChatColor.GRAY + decimalFormat.format(baseMultiplier*100));
					ItemAPI.addGlow(is);
				}
			}
			lore.add(ChatColor.DARK_AQUA + "Biome Multiplier: " + ChatColor.GRAY + biomeMultiplier);
			ItemAPI.addLore(is, lore);
			clicks.add(new LClickable(is, p -> {}));
		}
		Scrollbar middleBar = new Scrollbar(clicks, 45, 5, ContentAligners.getCenteredInOrder(clicks.size(), 45, 9));
		inventory.addComponent(middleBar, SlotPredicates.rows(5));
		StaticDisplaySection bottomLine = new StaticDisplaySection(9);
		if (history.hasPrevious()) {
			bottomLine.set(getBackClick(), 4);
		}
		inventory.addComponent(bottomLine, SlotPredicates.rows(1));
		inventory.show();
	}

	private IClickable getBackClick() {
		if (!history.hasPrevious()) {
			return null;
		}
		RBCHistoryItem previous = history.peekPrevious();
		LClickable click = new LClickable(Material.SPECTRAL_ARROW, ChatColor.GOLD + "Show previous page", p -> {
			RBCHistoryItem actualPrevious = history.goBack();
			actualPrevious.setStateTo();
		});
		ItemAPI.addLore(click.getItemStack(), ChatColor.GREEN + previous.toText());
		return click;
	}

	private abstract class RBCHistoryItem implements HistoryItem {
		abstract String toText();
	}

}
