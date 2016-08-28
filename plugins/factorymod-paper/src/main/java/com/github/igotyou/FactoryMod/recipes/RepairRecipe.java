package com.github.igotyou.FactoryMod.recipes;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.repairManager.PercentageHealthRepairManager;
import com.github.igotyou.FactoryMod.utility.LoggingUtils;

/**
 * Used to repair FurnCraftChest factories. Once one of those factories is in
 * disrepair the only recipe that can be run is one of this kind
 *
 */
public class RepairRecipe extends InputRecipe {
	private int healthPerRun;

	public RepairRecipe(String identifier, String name, int productionTime, ItemMap input,
			int healthPerRun) {
		super(identifier, name, productionTime, input);
		this.healthPerRun = healthPerRun;
	}

	public List<ItemStack> getOutputRepresentation(Inventory i) {
		List<ItemStack> result = new LinkedList<ItemStack>();
		ItemStack furn = new ItemStack(Material.FURNACE);
		ISUtils.setLore(furn, "+" + String.valueOf(healthPerRun) + " health");
		result.add(furn);
		return result;
	}

	public List<ItemStack> getInputRepresentation(Inventory i) {
		if (i == null) {
			return input.getItemStackRepresentation();
		}
		return createLoredStacksForInfo(i);
	}

	public void applyEffect(Inventory i, Factory f) {
		logBeforeRecipeRun(i, f);
		if (enoughMaterialAvailable(i)) {
			if (input.removeSafelyFrom(i)) {
				((PercentageHealthRepairManager) (f.getRepairManager()))
						.repair(healthPerRun);
				LoggingUtils.log(((PercentageHealthRepairManager) (f
						.getRepairManager())).getHealth()
						+ " for "
						+ f.getLogData() + " after repairing");
			}
		}
		logAfterRecipeRun(i, f);
	}

	public ItemStack getRecipeRepresentation() {
		ItemStack res = new ItemStack(Material.FURNACE);
		ISUtils.setName(res, getName());
		return res;
	}
	
	@Override
	public String getTypeIdentifier() {
		return "REPAIR";
	}
	
	public int getHealth() {
		return healthPerRun;
	}
}
