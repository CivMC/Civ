package com.github.igotyou.FactoryMod.Factorys;

import org.bukkit.Location;
import org.bukkit.Material;

import com.github.igotyou.FactoryMod.FactoryObject;
import com.github.igotyou.FactoryMod.interfaces.Factory;
import com.github.igotyou.FactoryMod.interfaces.Recipe;
import com.github.igotyou.FactoryMod.properties.ProductionProperties;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;

public class Production extends FactoryObject implements Factory
{

	private Material currentProductionItem;
	private Recipe currentRecipe = null;
	private ProductionProperties productionFactoryProperties;
	private int currentProductionTimer = 0;
	public static final FactoryType FACTORY_TYPE = FactoryType.PRODUCTION;
	public static SubFactoryType SUB_FACTORY_TYPE;
	
	public Production (Location factoryLocation, Location factoryInventoryLocation, Location factoryPowerSource
			, SubFactoryType subFactoryType)
	{
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource, Production.FACTORY_TYPE, subFactoryType);
		this.SUB_FACTORY_TYPE = subFactoryType;
		this.productionFactoryProperties = (ProductionProperties) factoryProperties;

	}

	public Production (Location factoryLocation, Location factoryInventoryLocation, Location factoryPowerSource,
			Material currentProductionItem, SubFactoryType subFactoryType)
	{
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource, Production.FACTORY_TYPE, subFactoryType);
		this.SUB_FACTORY_TYPE = subFactoryType;
		this.currentProductionItem = currentProductionItem;
		this.productionFactoryProperties = (ProductionProperties) factoryProperties;
	}
	
	public void update() 
	{
		//if factory is turned on
		if (active)
		{
			//if the production time has not reached the recipes production time
			if (currentProductionTimer < currentRecipe.getProductionTime())
			{
				if (isFuelAvailable())
				{
					removeMaterial(getPowerSourceInventory(), Material.COAL, 1);
					currentProductionTimer ++;
				}
			}
			else if (currentProductionTimer == currentRecipe.getProductionTime())
			{
				if (removeMaterials(getInventory(), currentRecipe.getInputMaterial(), currentRecipe.getInputAmountWithBatchAmount()))
				{
					addMaterial(getInventory(), currentRecipe.getOutput(), currentRecipe.getBatchAmount());
					currentProductionTimer = 0;
					powerOff();
				}
			}
			
		}
		// TODO add 	
	}

	public void powerOn() 
	{
		active = true;
	}

	public void powerOff() 
	{
		active = false;
	}

	public InteractionResponse togglePower() 
	{
		if (!active)
		{
			if (isFuelAvailable())
			{
				powerOn();
				return new InteractionResponse(InteractionResult.SUCCESS, "Factory activated!");
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
		Recipe[] recipeArray = (Recipe[]) productionFactoryProperties.getRecipes().toArray();
		if (currentRecipe != null)
		{		
			int currentRecipeNumber = 0;

			for (int i=0; i <= recipeArray.length; i++)
			{
				if (recipeArray[i] == currentRecipe)
				{
					currentRecipeNumber = i;
				}
			}
			if (currentRecipeNumber == recipeArray.length)
			{
				currentRecipe = recipeArray[0];
				return new InteractionResponse(InteractionResult.SUCCESS, "Recipe switched! Current recipe is:" + currentRecipe.getRecipeName());
			}
			else
			{
				currentRecipe = recipeArray[currentRecipeNumber+1];
				return new InteractionResponse(InteractionResult.SUCCESS, "Recipe switched! Current recipe is:" + currentRecipe.getRecipeName());
			}
		}
		else
		{
			currentRecipe = recipeArray[0];
			return new InteractionResponse(InteractionResult.SUCCESS, "Recipe selected! Current recipe is:" + currentRecipe.getRecipeName());
		}	
	}
	
	public void setRecipe(Recipe newRecipe)
	{
		currentRecipe = newRecipe;
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
		return isMaterialAvailable(getPowerSourceInventory(), Material.COAL, 1);
	}

}
