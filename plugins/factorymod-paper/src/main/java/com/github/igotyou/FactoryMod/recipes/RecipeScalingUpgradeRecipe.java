package com.github.igotyou.FactoryMod.recipes;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.github.igotyou.FactoryMod.utility.MultiInventoryWrapper;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

public class RecipeScalingUpgradeRecipe extends InputRecipe {

	private ProductionRecipe toUpgrade;
	private int newRank;
	private RecipeScalingUpgradeRecipe followUpRecipe;

	public RecipeScalingUpgradeRecipe(String identifier, String name, int productionTime, ItemMap input,
			ProductionRecipe toUpgrade, int newRank, RecipeScalingUpgradeRecipe followUpRecipe) {
		super(identifier, name, productionTime, input);
		this.toUpgrade = toUpgrade;
		this.newRank = newRank;
		this.followUpRecipe = followUpRecipe;
	}

	@Override
	public boolean applyEffect(Inventory inputInv, Inventory outputInv, FurnCraftChestFactory fccf) {
		MultiInventoryWrapper combo = new MultiInventoryWrapper(inputInv, outputInv);
		logBeforeRecipeRun(combo, fccf);
		if (toUpgrade == null || !fccf.getRecipes().contains(toUpgrade)) {
			return false;
		}
		ItemMap toRemove = input.clone();
		if (toRemove.isContainedIn(inputInv)) {
			if (toRemove.removeSafelyFrom(inputInv)) {
				if (newRank == 1) {
					fccf.addRecipe(toUpgrade);
				}
				else {
					fccf.setRecipeLevel(toUpgrade, newRank);
				}
				// no longer needed
				fccf.removeRecipe(this);
				if (followUpRecipe != null) {
					fccf.addRecipe(followUpRecipe);
					fccf.setRecipe(toUpgrade);
				}
			}
		}
		logAfterRecipeRun(combo, fccf);
		return true;
	}

	public void setUpgradedRecipe(ProductionRecipe rec) {
		this.toUpgrade = rec;
	}

	public void setFollowUpRecipe(RecipeScalingUpgradeRecipe rec) {
		this.followUpRecipe = rec;
	}

	@Override
	public String getTypeIdentifier() {
		return "RECIPEMODIFIERUPGRADE";
	}

	@Override
	public List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		if (i == null) {
			return input.getItemStackRepresentation();
		}
		return createLoredStacksForInfo(i);
	}

	@Override
	public List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		ItemStack is = getRecipeRepresentation();
		List<ItemStack> result = new LinkedList<>();
		result.add(is);
		return result;
	}
	
	@Override
	public Material getRecipeRepresentationMaterial() {
		return Material.GRINDSTONE;
	}

	public IRecipe getToUpgrade() {
		return toUpgrade;
	}

	public int getNewRank() {
		return newRank;
	}

	public IRecipe getFollowUpRecipe() {
		return followUpRecipe;
	}

	@Override
	public List<String> getTextualOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		return Arrays.asList("TODO");
	}

}
