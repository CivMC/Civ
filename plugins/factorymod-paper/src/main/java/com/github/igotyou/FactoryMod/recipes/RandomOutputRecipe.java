package com.github.igotyou.FactoryMod.recipes;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;

import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

public class RandomOutputRecipe extends InputRecipe {
	private Map<ItemMap, Double> outputs;
	private static Random rng;
	private ItemMap lowestChanceMap;

	public RandomOutputRecipe(String identifier, String name, int productionTime, ItemMap input,
			Map<ItemMap, Double> outputs, ItemMap displayOutput) {
		super(identifier, name, productionTime, input);
		this.outputs = outputs;
		if (rng == null) {
			rng = new Random();
		}
		if (displayOutput == null) {
			for(Entry <ItemMap, Double> entry : outputs.entrySet()) {
				if (lowestChanceMap == null) {
					lowestChanceMap = entry.getKey();
					continue;
				}
				if (entry.getValue() < outputs.get(lowestChanceMap)) {
					lowestChanceMap = entry.getKey();
				}
			}
			if (lowestChanceMap == null) {
				lowestChanceMap = new ItemMap(new ItemStack(Material.STONE));
			}
		} else {
			lowestChanceMap = displayOutput;
		}
	}

	public void applyEffect(Inventory i, FurnCraftChestFactory fccf) {
		logBeforeRecipeRun(i, fccf);
		ItemMap toRemove = input.clone();
		ItemMap toAdd = null;
		int counter = 0;
		while(counter < 20) {
			toAdd = getRandomOutput();
			if (toAdd != null) {
				toAdd = toAdd.clone();
				break;
			}
			else {
				counter++;
			}
		}
		if (toAdd == null) {
			FactoryMod.getPlugin().warning("Unable to find a random item to output. Recipe execution was cancelled," + fccf.getLogData());
			return;
		}
		if (toRemove.isContainedIn(i)) {
			if (toRemove.removeSafelyFrom(i)) {
				for(ItemStack is: toAdd.getItemStackRepresentation()) {
					i.addItem(is);
				}
			}
		}
		logAfterRecipeRun(i, fccf);
	}

	public Map<ItemMap, Double> getOutputs() {
		return outputs;
	}

	public ItemMap getRandomOutput() {
		double random = rng.nextDouble();
		double count = 0.0;
		for(Entry <ItemMap, Double> entry : outputs.entrySet()) {
			count += entry.getValue();
			if (count >= random) {
				return entry.getKey();
			}
		}
		return null;
	}

	public ItemStack getRecipeRepresentation() {
		ItemStack is = lowestChanceMap.getItemStackRepresentation().get(0);
		ISUtils.setName(is, name);
		return is;
	}

	public List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		if (i == null) {
			return input.getItemStackRepresentation();
		}
		return createLoredStacksForInfo(i);
	}

	public List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		List <ItemStack> items = lowestChanceMap.getItemStackRepresentation();
		for (ItemStack is : items) {
			ISUtils.addLore(is, ChatColor.LIGHT_PURPLE + "Randomized output");
		}
		return items;
	}
	
	@Override
	public String getTypeIdentifier() {
		return "RANDOM";
	}
	
	public ItemMap getDisplayMap() {
		return lowestChanceMap;
	}

}
