package com.github.igotyou.FactoryMod.recipes;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.utility.ItemMap;
import com.github.igotyou.FactoryMod.utility.ItemStackUtils;

/**
 * Consumes a set of materials from a container and outputs another set of
 * materials to the same container
 *
 */
public class ProductionRecipe extends InputRecipe {
	private ItemMap output;

	public ProductionRecipe(String name, int productionTime, ItemMap inputs,
			ItemMap output) {
		super(name, productionTime, inputs);
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
		int possibleRuns = input.getMultiplesContainedIn(i);
		for (ItemStack is : stacks) {
			ItemStackUtils.addLore(is, ChatColor.GREEN
					+ "Enough materials for " + String.valueOf(possibleRuns)
					+ " runs");
		}
		return stacks;
	}

	public List<ItemStack> getInputRepresentation(Inventory i) {
		return createLoredStacksForInfo(i);
	}

	public void applyEffect(Inventory i, Factory f) {
		ItemMap toRemove = input.clone();
		ItemMap toAdd = output.clone();
		if (toRemove.isContainedIn(i)) {
			for (ItemStack is : toRemove.getItemStackRepresentation()) {
				i.removeItem(is);
			}
			for (ItemStack is : toAdd.getItemStackRepresentation()) {
				i.addItem(is);
			}
		}
	}

	public ItemStack getRecipeRepresentation() {
		List<ItemStack> out = output.getItemStackRepresentation();
		ItemStack res;
		if (out.size() == 0) {
			res = new ItemStack(Material.STONE);
		} else {
			res = out.get(0);
		}
		ItemStackUtils.setName(res, getRecipeName());
		return res;
	}
}
