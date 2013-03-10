package com.github.igotyou.FactoryMod;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.Furnace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.interfaces.Properties;
import com.github.igotyou.FactoryMod.utility.InventoryMethods;

//original file:
/**
 * MachineObject.java
 * Purpose: Basic object base for machines to extend
 *
 * @author MrTwiggy
 * @version 0.1 1/14/13
 */
//edited version:
/**
 * FactoryObject.java	 
 * Purpose basic object base for factorys to extend
 * @author igotyou
 *
 */
public class FactoryObject
{
	public enum FactoryType
	{
		PRODUCTION, POWER
	}
	
	
	protected Location factoryLocation; // Current location of factory center
	protected Location factoryInventoryLocation;
	protected Location factoryPowerSourceLocation;
	protected boolean active; // Whether factory is currently active
	protected Inventory factoryInventory; // The inventory of the factory
	protected FactoryType factoryType; // The type this factory is
	protected String subFactoryType;
	protected Properties factoryProperties; // The properties of this factory type and tier
	
	protected boolean upgraded; // Whether the tier has recently upgraded
	private Inventory factoryPowerInventory;
	
	/**
	 * Constructor
	 */
	public FactoryObject(Location factoryLocation, Location factoryInventoryLocation, Location factoryPowerSource,
			FactoryType factoryType, String subFactoryType)
	{
		this.factoryLocation = factoryLocation;
		this.factoryInventoryLocation = factoryInventoryLocation;
		this.factoryPowerSourceLocation = factoryPowerSource;
		this.active = false;
		this.factoryType = factoryType;
		this.subFactoryType = subFactoryType;
		this.upgraded = false;
		initializeInventory();
		updateProperties();
	}

	/**
	 * Constructor
	 */
	public FactoryObject(Location factoryLocation, Location factoryInventoryLocation, Location factoryPowerSource,
			boolean active, FactoryType factoryType, String subFactoryType)
	{
		this.factoryLocation = factoryLocation;
		this.factoryInventoryLocation = factoryInventoryLocation;
		this.factoryPowerSourceLocation = factoryPowerSource;
		this.active = active;
		this.factoryType = factoryType;
		this.subFactoryType = subFactoryType;
		this.upgraded = false;
		initializeInventory();
		updateProperties();
	}
	
	/**
	 * Constructor
	 */
	public FactoryObject(Location factoryLocation, Location factoryInventoryLocation, Location factoryPowerSource,
			boolean active, int tierLevel, FactoryType factoryType, Inventory factoryInventory,
			String subFactoryType)
	{
		this.factoryLocation = factoryLocation;
		this.factoryInventoryLocation = factoryInventoryLocation;
		this.factoryPowerSourceLocation = factoryPowerSource;
		this.active = active;
		this.factoryType = factoryType;
		this.subFactoryType = subFactoryType;
		this.factoryInventory = factoryInventory;
		updateProperties();
	}

	/**
	 * Initializes the inventory for this factory
	 */
	public void initializeInventory()
	{
		switch(factoryType)
		{
		case PRODUCTION:
			Chest chestBlock = (Chest)factoryInventoryLocation.getBlock().getState();
			factoryInventory = chestBlock.getInventory();
			Furnace furnaceBlock = (Furnace)factoryPowerSourceLocation.getBlock().getState();
			factoryPowerInventory = furnaceBlock.getInventory();
			break;
		default:
			break;
		}
	}
	
	/**
	 * Updates the current properties for the current tier level
	 */
	public void updateProperties()
	{
		factoryProperties = FactoryModPlugin.getProperties(factoryType, subFactoryType);
	}
	
	/**
	 * Returns the user-friendly name for this factory type
	 */
	public String factoryName()
	{
		switch (factoryType)
		{
		case PRODUCTION:
			return "Production";
		default: 
			return null;
		}
	}

	public Inventory getInventory()
	{
		switch (factoryType)
		{
		case PRODUCTION:
			Chest chestBlock = (Chest)factoryInventoryLocation.getBlock().getState();
			factoryInventory = chestBlock.getInventory();
			return factoryInventory;
		default:
			return factoryInventory;
		}
	}

	public Inventory getPowerSourceInventory()
	{
		switch (factoryType)
		{
		case PRODUCTION:
			Furnace furnaceBlock = (Furnace)factoryPowerSourceLocation.getBlock().getState();
			factoryPowerInventory = furnaceBlock.getInventory();
			return factoryPowerInventory;
		default:
			return factoryPowerInventory;
		}
		
	
	}
	public String getSubFactoryType()
	{
		return subFactoryType;
	}
	
	public boolean getActive()
	{
		return active;
	}
}