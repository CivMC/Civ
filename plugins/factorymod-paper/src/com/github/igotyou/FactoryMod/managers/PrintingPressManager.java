package com.github.igotyou.FactoryMod.managers;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;

import com.github.igotyou.FactoryMod.FactoryModManager;
import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.Factorys.PrintingPress;
import com.github.igotyou.FactoryMod.properties.PrintingPressProperties;
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

public class PrintingPressManager extends FactoryModManager<PrintingPress>
{
	//private List<PrintingPress> presses;
	
	public PrintingPressManager(FactoryModPlugin plugin)
	{
		super(plugin);
	}

	@Override
	public InteractionResponse createFactory(Location factoryLocation, Location inventoryLocation, Location powerSourceLocation) 
	{
	PrintingPressProperties printingPressProperties = plugin.getPrintingPressProperties();
		
		if (!factoryExistsAt(factoryLocation))
		{
			Block inventoryBlock = inventoryLocation.getBlock();
			Chest chest = (Chest) inventoryBlock.getState();
			Inventory chestInventory = chest.getInventory();
			ItemList<AdvancedItemStack> inputs = printingPressProperties.getConstructionMaterials();
			boolean hasMaterials = inputs.allIn(chestInventory);
			if (hasMaterials)
			{
				PrintingPress production = new PrintingPress(factoryLocation, inventoryLocation, powerSourceLocation, false, plugin.getPrintingPressProperties());
				if (printingPressProperties.getConstructionMaterials().removeFrom(production.getInventory()))
				{
					addFactory(production);
					return new InteractionResponse(InteractionResult.SUCCESS, "Successfully created " + printingPressProperties.getName());
				}
			}
			return new InteractionResponse(InteractionResult.FAILURE, "Not enough materials in chest!");
		}
		return new InteractionResponse(InteractionResult.FAILURE, "There is already a " + printingPressProperties.getName() + " there!");
	}

	public boolean isClear(PrintingPress factory){
		return factory.getCenterLocation().getBlock().getType().equals(FactoryModPlugin.CENTRAL_BLOCK_MATERIAL) && (!factoryExistsAt(factory.getCenterLocation()))
				|| !factoryExistsAt(factory.getInventoryLocation()) || !factoryExistsAt(factory.getPowerSourceLocation());
	}

	@Override
	public PrintingPress getFactory(Location factoryLocation) 
	{
		for (PrintingPress production : factories)
		{
			if (production.getCenterLocation().equals(factoryLocation) || production.getInventoryLocation().equals(factoryLocation)
					|| production.getPowerSourceLocation().equals(factoryLocation))
				return production;
		}
		return null;
	}

	@Override
	public Class<PrintingPress> getFactoryType() {
		return PrintingPress.class;
	}

}
