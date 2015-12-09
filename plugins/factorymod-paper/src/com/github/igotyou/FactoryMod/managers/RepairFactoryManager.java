package com.github.igotyou.FactoryMod.managers;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;

import com.github.igotyou.FactoryMod.FactoryModManager;
import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.classicTriblockFactory.RepairFactory;
import com.github.igotyou.FactoryMod.properties.RepairFactoryProperties;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.AdvancedItemStack;

public class RepairFactoryManager extends FactoryModManager<RepairFactory>{

	public RepairFactoryManager(FactoryModPlugin plugin) {
		super(plugin);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Class<RepairFactory> getFactoryType() {
		return RepairFactory.class;
	}

	@Override
	public InteractionResponse createFactory(Location factoryLocation,
			Location inventoryLocation, Location powerLocation) {
		RepairFactoryProperties repairFactoryProperties = plugin.getRepairFactoryProperties();
		
		if(!factoryExistsAt(factoryLocation)) {
			Block inventoryBlock = inventoryLocation.getBlock();
			Chest chest = (Chest) inventoryBlock.getState();
			Inventory chestInventory = chest.getInventory();
			ItemList<AdvancedItemStack> constructionMaterials = repairFactoryProperties.getConstructionMaterials();
			
			if (constructionMaterials.exactlyIn(chestInventory)){
				RepairFactory factory = new RepairFactory(factoryLocation, inventoryLocation, powerLocation, false, repairFactoryProperties);
				constructionMaterials.removeFrom(factory.getInventory());

				if (addFactory(factory).getInteractionResult() == InteractionResult.FAILURE) {
					return new InteractionResponse(InteractionResult.FAILURE, "Unable to construct a " + repairFactoryProperties.getName());
				} else {
					return new InteractionResponse(InteractionResult.SUCCESS, "Successfully created a " + repairFactoryProperties.getName());
				}
			}
			return new InteractionResponse(InteractionResult.FAILURE, "Incorrect materials in chest! Stacks must match perfectly to create a " + repairFactoryProperties.getName());
		}
		return new InteractionResponse(InteractionResult.FAILURE, "There is already a " + repairFactoryProperties.getName() + " there!");
	}

	public boolean isClear(RepairFactory factory){
		return factory.getCenterLocation().getBlock().getType().equals(FactoryModPlugin.CENTRAL_BLOCK_MATERIAL) && 
				(!factoryExistsAt(factory.getCenterLocation())
				|| !factoryExistsAt(factory.getInventoryLocation()) 
				|| !factoryExistsAt(factory.getPowerSourceLocation()));
	}

	@Override
	public RepairFactory getFactory(Location factoryLocation) {
		for (RepairFactory factory: factories){
			if (factory.getCenterLocation().equals(factoryLocation)
					|| factory.getInventoryLocation().equals(factoryLocation)
					|| factory.getPowerSourceLocation().equals(factoryLocation))
				return factory;
		}
		return null;
	}

}
