package com.github.igotyou.FactoryMod.managers;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;

import com.github.igotyou.FactoryMod.FactoryModManager;
import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.Factorys.ProductionFactory;
import com.github.igotyou.FactoryMod.properties.ProductionProperties;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.AdvancedItemStack;

//original file:
/**
* Manager.java
* Purpose: Interface for Manager objects for basic manager functionality
*
* @author MrTwiggy
* @version 0.1 1/08/13
*/
//edited version:
/**
* Manager.java	 
* Purpose: Interface for Manager objects for basic manager functionality
* @author igotyou
*
*/

public class ProductionFactoryManager extends FactoryModManager<ProductionFactory>
{
	@Override
	public Class<ProductionFactory> getFactoryType() {
		return ProductionFactory.class;
	}
	//private List<ProductionFactory> producers;
	
	public ProductionFactoryManager(FactoryModPlugin plugin)
	{
		super(plugin);
	}

	@Override
	public InteractionResponse createFactory(Location factoryLocation, Location inventoryLocation, Location powerSourceLocation) 
	{
		if (!factoryExistsAt(factoryLocation))
		{
			Map<String, ProductionProperties> properties = FactoryModPlugin.productionProperties;
			Block inventoryBlock = inventoryLocation.getBlock();
			Chest chest = (Chest) inventoryBlock.getState();
			Inventory chestInventory = chest.getInventory();
			String subFactoryType = null;
			for (Map.Entry<String, ProductionProperties> entry : properties.entrySet())
			{
				ItemList<AdvancedItemStack> inputs = entry.getValue().getInputs();
				if(inputs.exactlyIn(chestInventory))
				{
					subFactoryType = entry.getKey();
				}
			}
			if (subFactoryType != null)
			{
				ProductionFactory production = new ProductionFactory(factoryLocation, inventoryLocation, powerSourceLocation,subFactoryType);
				if (properties.get(subFactoryType).getInputs().allIn(production.getInventory()))
				{
					addFactory(production);
					properties.get(subFactoryType).getInputs().removeFrom(production.getInventory());
					return new InteractionResponse(InteractionResult.SUCCESS, "Successfully created " + production.getProductionFactoryProperties().getName());
				}
			}
			return new InteractionResponse(InteractionResult.FAILURE, "Incorrect materials in chest! Stacks must match perfectly.");
		}
		return new InteractionResponse(InteractionResult.FAILURE, "There is already a factory there!");
	}
	
	public InteractionResponse createFactory(Location factoryLocation, Location inventoryLocation, Location powerSourceLocation, int productionTimer, int energyTimer) 
	{
		if (!factoryExistsAt(factoryLocation))
		{
			Map<String, ProductionProperties> properties = FactoryModPlugin.productionProperties;
			Block inventoryBlock = inventoryLocation.getBlock();
			Chest chest = (Chest) inventoryBlock.getState();
			Inventory chestInventory = chest.getInventory();
			String subFactoryType = null;
			boolean hasMaterials = true;
			for (Map.Entry<String, ProductionProperties> entry : properties.entrySet())
			{
				ItemList<AdvancedItemStack> inputs = entry.getValue().getInputs();
				if(!inputs.allIn(chestInventory))
				{
					hasMaterials = false;
				}
				if (hasMaterials == true)
				{
					subFactoryType = entry.getKey();
				}
			}
			if (hasMaterials && subFactoryType != null)
			{
				ProductionFactory production = new ProductionFactory(factoryLocation, inventoryLocation, powerSourceLocation,subFactoryType);
				if (properties.get(subFactoryType).getInputs().removeFrom(production.getInventory()))
				{
					addFactory(production);
					return new InteractionResponse(InteractionResult.SUCCESS, "Successfully created " + subFactoryType + " production factory");
				}
			}
			return new InteractionResponse(InteractionResult.FAILURE, "Not enough materials in chest!");
		}
		return new InteractionResponse(InteractionResult.FAILURE, "There is already a factory there!");
	}
	
	public boolean isClear(ProductionFactory factory){
		return factory.getCenterLocation().getBlock().getType().equals(Material.WORKBENCH) && (!factoryExistsAt(factory.getCenterLocation()))
				|| !factoryExistsAt(factory.getInventoryLocation()) || !factoryExistsAt(factory.getPowerSourceLocation());
	}

	@Override
	public ProductionFactory getFactory(Location factoryLocation) 
	{
		for (ProductionFactory production : factories)
		{
			if (production.getCenterLocation().equals(factoryLocation) || production.getInventoryLocation().equals(factoryLocation)
					|| production.getPowerSourceLocation().equals(factoryLocation))
				return production;
		}
		return null;
	}

}
