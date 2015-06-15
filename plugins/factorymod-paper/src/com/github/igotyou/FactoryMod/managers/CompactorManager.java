package com.github.igotyou.FactoryMod.managers;

import org.bukkit.Location;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.Factorys.Compactor;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;

public class CompactorManager extends AManager<Compactor> {
    
    public CompactorManager(FactoryModPlugin plugin) {
    	super(plugin);
    }

    @Override
    public InteractionResponse createFactory(Location factoryLocation,
            Location inventoryLocation, Location powerLocation) {
        return null;
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
