package com.github.igotyou.FactoryMod.managers;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;

import com.github.igotyou.FactoryMod.FactoryModManager;
import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.Factorys.Compactor;
import com.github.igotyou.FactoryMod.properties.CompactorProperties;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.AdvancedItemStack;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;

public class CompactorManager extends FactoryModManager<Compactor> {
    
    public CompactorManager(FactoryModPlugin plugin) {
    	super(plugin);
    }

    @Override
    public InteractionResponse createFactory(Location factoryLocation,
            Location inventoryLocation, Location powerLocation) {
        CompactorProperties compactorProperties = plugin.getCompactorProperties();
    	
        if (!factoryExistsAt(factoryLocation)) {
            Block inventoryBlock = inventoryLocation.getBlock();
            Chest chest = (Chest) inventoryBlock.getState();
            Inventory chestInventory = chest.getInventory();
            ItemList<AdvancedItemStack> inputs = compactorProperties.getConstructionMaterials();

			if (inputs.exactlyIn(chestInventory)) {
				Compactor production = new Compactor(factoryLocation, inventoryLocation, powerLocation, false, plugin.getCompactorProperties());
				inputs.removeFrom(production.getInventory());
				
				if (addFactory(production).getInteractionResult() == InteractionResult.FAILURE) {
					return new InteractionResponse(InteractionResult.FAILURE, "Unable to create a " + compactorProperties.getName());
				} else {
					return new InteractionResponse(InteractionResult.SUCCESS, "Successfully created " + compactorProperties.getName());
				}
			}
			return new InteractionResponse(InteractionResult.FAILURE, "Not enough materials in chest to create a " + compactorProperties.getName());
		}
		return new InteractionResponse(InteractionResult.FAILURE, "There is already a " + compactorProperties.getName() + " there!");
    }

    @Override
    public boolean isClear(Compactor factory){
    	return factory.getCenterLocation().getBlock().getType().equals(FactoryModPlugin.CENTRAL_BLOCK_MATERIAL) &&
                (!factoryExistsAt(factory.getCenterLocation())
                || !factoryExistsAt(factory.getInventoryLocation())
                || !factoryExistsAt(factory.getPowerSourceLocation()));
    }

    @Override
    public Compactor getFactory(Location factoryLocation) {
        for(Compactor factory : factories) {
            if(factory.getCenterLocation().equals(factoryLocation)
                    || factory.getInventoryLocation().equals(factoryLocation)
                    || factory.getPowerSourceLocation().equals(factoryLocation)) {
                return factory;
            }
        }
        return null;
    }

	@Override
	public Class<Compactor> getFactoryType() {
		return Compactor.class;
	}
}
