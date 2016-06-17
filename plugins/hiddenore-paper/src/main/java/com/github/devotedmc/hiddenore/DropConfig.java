package com.github.devotedmc.hiddenore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.inventory.ItemStack;

public class DropConfig {
	public List<DropItemConfig> drops;
	public String dropName;
	public String prefix;
	public DropLimitsConfig limits;
	private Map<String, DropLimitsConfig> biomeLimits;

	public DropConfig(String dropName, List<DropItemConfig> drops, String prefix, DropLimitsConfig limits) {
		this.dropName = dropName;
		this.drops = drops;
		this.limits = limits;
		this.biomeLimits = new HashMap<String, DropLimitsConfig>();
	}

	public void addBiomeLimits(String biome, DropLimitsConfig limits) {
		biomeLimits.put(biome, limits);
	}

	public Set<String> getBiomes() {
		return biomeLimits.keySet();
	}

	public DropLimitsConfig getBiomeLimits(String biome) {
		return biomeLimits.get(biome);
	}

	public Set<String> getTools(String biome) {
		return biomeLimits.containsKey(biome) ? biomeLimits.get(biome).tools : limits.tools;
	}

	public boolean dropsWithTool(String biome, ItemStack tool) {
		Set<String> t = getTools(biome);
		if (t == null || t.isEmpty()) {
			return true;
		} else {
			return ToolConfig.dropsWithTool(t, tool);
		}
	}
	
	public ToolConfig dropsWithToolConfig(String biome, ItemStack tool) {
		Set<String> t = getTools(biome);
		if (t == null || t.isEmpty()) {
			return null; // ToolConfig.getAnyTool(tool);
		} else {
			return ToolConfig.getTool(t, tool);
		}
	}

	public int getMinY(String biome) {
		return biomeLimits.containsKey(biome) ? biomeLimits.get(biome).minY : limits.minY;
	}

	public int getMaxY(String biome) {
		return biomeLimits.containsKey(biome) ? biomeLimits.get(biome).maxY : limits.maxY;
	}

	public double getMinAmount(String biome) {
		return biomeLimits.containsKey(biome) ? biomeLimits.get(biome).minAmount : limits.minAmount;
	}

	public double getMaxAmount(String biome) {
		return biomeLimits.containsKey(biome) ? biomeLimits.get(biome).maxAmount : limits.maxAmount;
	}

	public double getChance(String biome) {
		return biomeLimits.containsKey(biome) ? biomeLimits.get(biome).chance : limits.chance;
	}

	/**
	 * Gives even chance of any amount.
	 * 
	 * @param drop
	 * @param biome
	 * @return
	 */
	public List<ItemStack> renderDrop(String biome, ToolConfig modify) {
		/** multipliers **/
		double min = getMinAmount(biome) + (modify != null ? modify.getMinAmountModifier() : 0.0);
		double max = getMaxAmount(biome) + (modify != null ? modify.getMaxAmountModifier() : 0.0);
		double amount = (min == max) ? min : (double) ((max - min) * Math.random() + min);
		
		HiddenOre.getPlugin().getLogger().log(Level.INFO, "Trigger drop {0} [{2}, {3}] = {4}", 
				new Object[] {dropName, min, max, amount});
		
		List<ItemStack> toDrop = new ArrayList<ItemStack>(drops.size());
		for (DropItemConfig item : drops) {
			toDrop.add(item.render(amount));
		}
		return toDrop;
	}
}
