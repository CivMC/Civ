package com.github.igotyou.FactoryMod.eggs;

import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.interactionManager.FurnCraftChestInteractionManager;
import com.github.igotyou.FactoryMod.multiBlockStructures.FurnCraftChestStructure;
import com.github.igotyou.FactoryMod.multiBlockStructures.MultiBlockStructure;
import com.github.igotyou.FactoryMod.powerManager.FurnacePowerManager;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.repairManager.PercentageHealthRepairManager;

public class FurnCraftChestEgg implements IFactoryEgg {
	private String name;
	private int updateTime;
	private List<IRecipe> recipes;
	private ItemStack fuel;
	private int fuelConsumptionIntervall;

	public FurnCraftChestEgg(String name, int updateTime,
			List<IRecipe> recipes, ItemStack fuel, int fuelConsumptionIntervall) {
		this.name = name;
		this.updateTime = updateTime;
		this.recipes = recipes;
		this.fuel = fuel;
		this.fuelConsumptionIntervall = fuelConsumptionIntervall;
	}

	public Factory hatch(MultiBlockStructure mbs, Player p) {
		FurnCraftChestStructure fccs = (FurnCraftChestStructure) mbs;
		FurnacePowerManager fpm = new FurnacePowerManager(fuel,
				fuelConsumptionIntervall);
		FurnCraftChestInteractionManager fccim = new FurnCraftChestInteractionManager();
		PercentageHealthRepairManager phrm = new PercentageHealthRepairManager(
				100);
		FurnCraftChestFactory fccf = new FurnCraftChestFactory(fccim, phrm,
				fpm, fccs, updateTime, name, recipes);
		fccim.setFactory(fccf);
		fpm.setFactory(fccf);
		if (recipes.size() != 0) {
			fccf.setRecipe(recipes.get(0));
		}
		return fccf;
	}

	public String getName() {
		return name;
	}

	public int getUpdateTime() {
		return updateTime;
	}

	public ItemStack getFuel() {
		return fuel;
	}

	public List<IRecipe> getRecipes() {
		return recipes;
	}
	
	public void setRecipes(List <IRecipe> recipes) {
		this.recipes = recipes;
	}

	public int getFuelConsumptionIntervall() {
		return fuelConsumptionIntervall;
	}

	public Factory revive(List<Block> blocks, int health, String selectedRecipe, int productionTimer) {
		FurnCraftChestStructure fccs = new FurnCraftChestStructure(blocks);
		FurnacePowerManager fpm = new FurnacePowerManager(fuel,
				fuelConsumptionIntervall);
		FurnCraftChestInteractionManager fccim = new FurnCraftChestInteractionManager();
		PercentageHealthRepairManager phrm = new PercentageHealthRepairManager(
				health);
		FurnCraftChestFactory fccf = new FurnCraftChestFactory(fccim, phrm,
				fpm, fccs, updateTime, name, recipes);
		fccim.setFactory(fccf);
		fpm.setFactory(fccf);
		for (IRecipe recipe : recipes) {
			if (recipe.getRecipeName() == selectedRecipe) {
				fccf.setRecipe(recipe);
			}
		}
		if (fccf.getCurrentRecipe() == null && recipes.size() != 0) {
			fccf.setRecipe(recipes.get(0));
		}
		if (productionTimer != 0) {
			fccf.attemptToActivate(null);
			if (fccf.isActive()) {
				fccf.setProductionTimer(productionTimer);
			}
		}
		return fccf;
	}

}
