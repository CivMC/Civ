package com.github.igotyou.FactoryMod.recipes;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.recipes.scaling.ProductionRecipeModifier;

/**
 * Consumes a set of materials from a container and outputs another set of
 * materials to the same container
 *
 */
public class ProductionRecipe extends InputRecipe {
	
	private ItemMap output;
	private ProductionRecipeModifier modifier;
	private Random rng;
	private DecimalFormat decimalFormatting;

	public ProductionRecipe(String identifier, String name, int productionTime, ItemMap inputs, ItemMap output,
			ProductionRecipeModifier modifier) {
		super(identifier, name, productionTime, inputs);
		this.output = output;
		this.modifier = modifier;
		this.rng = new Random();
		this.decimalFormatting = new DecimalFormat("#.#####");
	}

	public ItemMap getOutput() {
		return output;
	}

	public ItemMap getAdjustedOutput(int rank, int runs) {
		ItemMap im = output.clone();
		if (modifier != null) {
			im.multiplyContent(modifier.getFactor(rank, runs));
			return im;
		}
		return im;
	}
	
	public ItemMap getGuaranteedOutput(int rank, int runs) {
		if (modifier == null) {
			return output.clone();
		}
		ItemMap adjusted = new ItemMap();
		double factor = modifier.getFactor(rank, runs);
		for(Entry<ItemStack, Integer> entry : output.getEntrySet()) {
			adjusted.addItemAmount(entry.getKey(), (int) (Math.floor(entry.getValue() * factor)));
		}
		return adjusted;
	}

	public int getCurrentMultiplier(Inventory i) {
		return input.getMultiplesContainedIn(i);
	}

	public List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		if (i == null || fccf == null) {
			return output.getItemStackRepresentation();
		}
		ItemMap currentOut = getGuaranteedOutput(fccf.getRecipeLevel(this), fccf.getRunCount(this));
		List<ItemStack> stacks = currentOut.getItemStackRepresentation();
		double factor = modifier.getFactor(fccf.getRecipeLevel(this), fccf.getRunCount(this));
		for(Entry<ItemStack, Integer> entry : output.getEntrySet()) {
			double additionalChance = (((double) entry.getValue()) * factor) - currentOut.getAmount(entry.getKey());
			if (Math.abs(additionalChance) > 0.00000001) {
				ItemStack is = entry.getKey().clone();
				ISUtils.addLore(is, ChatColor.GOLD + decimalFormatting.format(additionalChance) + " chance for additional item");
				stacks.add(is);
			}
		}
		int possibleRuns = input.getMultiplesContainedIn(i);
		for (ItemStack is : stacks) {
			ISUtils.addLore(is, ChatColor.GREEN + "Enough materials for " + String.valueOf(possibleRuns) + " runs");
		}
		return stacks;
	}
	
	public List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		if (i == null) {
			return input.getItemStackRepresentation();
		}
		return createLoredStacksForInfo(i);
	}

	public List<ItemStack> getGuaranteedOutput(Inventory i, FurnCraftChestFactory fccf) {
		if (i == null) {
			return input.getItemStackRepresentation();
		}
		return createLoredStacksForInfo(i);
	}

	public void applyEffect(Inventory i, FurnCraftChestFactory fccf) {
		logBeforeRecipeRun(i, fccf);
		ItemMap toRemove = input.clone();
		ItemMap toAdd;
		if (getModifier() == null) {
			toAdd = output.clone();
		}
		else {
			toAdd = getGuaranteedOutput(fccf.getRecipeLevel(this), fccf.getRunCount(this));
			double factor = modifier.getFactor(fccf.getRecipeLevel(this), fccf.getRunCount(this));
			for(Entry<ItemStack, Integer> entry : output.getEntrySet()) {
				double additionalChance = (((double) entry.getValue()) * factor) - toAdd.getAmount(entry.getKey());
				if (rng.nextDouble() <= additionalChance) {
					toAdd.addItemAmount(entry.getKey(), 1);
				}
			}
		}
		if (toRemove.isContainedIn(i)) {
			if (toRemove.removeSafelyFrom(i)) {
				for (ItemStack is : toAdd.getItemStackRepresentation()) {
					i.addItem(is);
				}
			}
		}
		logAfterRecipeRun(i, fccf);
	}

	public ItemStack getRecipeRepresentation() {
		List<ItemStack> out = getOutput().getItemStackRepresentation();
		ItemStack res;
		if (out.size() == 0) {
			res = new ItemStack(Material.STONE);
		} else {
			res = out.get(0);
		}
		ISUtils.setName(res, getName());
		return res;
	}

	public ProductionRecipeModifier getModifier() {
		return modifier;
	}

	@Override
	public String getTypeIdentifier() {
		return "PRODUCTION";
	}
}
