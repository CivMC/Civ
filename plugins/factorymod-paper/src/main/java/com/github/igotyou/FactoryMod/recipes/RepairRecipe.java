package com.github.igotyou.FactoryMod.recipes;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.repairManager.PercentageHealthRepairManager;
import com.github.igotyou.FactoryMod.utility.LoggingUtils;

import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

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

	@Override
	public List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		List<ItemStack> result = new LinkedList<>();
		ItemStack furn = new ItemStack(Material.FURNACE);
		ItemAPI.setLore(furn, "+" + String.valueOf(healthPerRun) + " health");
		result.add(furn);
		return result;
	}

	@Override
	public List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		if (i == null) {
			return input.getItemStackRepresentation();
		}
		return createLoredStacksForInfo(i);
	}

	@Override
	public boolean applyEffect(Inventory i, FurnCraftChestFactory fccf) {
		logBeforeRecipeRun(i, fccf);
		if (enoughMaterialAvailable(i)) {
			if (input.removeSafelyFrom(i)) {
				((PercentageHealthRepairManager) (fccf.getRepairManager()))
						.repair(healthPerRun);
				LoggingUtils.log(((PercentageHealthRepairManager) (fccf
						.getRepairManager())).getHealth()
						+ " for "
						+ fccf.getLogData() + " after repairing");
			}
		}
		logAfterRecipeRun(i, fccf);
		return true;
	}
	
	@Override
	public Material getRecipeRepresentationMaterial() {
		return Material.FURNACE;
	}

	@Override
	public String getTypeIdentifier() {
		return "REPAIR";
	}

	public int getHealth() {
		return healthPerRun;
	}

	@Override
	public List<String> getTextualOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		return Arrays.asList("Repairs the factory by " + healthPerRun + " health");
	}
}
