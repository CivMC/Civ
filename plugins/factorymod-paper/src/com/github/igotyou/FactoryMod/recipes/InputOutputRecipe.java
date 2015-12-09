package com.github.igotyou.FactoryMod.recipes;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.igotyou.FactoryMod.utility.ItemMap;

public abstract class InputOutputRecipe implements IRecipe {
	protected String name;
	protected int productionTime;
	protected ItemMap input;

	public InputOutputRecipe(String name, int productionTime, ItemMap input) {
		this.name = name;
		this.productionTime = productionTime;
		this.input = input;
	}

	public abstract List<ItemStack> getInputRepresentation(Inventory i);

	public abstract List<ItemStack> getOutputRepresentation(Inventory i);

	public String getRecipeName() {
		return name;
	}

	public int getProductionTime() {
		return productionTime;
	}

	public ItemMap getInput() {
		return input;
	}

	/**
	 * Creates a list of ItemStack for a GUI representation. This list contains
	 * all the itemstacks contained in the itemstack representation of the input
	 * map and adds to each of the stacks how many runs could be made with the
	 * material available in the chest
	 * 
	 * @param i Inventory to calculate the possible runs for
	 * @return ItemStacks containing the additional information, ready for the GUI
	 */
	protected List<ItemStack> createLoredStacksForInfo(Inventory i) {
		LinkedList<ItemStack> result = new LinkedList<ItemStack>();
		ItemMap inventoryMap = new ItemMap(i);
		if (input.isContainedIn(inventoryMap)) {
			for (ItemStack is : input.getItemStackRepresentation()) {
				int possibleRuns = new ItemMap(is)
						.getMultiplesContainedIn(inventoryMap);
				ItemMeta im = is.getItemMeta();
				List<String> lore;
				if (im.hasLore()) {
					lore = im.getLore();
				} else {
					lore = new LinkedList<String>();
				}
				lore.add(ChatColor.GREEN + "Enough materials for "
						+ String.valueOf(possibleRuns) + " runs");
				im.setLore(lore);
				is.setItemMeta(im);
				result.add(is);
			}
		}
		return result;
	}

}
