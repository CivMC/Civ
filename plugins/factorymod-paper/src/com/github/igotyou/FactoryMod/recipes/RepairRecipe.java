package com.github.igotyou.FactoryMod.recipes;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.Factory;
import com.github.igotyou.FactoryMod.utility.ItemMap;
import com.github.igotyou.FactoryMod.utility.ItemStackUtils;

/**
 * Used to repair FurnCraftChest factories. Once one of those factories is in
 * disrepair the only recipe that can be run is one of this kind
 *
 */
public class RepairRecipe extends InputRecipe {
	private int healthPerRun;

	public RepairRecipe(String name, int productionTime, ItemMap input,
			int healthPerRun) {
		super(name, productionTime, input);
		this.healthPerRun = healthPerRun;
	}

	public List<ItemStack> getOutputRepresentation(Inventory i) {
		List<ItemStack> result = new LinkedList<ItemStack>();
		ItemStack furn = new ItemStack(Material.FURNACE);
		ItemStackUtils.setLore(furn, "+" + String.valueOf(healthPerRun) + " health");
		result.add(furn);
		return result;
	}

	public List<ItemStack> getInputRepresentation(Inventory i) {
		return createLoredStacksForInfo(i);
	}

	public void applyEffect(Inventory i, Factory f) {
		if (enoughMaterialAvailable(i)) {
			for (ItemStack is : input.getItemStackRepresentation()) {
				i.removeItem(is);
			}
			f.getRepairManager().repair(healthPerRun);
		}
	}

	public ItemStack getRecipeRepresentation() {
		ItemStack res = new ItemStack(Material.FURNACE);
		ItemStackUtils.setName(res, getRecipeName());
		return res;
	}
}
