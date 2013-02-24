package com.github.igotyou.FactoryMod.Factorys;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.FactoryObject;
import com.github.igotyou.FactoryMod.interfaces.Factory;
import com.github.igotyou.FactoryMod.managers.ProductionManager;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;

public class Production extends FactoryObject implements Factory
{

	private Material currentProductionItem;
	public static final FactoryType FACTORY_TYPE = FactoryType.PRODUCTION;
	public static SubFactoryType SUB_FACTORY_TYPE;
	
	public Production (Location factoryLocation, Location factoryInventoryLocation, Location factoryPowerSource
			, SubFactoryType subFactoryType)
	{
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource, Production.FACTORY_TYPE, subFactoryType);
		this.SUB_FACTORY_TYPE = subFactoryType;

	}

	public Production (Location factoryLocation, Location factoryInventoryLocation, Location factoryPowerSource,
			Material currentProductionItem, SubFactoryType subFactoryType)
	{
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource, Production.FACTORY_TYPE, subFactoryType);
		this.SUB_FACTORY_TYPE = subFactoryType;
		this.currentProductionItem = currentProductionItem;
	}
	
	public void update() 
	{
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
		// TODO Auto-generated method stub
		return null;
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
		return factoryPowerSource;
	}

}
