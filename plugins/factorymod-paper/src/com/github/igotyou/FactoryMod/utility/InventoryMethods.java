package com.github.igotyou.FactoryMod.utility;

import java.util.ListIterator;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.interfaces.Properties;
import java.util.Iterator;
import java.util.List;
/**
 * InventoryMethods.java
 * @author igotyou
 * purpose: Various methods to manipulate inventorys.
 */
public class InventoryMethods 
{
	//returns a string cotaining the material names of the supplied hashap
	public static String getMaterialsNeededMessage(Map<ItemStack,String> itemStacks)
	{
		Iterator<ItemStack> itemStackItr=itemStacks.keySet().iterator();
		String returnValue = "";
		while(itemStackItr.hasNext())
		{
			ItemStack itemStack=itemStackItr.next();
			if (itemStack.getData() != null)
			{
				returnValue = returnValue + String.valueOf(itemStack.getAmount() + " " + itemStack.getData().toString() + ", ");
			}
			else
			{
				returnValue = returnValue + String.valueOf(itemStack.getAmount() + " " + itemStack.getType().toString() + ", ");
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
	
	//checks whether the inventory has atleast the amount of item's in the list
	public static boolean areItemStacksAvilable(Inventory inventory, Map<ItemStack,String> itemStacks)
	{
		boolean returnValue = true;
		Iterator<ItemStack> itemStackItr=itemStacks.keySet().iterator();
		while(itemStackItr.hasNext())
		{
			ItemStack itemStack=itemStackItr.next();
			if (!isItemStackAvailable(inventory, itemStack))
			{
				returnValue = false;
			}
		}
		return returnValue;
	}

	//checks whether the inventory has atleast one of the item stack inputs
	public static boolean isOneItemStackAvilable(Inventory inventory, Map<ItemStack,String> itemStacks)
	{
		Iterator<ItemStack> itemStackItr=itemStacks.keySet().iterator();
		while(itemStackItr.hasNext())
		{
			ItemStack itemStack=itemStackItr.next();
			if (isItemStackAvailable(inventory, itemStack))
			{
				return true;
			}
		}
		return false;
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
	public static boolean itemStacksMatch(Inventory inventory, Map<ItemStack,String> itemStacks)
	{
		boolean returnValue = true;
		Iterator<ItemStack> itemStackItr=itemStacks.keySet().iterator();
		while(itemStackItr.hasNext())
		{
			ItemStack itemStack=itemStackItr.next();
			if (!itemStackMatches(inventory, itemStack))
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

	//removes the itemStacks from the supplied List from the inventory.
	public static boolean removeItemStacks(Inventory inventory, Map<ItemStack,String> itemStacks)
	{
		if(areItemStacksAvilable(inventory,itemStacks))
		{
			boolean returnValue = true;
			Iterator<ItemStack> itemStackItr=itemStacks.keySet().iterator();
			while(itemStackItr.hasNext())
			{
				ItemStack itemStack=itemStackItr.next();
				if (!removeItemStack(inventory, itemStack))
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
	public static ItemStack removeOneItemStack(Inventory inventory, Map<ItemStack,String> itemStacks)
	{
		ItemStack removedItem=null;
		Iterator<ItemStack> itemStackItr=itemStacks.keySet().iterator();
		while(itemStackItr.hasNext())
		{
			ItemStack itemStack=itemStackItr.next();
			if (removeItemStack(inventory, itemStack))
			{
				removedItem=itemStack;
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