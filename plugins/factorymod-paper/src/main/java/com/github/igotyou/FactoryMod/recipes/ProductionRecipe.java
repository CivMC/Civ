package com.github.igotyou.FactoryMod.recipes;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

import com.github.igotyou.FactoryMod.factories.Factory;

/**
 * Consumes a set of materials from a container and outputs another set of
 * materials to the same container
 *
 */
public class ProductionRecipe extends InputRecipe {
	private ItemMap output;

	public ProductionRecipe(String identifier, String name, int productionTime, ItemMap inputs,
			ItemMap output) {
		super(identifier, name, productionTime, inputs);
		this.output = output;
	}

	public ItemMap getOutput() {
		return output;
	}

	public int getCurrentMultiplier(Inventory i) {
		return input.getMultiplesContainedIn(i);
	}

	public ItemMap getCurrentOutput(Inventory i) {
		ItemMap copy = output.clone();
		copy.multiplyContent(getCurrentMultiplier(i));
		return copy;
	}

	public List<ItemStack> getOutputRepresentation(Inventory i) {
		List<ItemStack> stacks = output.getItemStackRepresentation();
		if (i == null) {
			return stacks;
		}
		int possibleRuns = input.getMultiplesContainedIn(i);
		for (ItemStack is : stacks) {
			ISUtils.addLore(is, ChatColor.GREEN + "Enough materials for "
					+ String.valueOf(possibleRuns) + " runs");
		}
		return stacks;
	}

	public List<ItemStack> getInputRepresentation(Inventory i) {
		if (i == null) {
			return input.getItemStackRepresentation();
		}
		return createLoredStacksForInfo(i);
	}

	public void applyEffect(Inventory i, Factory f) {
		logBeforeRecipeRun(i, f);
		ItemMap toRemove = input.clone();
		ItemMap toAdd = output.clone();
		if (toRemove.isContainedIn(i)) {
			if (toRemove.removeSafelyFrom(i)) {
				for(ItemStack is: toAdd.getItemStackRepresentation()) {
					i.addItem(is);
				}
			}
		}
		logAfterRecipeRun(i, f);
	}

	public ItemStack getRecipeRepresentation() {
		List<ItemStack> out = output.getItemStackRepresentation();
		ItemStack res;
		if (out.size() == 0) {
			res = new ItemStack(Material.STONE);
		} else {
			res = out.get(0);
		}
		ISUtils.setName(res, getName());
		return res;
	}
	
	@Override
	public String getTypeIdentifier() {
		return "PRODUCTION";
	}
}
