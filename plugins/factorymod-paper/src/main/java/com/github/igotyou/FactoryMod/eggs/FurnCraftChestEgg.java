package com.github.igotyou.FactoryMod.eggs;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.interactionManager.FurnCraftChestInteractionManager;
import com.github.igotyou.FactoryMod.powerManager.FurnacePowerManager;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.repairManager.PercentageHealthRepairManager;
import com.github.igotyou.FactoryMod.structures.FurnCraftChestStructure;
import com.github.igotyou.FactoryMod.structures.MultiBlockStructure;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;

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
	private double citadelBreakReduction;
	private ItemMap setupCost;

	public FurnCraftChestEgg(String name, int updateTime,
			List<IRecipe> recipes, ItemStack fuel,
			int fuelConsumptionIntervall, double returnRateOnDestruction, int maximumHealth, long breakGracePeriod, int healthPerDamagePeriod, double citadelBreakReduction, ItemMap setupCost) {
		this.name = name;
		this.updateTime = updateTime;
		this.recipes = recipes;
		this.fuel = fuel;
		this.breakGracePeriod = breakGracePeriod;
		this.healthPerDamagePeriod = healthPerDamagePeriod;
		this.fuelConsumptionIntervall = fuelConsumptionIntervall;
		this.returnRateOnDestruction = returnRateOnDestruction;
		this.maximumHealth = maximumHealth;
		this.citadelBreakReduction = citadelBreakReduction;
		this.setupCost = setupCost;
	}

	@Override
	public Factory hatch(MultiBlockStructure mbs, Player p) {
		FurnCraftChestStructure fccs = (FurnCraftChestStructure) mbs;
		FurnacePowerManager fpm = new FurnacePowerManager(fccs.getFurnace(),
				fuel, fuelConsumptionIntervall);
		FurnCraftChestInteractionManager fccim = new FurnCraftChestInteractionManager();
		PercentageHealthRepairManager phrm = new PercentageHealthRepairManager(maximumHealth, maximumHealth, 0, healthPerDamagePeriod, breakGracePeriod);
		FurnCraftChestFactory fccf = new FurnCraftChestFactory(fccim, phrm,
				fpm, fccs, updateTime, name, recipes, citadelBreakReduction);
		fccim.setFactory(fccf);
		phrm.setFactory(fccf);
		if (!recipes.isEmpty()) {
			fccf.setRecipe(recipes.get(0));
		}
		return fccf;
	}

	@Override
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

	@Override
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
			String selectedRecipe, int productionTimer, long breakTime, List <String> recipeStrings) {
		FurnCraftChestStructure fccs = new FurnCraftChestStructure(blocks);
		FurnacePowerManager fpm = new FurnacePowerManager(fccs.getFurnace(),
				fuel, fuelConsumptionIntervall);
		FurnCraftChestInteractionManager fccim = new FurnCraftChestInteractionManager();
		PercentageHealthRepairManager phrm = new PercentageHealthRepairManager(health, maximumHealth, breakTime, healthPerDamagePeriod, breakGracePeriod);
		List <IRecipe> currRecipes = new ArrayList<> ();
		for(String recName : recipeStrings) {
			boolean found = false;
			for(IRecipe exRec : currRecipes) {
				if (exRec.getIdentifier().equals(recName)) {
					found = true;
					break;
				}
			}
			if (!found) {
				IRecipe rec = FactoryMod.getInstance().getManager().getRecipe(recName);
				if (rec == null) {
					FactoryMod.getInstance().warning("Factory at " + blocks.get(0).toString() + " had recipe " + recName + " saved, but it could not be loaded from the config");
				}
				else {
					currRecipes.add(rec);
				}
			}
		}
		FurnCraftChestFactory fccf = new FurnCraftChestFactory(fccim, phrm,
				fpm, fccs, updateTime, name, currRecipes, citadelBreakReduction);
		fccim.setFactory(fccf);
		phrm.setFactory(fccf);
		for (IRecipe recipe : currRecipes) {
			if (recipe.getName().equals(selectedRecipe)) {
				fccf.setRecipe(recipe);
			}
		}
		if (fccf.getCurrentRecipe() == null && !currRecipes.isEmpty()) {
			fccf.setRecipe(currRecipes.get(0));
		}
		if (productionTimer != 0) {
			fccf.attemptToActivate(null, true);
			if (fccf.isActive()) {
				fccf.setProductionTimer(productionTimer);
			}
		}
		return fccf;
	}

	@Override
	public Class <FurnCraftChestStructure> getMultiBlockStructure() {
		return FurnCraftChestStructure.class;
	}
	
	public ItemMap getSetupCost() {
		return setupCost;
	}

	public double getCitadelBreakReduction() {
		return citadelBreakReduction;
	}
}
