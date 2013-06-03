package com.github.igotyou.FactoryMod.Factorys;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;

import com.github.igotyou.FactoryMod.FactoryObject;
import com.github.igotyou.FactoryMod.interfaces.Factory;
import com.github.igotyou.FactoryMod.properties.PrintingPressProperties;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.NamedItemStack;

public class PrintingPress extends BaseFactory {
	
	private PrintingPressProperties printingPressProperties;

	public PrintingPress(Location factoryLocation,
			Location factoryInventoryLocation, Location factoryPowerSource,
			boolean active, PrintingPressProperties printingPressProperties) {
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource, active,
				FactoryType.PRINTING_PRESS, "press");
		this.printingPressProperties = printingPressProperties;
	}
	
	

	public PrintingPress(Location factoryLocation,
			Location factoryInventoryLocation, Location factoryPowerSource,
			boolean active,
			int currentProductionTimer, int currentEnergyTimer,
			double currentMaintenance, long timeDisrepair, PrintingPressProperties printingPressProperties) {
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource,
				FactoryType.PRINTING_PRESS, active, "Printing Press", currentProductionTimer,
				currentEnergyTimer, currentMaintenance, timeDisrepair);
		this.active = active;
		this.printingPressProperties = printingPressProperties;
	}

	@Override
	public ItemList<NamedItemStack> getFuel() {
		return printingPressProperties.getFuel();
	}

	@Override
	public double getEnergyTime() {
		return 2;
	}

	@Override
	public double getProductionTime() {
		return 20;
	}

	@Override
	public ItemList<NamedItemStack> getInputs() {
		ItemList<NamedItemStack> inputs = new ItemList<NamedItemStack>();
		return inputs;
	}

	@Override
	public ItemList<NamedItemStack> getOutputs() {
		ItemList<NamedItemStack> outputs = new ItemList<NamedItemStack>();
		NamedItemStack pages = new NamedItemStack(Material.PAPER, 64, (short) 0, "pages");
		pages.getItemMeta().setDisplayName("Test Page");
		outputs.add(pages);
		return outputs;
	}

	@Override
	public ItemList<NamedItemStack> getRepairs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void recipeFinished() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getMaxRepair() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
}
