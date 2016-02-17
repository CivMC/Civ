package com.github.devotedmc.hiddenore;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class DropConfig {
	public String prefix;
	public byte subtype;
	public DropLimitsConfig limits;
	private Map<String, DropLimitsConfig> biomeLimits;
	
	public DropConfig(String prefix, byte subtype, DropLimitsConfig limits) {
		this.prefix = prefix;
		this.subtype = subtype;
		this.limits = limits;
		this.biomeLimits = new HashMap<String, DropLimitsConfig>();
	}
	
	public void addBiomeLimits(String biome, DropLimitsConfig limits){
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
	
	public boolean dropsWithTool(String biome, String tool) {
		Set<String> t = getTools(biome);
		return (t == null || t.isEmpty()) ? true : t.contains(tool);
	}
	
	public int getMinY(String biome) {
		return biomeLimits.containsKey(biome) ? biomeLimits.get(biome).minY : limits.minY;
	}
	
	public int getMaxY(String biome) {
		return biomeLimits.containsKey(biome) ? biomeLimits.get(biome).maxY : limits.maxY;
	}
	
	public int getMinAmount(String biome) {
		return biomeLimits.containsKey(biome) ? biomeLimits.get(biome).minAmount : limits.minAmount;
	}
	
	public int getMaxAmount(String biome) {
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
	public ItemStack renderDrop(String drop, String biome) {
		int min = getMinAmount(biome);
		int max = getMaxAmount(biome);
		int amount = (min == max) ? min : (int) ((max - min) * Math.random() + min);
		HiddenOre.getPlugin().getLogger().log(Level.INFO, "Item {0}:{1} [{2}, {3}] = {4}",
				new Object[] { drop, this.subtype, min, max, amount});
		return new ItemStack(Material.getMaterial(drop), amount, this.subtype);
	}
}
