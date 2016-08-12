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
	public boolean transformIfAble;
	public boolean dropIfTransformFails;
	private Map<String, DropLimitsConfig> biomeLimits;

	public DropConfig(String dropName, List<DropItemConfig> drops, boolean transformIfAble, boolean dropIfTransformFails, String prefix, DropLimitsConfig limits) {
		this.dropName = dropName;
		this.drops = drops;
		this.transformIfAble = transformIfAble;
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

	public List<String> getTools(String biome) {
		return biomeLimits.containsKey(biome) ? biomeLimits.get(biome).tools : limits.tools;
	}

	public boolean dropsWithTool(String biome, ItemStack tool) {
		List<String> t = getTools(biome);
		if (t == null || t.isEmpty()) {
			/*DIAGNOSTICS*HiddenOre.getPlugin().getLogger().log(Level.INFO, "Drops with {0} special case: no tools set", tool);*/
			return true;
		} else {
			boolean ret = ToolConfig.dropsWithTool(t, tool);
			/*DIAGNOSTICS*HiddenOre.getPlugin().getLogger().log(Level.INFO, "Drops with {0} normal case: {1}",
					new Object[] { tool, ret});*/
			return ret;
		}
	}
	
	public ToolConfig dropsWithToolConfig(String biome, ItemStack tool) {
		List<String> t = getTools(biome);
		if (t == null || t.isEmpty()) {
			/*DIAGNOSTICS*HiddenOre.getPlugin().getLogger().log(Level.INFO, "Out of [] picked null for {0}", tool);*/
			return null; // ToolConfig.getAnyTool(tool);
		} else {
			ToolConfig ret = ToolConfig.getTool(t, tool);
			/*DIAGNOSTICS*HiddenOre.getPlugin().getLogger().log(Level.INFO, "Out of {0} picked {1} for {2}", 
					new Object[] {t, ret, tool});*/
			return ret;
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
	 * @return Items that can be rendered.
	 */
	public List<ItemStack> renderDrop(String biome, ToolConfig modify) {
		/** multipliers **/
		double min = getMinAmount(biome) + (modify != null ? modify.getMinAmountModifier() : 0.0);
		double max = getMaxAmount(biome) + (modify != null ? modify.getMaxAmountModifier() : 0.0);
		double amount = (min == max) ? min : (double) ((max - min) * Math.random() + min);
		
		if (Config.isDebug)
			HiddenOre.getPlugin().getLogger().log(Level.INFO, "Trigger drop {0} [{1}, {2}] = {3}", 
					new Object[] {dropName, min, max, amount});
		
		List<ItemStack> toDrop = new ArrayList<ItemStack>(drops.size());
		for (DropItemConfig item : drops) {
			if (!this.transformIfAble || !item.canTransform())
				toDrop.add(item.render(amount));
		}
		return toDrop;
	}

	/**
	 * Gives even chance of any amount. 
	 * 
	 * @param drop
	 * @param biome
	 * @return Items that can be transformed.
	 */
	public List<ItemStack> renderTransform(String biome, ToolConfig modify) {
		/** multipliers **/
		double min = getMinAmount(biome) + (modify != null ? modify.getMinAmountModifier() : 0.0);
		double max = getMaxAmount(biome) + (modify != null ? modify.getMaxAmountModifier() : 0.0);
		double amount = (min == max) ? min : (double) ((max - min) * Math.random() + min);
		
		if (Config.isDebug)
			HiddenOre.getPlugin().getLogger().log(Level.INFO, "Trigger transform {0} [{1}, {2}] = {3}", 
					new Object[] {dropName, min, max, amount});
		
		List<ItemStack> toTransform = new ArrayList<ItemStack>(drops.size());
		for (DropItemConfig item : drops) {
			if (this.transformIfAble && item.canTransform())
				toTransform.add(item.render(amount));
		}
		return toTransform;
	}

	
	public boolean shouldTransformIfAble() {
		return this.transformIfAble;
	}
}
