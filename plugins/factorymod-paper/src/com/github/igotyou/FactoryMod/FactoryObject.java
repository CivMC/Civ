package com.github.igotyou.FactoryMod;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.interfaces.Properties;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;

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
		PRODUCTION
	}
	public enum SubFactoryType
	{
		WORKSHOP,
		MOSSYCOBBLE
	}
	
	
	protected Location factoryLocation; // Current location of factory center
	protected Location factoryInventoryLocation;
	protected Location factoryPowerSource;
	protected boolean active; // Whether factory is currently active
	protected Inventory factoryInventory; // The inventory of the factory
	protected FactoryType factoryType; // The type this factory is
	protected SubFactoryType subFactoryType;
	protected Properties factoryProperties; // The properties of this factory type and tier
	
	protected boolean upgraded; // Whether the tier has recently upgraded
	
	/**
	 * Constructor
	 */
	public FactoryObject(Location factoryLocation, Location factoryInventoryLocation, Location factoryPowerSource,
			FactoryType factoryType, SubFactoryType subFactoryType)
	{
		this.factoryLocation = factoryLocation;
		this.factoryInventoryLocation = factoryInventoryLocation;
		this.factoryPowerSource = factoryPowerSource;
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
			boolean active, FactoryType factoryType, SubFactoryType subFactoryType)
	{
		this.factoryLocation = factoryLocation;
		this.factoryInventoryLocation = factoryInventoryLocation;
		this.factoryPowerSource = factoryPowerSource;
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
			SubFactoryType subFactoryType)
	{
		this.factoryLocation = factoryLocation;
		this.factoryInventoryLocation = factoryInventoryLocation;
		this.factoryPowerSource = factoryPowerSource;
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
			Chest chestBlock = (Chest)factoryLocation.getBlock();
			factoryInventory = chestBlock.getInventory();
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
	
	/**
	 * Returns whether there is enough material available for an upgrade in cloaker inventory
	 */
	public boolean upgradeMaterialAvailable(int desiredTier)
	{
		Properties desiredProperties = FactoryModPlugin.getProperties(factoryType, subFactoryType);
		boolean returnValue = true;
		for (int i = 1; i <= desiredProperties.getBuildMaterial().size(); i++)
		{
			if (!isMaterialAvailable(desiredProperties.getBuildAmount().get(i), desiredProperties.getBuildMaterial().get(i)))
			{
				returnValue = false;
			}
		}
		return returnValue;
	}
	
	public boolean buildMaterialAvailable(Properties desiredProperties)
	{
		boolean returnValue = true;
		for (int i = 1; i <= desiredProperties.getBuildMaterial().size(); i++)
		{
			if (!isMaterialAvailable(desiredProperties.getBuildAmount().get(i), desiredProperties.getBuildMaterial().get(i)))
			{
				returnValue = false;
			}
		}
		return returnValue;
	}
	
	/**
	 * Attempts to remove materials for upgrading from cloaker inventory
	 */
	public boolean removeBuildMaterial(Properties desiredProperties)
	{
		boolean returnValue = true;
		for (int i = 1; i <= desiredProperties.getBuildMaterial().size(); i++)
		{
			if (!removeMaterial(desiredProperties.getBuildAmount().get(i), desiredProperties.getBuildMaterial().get(i)))
			{
				returnValue = false;
			}
		}
		return returnValue;
	}
	
	/**
	 * Attempts to remove a specific material of given amount from dispenser
	 */
	public boolean removeMaterial(int amount, Material material)
	{
		HashMap<Integer,? extends ItemStack> inventoryMaterials = getInventory().all(material);
		
		int materialsToRemove = amount;
		for(Entry<Integer,? extends ItemStack> entry : inventoryMaterials.entrySet())
		{
			if (materialsToRemove <= 0)
				break;
			
			if(entry.getValue().getAmount() == materialsToRemove)
			{
				getInventory().setItem(entry.getKey(), new ItemStack(Material.AIR, 0));
				materialsToRemove = 0;
			}
			else if(entry.getValue().getAmount() > materialsToRemove)
			{
				getInventory().setItem(entry.getKey(), new ItemStack(material, (entry.getValue().getAmount() - materialsToRemove)));
				materialsToRemove = 0;
			}
			else
			{
				int inStack = entry.getValue().getAmount();
				getInventory().setItem(entry.getKey(), new ItemStack(Material.AIR, 0));
				materialsToRemove -= inStack;
			}
		}
		
		return materialsToRemove == 0;
	}
	
	/**
	 * Checks if a specific material of given amount is available in dispenser
	 */
	public boolean isMaterialAvailable(int amount, Material material)
	{
		HashMap<Integer,? extends ItemStack> inventoryMaterials = getInventory().all(material);
		
		int totalMaterial = 0;
		for(Entry<Integer,? extends ItemStack> entry : inventoryMaterials.entrySet())
		{
			totalMaterial += entry.getValue().getAmount();
		}
		
		return (totalMaterial >= amount);
	}
	
	
	/**
     * Returns how much of a specified material is available in dispenser
	 */
	public int getMaterialAvailableAmount(Material material)
	{
		HashMap<Integer,? extends ItemStack> inventoryMaterials = getInventory().all(material);
		
		int totalMaterial = 0;
		for(Entry<Integer,? extends ItemStack> entry : inventoryMaterials.entrySet())
		{
			totalMaterial += entry.getValue().getAmount();
		}
		
		return totalMaterial;
	}

	/**
	 * 'cloakerInventory' public accessor
	 */
	public Inventory getInventory()
	{
		switch (factoryType)
		{
		case PRODUCTION:
			Chest chestBlock = (Chest)factoryLocation.getBlock();
			factoryInventory = chestBlock.getInventory();
			return factoryInventory;
		default:
			return factoryInventory;
		}
	}

}