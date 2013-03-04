package com.github.igotyou.FactoryMod;

import java.util.HashMap;
import java.util.Iterator;
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
	
	/**
	 * Returns whether there is enough material available for an upgrade in cloaker inventory
	 */	
	public boolean buildMaterialAvailable(Properties desiredProperties)
	{
		boolean returnValue = true;
		for (int i = 1; i <= desiredProperties.getBuildMaterials().size(); i++)
		{
			if (!isMaterialAvailable(getInventory(), desiredProperties.getBuildMaterials().get(i)))
			{
				returnValue = false;
			}
		}
		return returnValue;
	}
	
	public void addMaterial(Inventory inventory, Material material, int amount)
	{
		ItemStack itemStack = new ItemStack(material, amount);
		inventory.addItem(itemStack);
	}
	
	public void addItem(Inventory inventory, ItemStack itemStack)
	{
		inventory.addItem(itemStack);
	}
	
	/**
	 * Attempts to remove materials for upgrading from cloaker inventory
	 */
	public boolean removeBuildMaterial(Properties desiredProperties)
	{
		boolean returnValue = true;
		for (int i = 1; i <= desiredProperties.getBuildMaterials().size(); i++)
		{
			if (!removeMaterial(getInventory(), desiredProperties.getBuildMaterials().get(i)))
			{
				returnValue = false;
			}
		}
		return returnValue;
	}
	
	public boolean removeMaterials(Inventory inventory, HashMap<Integer, Material> materials, HashMap<Integer, Integer> amount)
	{
		boolean returnValue = true;
		for (int i = 1; i <= materials.size(); i++)
		{
			if (!removeMaterial(inventory, materials.get(i), amount.get(i)))
			{
				returnValue = false;
			}
		}
		return returnValue;
	}
	
	public boolean removeMaterials(Inventory inventory, HashMap<Integer, ItemStack> itemStack)
	{
		boolean returnValue = true;
		for (int i = 1; i <= itemStack.size(); i++)
		{
			if (!removeMaterial(inventory, itemStack.get(i)))
			{
				returnValue = false;
			}
		}
		return returnValue;
	}

	
	/**
	 * Attempts to remove a specific material of given amount from dispenser
	 */
	public boolean removeMaterial(Inventory inventory, Material material, int amount)
	{
		HashMap<Integer,? extends ItemStack> inventoryMaterials = inventory.all(material);
		
		int materialsToRemove = amount;
		for(Entry<Integer,? extends ItemStack> entry : inventoryMaterials.entrySet())
		{
			if (materialsToRemove <= 0)
				break;
			
			if(entry.getValue().getAmount() == materialsToRemove)
			{
				inventory.setItem(entry.getKey(), new ItemStack(Material.AIR, 0));
				materialsToRemove = 0;
			}
			else if(entry.getValue().getAmount() > materialsToRemove)
			{
				inventory.setItem(entry.getKey(), new ItemStack(material, (entry.getValue().getAmount() - materialsToRemove)));
				materialsToRemove = 0;
			}
			else
			{
				int inStack = entry.getValue().getAmount();
				inventory.setItem(entry.getKey(), new ItemStack(Material.AIR, 0));
				materialsToRemove -= inStack;
			}
		}
		
		return materialsToRemove == 0;
	}

	public boolean removeMaterial(Inventory inventory, ItemStack itemStack)
	{		
		int materialsToRemove = itemStack.getAmount();
		ListIterator<ItemStack> iterator = inventory.iterator();
		
		while(iterator.hasNext())
		{
			ItemStack currentItemStack = iterator.next();
			if (currentItemStack != null)
			{
				if (currentItemStack.isSimilar(itemStack))
				{
					if (materialsToRemove <= 0)
						break;
					
					if(currentItemStack.getAmount() == materialsToRemove)
					{
						iterator.set(new ItemStack(Material.AIR, 0));
						materialsToRemove = 0;
					}
					else if(currentItemStack.getAmount() > materialsToRemove)
					{
						ItemStack temp = currentItemStack.clone();
						temp.setAmount(currentItemStack.getAmount() - materialsToRemove);
						iterator.set(temp);
						materialsToRemove = 0;
					}
					else
					{
						int inStack = currentItemStack.getAmount();
						iterator.set(new ItemStack(Material.AIR, 0));
						materialsToRemove -= inStack;
					}
				}
			}
		}				
		return materialsToRemove == 0;
	}
	
	public boolean areMaterialsAvailable(Inventory inventory, HashMap<Integer, Material> materials, HashMap<Integer, Integer> amount)
	{
		boolean returnValue = true;
		for (int i = 1; i <= materials.size(); i++)
		{
			if (!isMaterialAvailable(inventory, materials.get(i), amount.get(i)))
			{
				returnValue = false;
			}
		}
		return returnValue;
	}
	
	public static boolean areMaterialsAvailable(Inventory inventory, HashMap<Integer, ItemStack> itemStack)
	{
		boolean returnValue = true;
		for (int i = 1; i <= itemStack.size(); i++)
		{

			if (!isMaterialAvailable(inventory, itemStack.get(i)))
			{
				returnValue = false;
			}
		}
		return returnValue;
	}
	
	public static boolean materialsMatch(Inventory inventory, HashMap<Integer, ItemStack> itemStack)
	{
		boolean returnValue = true;
		for (int i = 1; i <= itemStack.size(); i++)
		{

			if (!materialMatches(inventory, itemStack.get(i)))
			{
				returnValue = false;
			}
		}
		return returnValue;
	}
	
	/**
	 * Checks if a specific material of given amount is available in dispenser
	 */
	public boolean isMaterialAvailable(Inventory inventory, Material material, int amount)
	{
		HashMap<Integer,? extends ItemStack> inventoryMaterials = inventory.all(material);
		
		int totalMaterial = 0;
		for(Entry<Integer,? extends ItemStack> entry : inventoryMaterials.entrySet())
		{
			totalMaterial += entry.getValue().getAmount();
		}
		
		return (totalMaterial >= amount);
	}
	
	public static boolean isMaterialAvailable(Inventory inventory, ItemStack itemStack)
	{
		ListIterator<ItemStack> iterator = inventory.iterator();
		int totalMaterial = 0;
		
		while(iterator.hasNext())
		{
			ItemStack currentItemStack = iterator.next();
			if (currentItemStack != null)
			{
				if (currentItemStack.isSimilar(itemStack) ||
						itemStack.getType() == Material.NETHER_WARTS && currentItemStack.getType() == Material.NETHER_WARTS)
				{		
					totalMaterial += currentItemStack.getAmount();
				}
			}
		}
		if (totalMaterial >= itemStack.getAmount())
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public static boolean materialMatches(Inventory inventory, ItemStack itemStack)
	{
		ListIterator<ItemStack> iterator = inventory.iterator();
		boolean returnValue = false;
		
		while(iterator.hasNext())
		{
			ItemStack currentItemStack = iterator.next();
			if (currentItemStack != null)
			{
				if (currentItemStack.equals(itemStack))
				{		
					returnValue = !returnValue;
				}
			}
		}
		return returnValue;
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
	
	public static String getMaterialsNeededMessage(HashMap<Integer, ItemStack> itemStack)
	{
		String returnValue = "";
		for (int i = 1; i <= itemStack.size(); i++)
		{
			if (itemStack.get(i).getData() != null)
			{
				returnValue = returnValue + String.valueOf(itemStack.get(i).getAmount() + " " + itemStack.get(i).getData().toString() + ", ");
			}
			else
			{
				returnValue = returnValue + String.valueOf(itemStack.get(i).getAmount() + " " + itemStack.get(i).getType().toString() + ", ");
			}
		}
		return returnValue;
	}
	
	public static String getEnchantmentsMessage(Map<Enchantment, Integer> enchants)
	{
		if (enchants != null)
		{
			String returnValue = " with ";

			for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet())
			{
				returnValue = returnValue + entry.getKey().getName() + " " + entry.getValue() + ", ";
			}
			return returnValue;
		}
		return "";
	}
}