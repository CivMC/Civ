package com.github.igotyou.FactoryMod.eggs;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.interactionManager.FurnCraftChestInteractionManager;
import com.github.igotyou.FactoryMod.powerManager.FurnacePowerManager;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.repairManager.PercentageHealthRepairManager;
import com.github.igotyou.FactoryMod.structures.FurnCraftChestStructure;
import com.github.igotyou.FactoryMod.structures.MultiBlockStructure;

public class FurnCraftChestEgg implements IFactoryEgg {
	private String name;
	private int updateTime;
	private List<IRecipe> recipes;
	private ItemStack fuel;
	private int fuelConsumptionIntervall;
	private int maximumHealth;
	private long breakGracePeriod;
	private int healthPerDamagePeriod;
	private double returnRateOnDestruction;

	public FurnCraftChestEgg(String name, int updateTime,
			List<IRecipe> recipes, ItemStack fuel,
			int fuelConsumptionIntervall, double returnRateOnDestruction, int maximumHealth, long breakGracePeriod, int healthPerDamagePeriod) {
		this.name = name;
		this.updateTime = updateTime;
		this.recipes = recipes;
		this.fuel = fuel;
		this.breakGracePeriod = breakGracePeriod;
		this.healthPerDamagePeriod = healthPerDamagePeriod;
		this.fuelConsumptionIntervall = fuelConsumptionIntervall;
		this.returnRateOnDestruction = returnRateOnDestruction;
		this.maximumHealth = maximumHealth;
	}

	public Factory hatch(MultiBlockStructure mbs, Player p) {
		FurnCraftChestStructure fccs = (FurnCraftChestStructure) mbs;
		FurnacePowerManager fpm = new FurnacePowerManager(fccs.getFurnace(),
				fuel, fuelConsumptionIntervall);
		FurnCraftChestInteractionManager fccim = new FurnCraftChestInteractionManager();
		PercentageHealthRepairManager phrm = new PercentageHealthRepairManager(maximumHealth, maximumHealth, 0, healthPerDamagePeriod, breakGracePeriod);
		FurnCraftChestFactory fccf = new FurnCraftChestFactory(fccim, phrm,
				fpm, fccs, updateTime, name, recipes);
		fccim.setFactory(fccf);
		phrm.setFactory(fccf);
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

	public void setRecipes(List<IRecipe> recipes) {
		this.recipes = recipes;
	}
	
	public double getReturnRate() {
		return returnRateOnDestruction;
	}

	public int getFuelConsumptionIntervall() {
		return fuelConsumptionIntervall;
	}
	
	public int getMaximumHealth() {
		return maximumHealth;
	}
	
	public int getDamagePerDamagingPeriod() {
		return healthPerDamagePeriod;
	}
	
	public long getBreakGracePeriod() {
		return breakGracePeriod;
	}

	public Factory revive(List<Location> blocks, int health,
			String selectedRecipe, int productionTimer, int breakTime) {
		FurnCraftChestStructure fccs = new FurnCraftChestStructure(blocks);
		FurnacePowerManager fpm = new FurnacePowerManager(fccs.getFurnace(),
				fuel, fuelConsumptionIntervall);
		FurnCraftChestInteractionManager fccim = new FurnCraftChestInteractionManager();
		PercentageHealthRepairManager phrm = new PercentageHealthRepairManager(health, maximumHealth, breakTime, healthPerDamagePeriod, breakGracePeriod);
		FurnCraftChestFactory fccf = new FurnCraftChestFactory(fccim, phrm,
				fpm, fccs, updateTime, name, recipes);
		fccim.setFactory(fccf);
		phrm.setFactory(fccf);
		for (IRecipe recipe : recipes) {
			if (recipe.getRecipeName().equals(selectedRecipe)) {
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
	
	public Class getMultiBlockStructure() {
		return FurnCraftChestStructure.class;
	}

}
