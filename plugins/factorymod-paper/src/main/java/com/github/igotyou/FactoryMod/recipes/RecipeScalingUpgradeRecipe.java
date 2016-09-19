package com.github.igotyou.FactoryMod.recipes;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;

import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
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
	public void applyEffect(Inventory i, FurnCraftChestFactory fccf) {
		logBeforeRecipeRun(i, fccf);
		if (toUpgrade == null || !fccf.getRecipes().contains(toUpgrade)) {
			return;
		}
		ItemMap toRemove = input.clone();
		if (toRemove.isContainedIn(i)) {
			if (toRemove.removeSafelyFrom(i)) {
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
		logAfterRecipeRun(i, fccf);
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
		List<ItemStack> result = new LinkedList<ItemStack>();
		result.add(is);
		return result;
	}

	@Override
	public ItemStack getRecipeRepresentation() {
		ItemStack is = new ItemStack(Material.PAPER);
		if (toUpgrade == null) {
			ISUtils.addLore(is, ChatColor.RED + "ERROR ERROR ERROR ERROR");
			return is;
		}
		if (newRank == 1) {
			ISUtils.addLore(is, ChatColor.GOLD + "Unlock " + toUpgrade.getName());
		}
		else {
			ISUtils.addLore(is, ChatColor.GOLD + "Upgrade " + toUpgrade.getName() + " to rank " + newRank);
		}
		ISUtils.addLore(is, ChatColor.GOLD + "Up to " + toUpgrade.getModifier().getMaximumMultiplierForRank(newRank)
				+ " output multiplier");
		return is;
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

}
