package com.github.igotyou.FactoryMod.recipes;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.utility.ItemMap;
import com.github.igotyou.FactoryMod.utility.ItemStackUtils;

/**
 * A recipe with any form of item input to run it
 *
 */
public abstract class InputRecipe implements IRecipe {
	protected String name;
	protected int productionTime;
	protected ItemMap input;

	public InputRecipe(String name, int productionTime, ItemMap input) {
		this.name = name;
		this.productionTime = productionTime;
		this.input = input;
	}

	/**
	 * Used to get a representation of a recipes input materials, which is
	 * displayed in an item gui to illustrate the recipe and to give additional
	 * information. If null is given instead of an inventory just general
	 * information should be returned, which doesnt depend on a specific
	 * instance
	 * 
	 * @param i
	 *            Inventory for which the recipe would be run, this is used to
	 *            add lore to the items, which tells how often the recipe could
	 *            be run
	 * @return List of itemstacks which represent the input required to run this
	 *         recipe
	 */
	public abstract List<ItemStack> getInputRepresentation(Inventory i);

	/**
	 * Used to get a representation of a recipes output materials, which is
	 * displayed in an item gui to illustrate the recipe and to give additional
	 * information. If null is given instead of an inventory just general
	 * information should be returned, which doesnt depend on a specific
	 * instance
	 * 
	 * @param i
	 *            Inventory for which the recipe would be run, this is used to
	 *            add lore to the items, which tells how often the recipe could
	 *            be run
	 * @return List of itemstacks which represent the output returned when
	 *         running this recipe
	 */
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

	public boolean enoughMaterialAvailable(Inventory i) {
		return input.isContainedIn(i);
	}

	/**
	 * @return A single itemstack which is used to represent this recipe as a
	 *         whole in an item gui
	 */
	public abstract ItemStack getRecipeRepresentation();

	/**
	 * Creates a list of ItemStack for a GUI representation. This list contains
	 * all the itemstacks contained in the itemstack representation of the input
	 * map and adds to each of the stacks how many runs could be made with the
	 * material available in the chest
	 * 
	 * @param i
	 *            Inventory to calculate the possible runs for
	 * @return ItemStacks containing the additional information, ready for the
	 *         GUI
	 */
	protected List<ItemStack> createLoredStacksForInfo(Inventory i) {
		LinkedList<ItemStack> result = new LinkedList<ItemStack>();
		ItemMap inventoryMap = new ItemMap(i);
		ItemMap possibleRuns = new ItemMap();
		for (Entry<ItemStack, Integer> entry : input.getEntrySet()) {
			if (inventoryMap.getAmount(entry.getKey()) != 0) {
				possibleRuns.addItemAmount(
						entry.getKey(),
						inventoryMap.getAmount(entry.getKey())
								/ entry.getValue());
			} else {
				possibleRuns.addItemAmount(entry.getKey(), 0);

			}
		}

		for (ItemStack is : input.getItemStackRepresentation()) {
			ItemStackUtils.addLore(
					is,
					ChatColor.GREEN + "Enough materials for "
							+ String.valueOf(possibleRuns.getAmount(is))
							+ " runs");
			result.add(is);
		}
		return result;
	}

}
