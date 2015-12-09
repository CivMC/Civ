package com.github.igotyou.FactoryMod.classicTriblockFactory;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.interactionManager.IInteractionManager;
import com.github.igotyou.FactoryMod.multiBlockStructures.FurnCraftChestStructure;
import com.github.igotyou.FactoryMod.powerManager.IPowerManager;
import com.github.igotyou.FactoryMod.recipes.ProductionRecipe;
import com.github.igotyou.FactoryMod.repairManager.IRepairManager;
import com.github.igotyou.FactoryMod.utility.ItemMap;

public class ProductionFactory extends FurnCraftChestFactory {

	public ProductionFactory(IInteractionManager im, IRepairManager rm,
			IPowerManager ipm, FurnCraftChestStructure mbs, int updateTime) {
		super(im, rm, ipm, mbs, updateTime);
	}

	@Override
	public boolean hasInputMaterials() {
		return ((ProductionRecipe) currentRecipe).getRawInputs().isContainedBy(
				new ItemMap(getInventory()));
	}

	public void applyRecipeEffect() {
		ProductionRecipe pr = (ProductionRecipe) currentRecipe;
		int multiplier = pr.getRawInputs().getMultiplesContainedIn(
				new ItemMap(getInventory()));
		ItemMap toRemove = pr.getRawInputs().clone();
		toRemove.multiplyContent(multiplier);
		ItemMap toAdd = pr.getRawOutputs().clone();
		toAdd.multiplyContent(multiplier);
		for (ItemStack is : toRemove.getItemStackRepresentation()) {
			getInventory().remove(is);
		}
		for (ItemStack is : toAdd.getItemStackRepresentation()) {
			HashMap<Integer, ItemStack> notAdded = getInventory().addItem(is);
			// drop items that didnt fit in the chest
			for (Entry<Integer, ItemStack> entry : notAdded.entrySet()) {
				getChest()
						.getLocation()
						.getWorld()
						.dropItemNaturally(
								getChest().getRelative(BlockFace.UP)
										.getLocation(), entry.getValue());
			}
		}
	}

}
