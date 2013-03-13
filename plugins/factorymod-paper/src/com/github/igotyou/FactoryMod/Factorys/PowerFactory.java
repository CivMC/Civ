package com.github.igotyou.FactoryMod.Factorys;

import org.bukkit.Location;

import com.github.igotyou.FactoryMod.FactoryObject;
import com.github.igotyou.FactoryMod.interfaces.Factory;
import com.github.igotyou.FactoryMod.properties.PowerProperties;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;

public class PowerFactory extends FactoryObject implements Factory
{
	public static final FactoryType FACTORY_TYPE = FactoryType.POWER;
	public String SUB_FACTORY_TYPE;
	private PowerProperties powerFactoryProduction;
	private int outputTimer = 0;
	private int energyTimer = 0;
	
	public PowerFactory (Location factoryLocation, Location factoryInventoryLocation, Location factoryPowerSource
			, String subFactoryType)
	{
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource, ProductionFactory.FACTORY_TYPE, subFactoryType);
		this.SUB_FACTORY_TYPE = subFactoryType;
		this.powerFactoryProduction = (PowerProperties) factoryProperties;
		
	}

	public void update() 
	{
			
	}

	
	public void destroy(Location destroyLocation) 
	{
			
	}

	public void powerOn() 
	{
			
	}

	public void powerOff() 
	{
		
	}

	public InteractionResponse togglePower() 
	{
		return null;
	}

	public Location getCenterLocation() 
	{
		return null;
	}

	public Location getInventoryLocation() 
	{
		return null;
	}

	public Location getPowerSourceLocation() 
	{
		return null;
	}
}
