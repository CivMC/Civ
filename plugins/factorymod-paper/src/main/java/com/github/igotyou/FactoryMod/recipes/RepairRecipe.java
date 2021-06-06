package com.github.igotyou.FactoryMod.recipes;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.repairManager.PercentageHealthRepairManager;
import com.github.igotyou.FactoryMod.utility.LoggingUtils;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.github.igotyou.FactoryMod.utility.MultiInventoryWrapper;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
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
		ItemUtils.setLore(furn, "+" + String.valueOf(healthPerRun) + " health");
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
	public boolean applyEffect(Inventory inputInv, Inventory outputInv, FurnCraftChestFactory fccf) {
		MultiInventoryWrapper combo = new MultiInventoryWrapper(inputInv, outputInv);
		logBeforeRecipeRun(combo, fccf);
		if (enoughMaterialAvailable(inputInv)) {
			if (input.removeSafelyFrom(inputInv)) {
				((PercentageHealthRepairManager) (fccf.getRepairManager()))
						.repair(healthPerRun);
				LoggingUtils.log(((PercentageHealthRepairManager) (fccf
						.getRepairManager())).getHealth()
						+ " for "
						+ fccf.getLogData() + " after repairing");
			}
		}
		logAfterRecipeRun(combo, fccf);
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
		return Arrays.asList(ChatColor.YELLOW + "Repairs the factory by " + healthPerRun + " health");
	}
}
