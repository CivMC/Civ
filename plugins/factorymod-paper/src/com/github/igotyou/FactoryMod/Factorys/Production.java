package com.github.igotyou.FactoryMod.Factorys;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.FactoryObject;
import com.github.igotyou.FactoryMod.interfaces.Factory;
import com.github.igotyou.FactoryMod.interfaces.Recipe;
import com.github.igotyou.FactoryMod.properties.ProductionProperties;
import com.github.igotyou.FactoryMod.recipes.ProductionRecipe;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;

public class Production extends FactoryObject implements Factory
{

	private Recipe currentRecipe = null;
	private ProductionProperties productionFactoryProperties;
	private int currentProductionTimer = 0;
	private int currentEnergyTimer = 0;
	private int currentRecipeNumber = 0;
	public static final FactoryType FACTORY_TYPE = FactoryType.PRODUCTION;
	public String SUB_FACTORY_TYPE;
	
	public Production (Location factoryLocation, Location factoryInventoryLocation, Location factoryPowerSource
			, String subFactoryType)
	{
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource, Production.FACTORY_TYPE, subFactoryType);
		this.SUB_FACTORY_TYPE = subFactoryType;
		this.productionFactoryProperties = (ProductionProperties) factoryProperties;
		this.setRecipeToNumber(0);

	}

	public Production (Location factoryLocation, Location factoryInventoryLocation, Location factoryPowerSource,
			String subFactoryType, boolean active, int currentProductionTimer, int currentEnergyTimer, int currentRecipeNumber)
	{
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource, Production.FACTORY_TYPE, subFactoryType);
		this.SUB_FACTORY_TYPE = subFactoryType;
		this.productionFactoryProperties = (ProductionProperties) factoryProperties;
		this.active = active;
		this.currentEnergyTimer = currentEnergyTimer;
		this.currentProductionTimer = currentProductionTimer;
		this.setRecipeToNumber(currentRecipeNumber);
	}
	
	public void update() 
	{
		//if factory is turned on
		if (active)
		{
			if (areMaterialsAvailable(getInventory(), currentRecipe.getInputMaterial(), currentRecipe.getInputAmountWithBatchAmount()))
			{
				//if the production time has not reached the recipes production time
				if (currentProductionTimer < currentRecipe.getProductionTime())
				{
					//FactoryModPlugin.sendConsoleMessage("current Production Timer is: " + String.valueOf(currentProductionTimer) + " and current recipe's production timer is: " + String.valueOf(currentRecipe.getProductionTime()));
					if (isFuelAvailable())
					{
						if (currentEnergyTimer == productionFactoryProperties.getEnergyTime())
						{
							removeMaterial(getPowerSourceInventory(), productionFactoryProperties.getEnergyMaterial(), productionFactoryProperties.getEnergyConsumption());
							currentEnergyTimer = 0;
						}
						currentEnergyTimer++;
						currentProductionTimer ++;
					}
					else
					{
						powerOff();
					}
				}
				
				//if the production timer has reached the recipes production time remove input from chest, and add output material
				else if (currentProductionTimer == currentRecipe.getProductionTime())
				{
					if (removeMaterials(getInventory(), currentRecipe.getInputMaterial(), currentRecipe.getInputAmountWithBatchAmount()))
					{
						for (int i = 1; i <= currentRecipe.getBatchAmount(); i++)
						{
							addMaterial(getInventory(), currentRecipe.getOutput(), 1);
						}
						
						//addMaterial(getInventory(), currentRecipe.getOutput(), currentRecipe.getBatchAmount());
						currentProductionTimer = 0;
						powerOff();
					}
				}
			}	
		}
		// TODO add 	
	}

	public void powerOn() 
	{
		Furnace furnace = (Furnace) factoryPowerSourceLocation.getBlock().getState();
		byte data = furnace.getData().getData();
		ItemStack[] oldContents = furnace.getInventory().getContents();
		furnace.getInventory().clear();
		factoryPowerSourceLocation.getBlock().setType(Material.BURNING_FURNACE);
		furnace = (Furnace) factoryPowerSourceLocation.getBlock().getState();
		furnace.setRawData(data);
		furnace.update();
		furnace.getInventory().setContents(oldContents);
		furnace.setBurnTime((short) 1000);
		active = true;
	}

	public void powerOff() 
	{
		Furnace furnace = (Furnace) factoryPowerSourceLocation.getBlock().getState();
		byte data = furnace.getData().getData();
		ItemStack[] oldContents = furnace.getInventory().getContents();
		furnace.getInventory().clear();
		factoryPowerSourceLocation.getBlock().setType(Material.FURNACE);
		furnace = (Furnace) factoryPowerSourceLocation.getBlock().getState();
		furnace.setRawData(data);
		furnace.update();
		furnace.getInventory().setContents(oldContents);
		furnace.setBurnTime((short) 0);
		active = false;
	}

	public InteractionResponse togglePower() 
	{
		if (!active)
		{
			if (isFuelAvailable())
			{
				if (areMaterialsAvailable(getInventory(), currentRecipe.getInputMaterial(), currentRecipe.getInputAmountWithBatchAmount()))
				{
					powerOn();
					return new InteractionResponse(InteractionResult.SUCCESS, "Factory activated!");
				}
				else
				{
					return new InteractionResponse(InteractionResult.FAILURE, "Factory does not have enough materials for the current recipe! You need: " + getMaterialsNeededMessage(currentRecipe.getInputMaterial(), currentRecipe.getInputAmountWithBatchAmount()));
				}
			}
			else
			{
				return new InteractionResponse(InteractionResult.FAILURE, "Factory is missing fuel!");
			}
		}
		else
		{
			powerOff();
			return new InteractionResponse(InteractionResult.FAILURE, "Factory has been deactivated!");
		}
	}
	
	public InteractionResponse toggleRecipes()
	{
		if (!active)
		{
			if (currentRecipe != null)
			{		
				if (currentRecipeNumber == productionFactoryProperties.getRecipes().size() - 1)
				{
					setRecipeToNumber(0);
					return new InteractionResponse(InteractionResult.SUCCESS, "Recipe switched! Current recipe is:" + currentRecipe.getRecipeName());
				}
				else
				{
					setRecipeToNumber(currentRecipeNumber + 1);
					return new InteractionResponse(InteractionResult.SUCCESS, "Recipe switched! Current recipe is:" + currentRecipe.getRecipeName());
				}
			}
			else
			{
				setRecipeToNumber(0);
				return new InteractionResponse(InteractionResult.SUCCESS, "Recipe selected! Current recipe is:" + currentRecipe.getRecipeName());
			}	
		}
		else
		{
			return new InteractionResponse(InteractionResult.FAILURE, "You can't change recipes while the factory is on! Turn it off first.");
		}
	}
	
	public void setRecipe(Recipe newRecipe)
	{
		currentRecipe = newRecipe;
	}
	
	public void setRecipeToNumber(int newRecipeNumber)
	{
		currentRecipe = productionFactoryProperties.getRecipes().get(newRecipeNumber);
		currentRecipeNumber = newRecipeNumber;
	}

	public Location getCenterLocation() 
	{
		return factoryLocation;
	}

	public Location getInventoryLocation() 
	{
		return factoryInventoryLocation;
	}

	public Location getPowerSourceLocation() 
	{
		return factoryPowerSourceLocation;
	}
	
	public boolean isFuelAvailable()
	{
		return isMaterialAvailable(getPowerSourceInventory(), productionFactoryProperties.getEnergyMaterial(), productionFactoryProperties.getEnergyConsumption());
	}

	public void destroy(Location destroyLocation)
	{
		if (FactoryModPlugin.RETURN_BUILD_MATERIALS)
		{
			for (int i = 1; i <= productionFactoryProperties.getBuildMaterial().size(); i++)
			{
				ItemStack item = new ItemStack(productionFactoryProperties.getBuildMaterial().get(i), productionFactoryProperties.getBuildAmount().get(i));
				factoryLocation.getWorld().dropItemNaturally(destroyLocation, item);
			}
		}
		destroyLocation.getBlock().setType(Material.AIR);
	}
	
	public int getProductionTimer()
	{
		return currentProductionTimer;
	}
	
	public int getEnergyTimer()
	{
		return currentEnergyTimer;
	}

	public Recipe getCurrentRecipe()
	{
		return currentRecipe;
	}
	
	public int getCurrentRecipeNumber()
	{
		return currentRecipeNumber;
	}
}
