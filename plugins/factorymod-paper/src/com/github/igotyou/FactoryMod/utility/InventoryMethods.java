package com.github.igotyou.FactoryMod.utility;

import java.util.ListIterator;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.interfaces.Properties;
import java.util.List;
/**
 * InventoryMethods.java
 * @author igotyou
 * purpose: Various methods to manipulate inventorys.
 */
public class InventoryMethods 
{
	//returns a string cotaining the material names of the supplied hashap
	public static String getMaterialsNeededMessage(List<ItemStack> itemStack)
	{
		String returnValue = "";
		for (int i = 0; i < itemStack.size(); i++)
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
	
	//returns a string cotaining all enchantments in the supplied hashmap
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
	
	//checks whever or not there are atleast the amount of item's nedded for the supplied property.
	public static boolean buildMaterialAvailable(Inventory inventory, Properties desiredProperties)
	{
		boolean returnValue = true;
		for (int i = 0; i < desiredProperties.getBuildMaterials().size(); i++)
		{
			if (!isItemStackAvailable(inventory, desiredProperties.getBuildMaterials().get(i)))
			{
				returnValue = false;
			}
		}
		return returnValue;
	}
	
	//checks whether the inventory has atleast the amount of item's in the list
	public static boolean areItemStacksAvilable(Inventory inventory, List<ItemStack> itemStacks)
	{
		boolean returnValue = true;
		for (int i = 0; i < itemStacks.size(); i++)
		{

			if (!isItemStackAvailable(inventory, itemStacks.get(i)))
			{
				returnValue = false;
			}
		}
		return returnValue;
	}

	//checks whether the inventory has atleast one of the item stack inputs
	public static boolean isOneItemStackAvilable(Inventory inventory, List<ItemStack> itemStacks)
	{
		boolean returnValue = false;
		for (int i = 0; i < itemStacks.size(); i++)
		{

			if (isItemStackAvailable(inventory, itemStacks.get(i)))
			{
				returnValue = true;
			}
		}
		return returnValue;
	}
	
	//checks whether or not the inventory contains atleast the amount of items in the itemstack
	public static boolean isItemStackAvailable(Inventory inventory, ItemStack itemStack)
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
	
	//checks whether or not the inventory cotains EXACTLY the amount of items in the supplied hashMap
	public static boolean itemStacksMatch(Inventory inventory, List<ItemStack> itemStack)
	{
		boolean returnValue = true;
		for (int i = 0; i < itemStack.size(); i++)
		{

			if (!itemStackMatches(inventory, itemStack.get(i)))
			{
				returnValue = false;
			}
		}
		return returnValue;
	}
	
	//checks if the inventory cotains a itemStack with exactly the amount as the supplied itemStack
	public static boolean itemStackMatches(Inventory inventory, ItemStack itemStack)
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
	
	//removes the build materials nedded(form the properties file)
	public static boolean removeBuildMaterial(Inventory inventory, Properties desiredProperties)
	{
		boolean returnValue = true;
		for (int i = 0; i < desiredProperties.getBuildMaterials().size(); i++)
		{
			if (!removeItemStack(inventory, desiredProperties.getBuildMaterials().get(i)))
			{
				returnValue = false;
			}
		}
		return returnValue;
	}
	
	//removes the itemStacks from the supplied List from the inventory.
	public static boolean removeItemStacks(Inventory inventory, List<ItemStack> itemStacks)
	{
		if(areItemStacksAvilable(inventory,itemStacks))
		{
			boolean returnValue = true;
			for (int i = 0; i < itemStacks.size(); i++)
			{
				if (!removeItemStack(inventory, itemStacks.get(i)))
				{
					returnValue = false;
				}
			}
			return returnValue;
		}
		else
		{
			return false;
		}
	}
	
	//removes one of the items in the list from the inventory. The first item in the list
	//which matches is removed. The item is then returned
	public static ItemStack removeOneItemStack(Inventory inventory, List<ItemStack> itemStacks)
	{
		ItemStack removedItem=null;
		for (int i = 0; i < itemStacks.size(); i++)
		{
			if (removeItemStack(inventory, itemStacks.get(i)))
			{
				removedItem=itemStacks.get(i);
				break;
			}
		}
		return removedItem;
	}
	
	//removes the amount of items in the supplied ItemStack from the inventory
	public static boolean removeItemStack(Inventory inventory, ItemStack itemStack)
	{		
		if(isItemStackAvailable(inventory,itemStack))
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
		else
		{
			return false;
		}
	}
}