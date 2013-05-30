/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.untamedears.ItemExchange.utility;

import com.untamedears.ItemExchange.ItemExchangePlugin;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Brian
 */
public class InventoryHelpers {
	/*
	 * Checks if there is an input/output pair
	 */
	public static boolean hasIO(Inventory inventory)
	{
		ItemStack[] contents=inventory.getContents();
		for(int i=0;i<contents.length;i++)
		{
			if(contents[i].hasItemMeta()&&contents[i].getItemMeta().hasDisplayName()&&contents[i].getItemMeta().getDisplayName().equals(ItemExchangePlugin.INPUT_NAME))
			{
				if(contents[i+1].hasItemMeta()&&contents[i+1].getItemMeta().hasDisplayName()&&contents[i+1].getItemMeta().getDisplayName().equals(ItemExchangePlugin.OUTPUT_NAME))
				{
					return true;
				}
			}
		}
		return false;
	}
	/*
	 * Gets an input/output pair
	 */
	public static ItemRule[] getIO(Inventory inventory)
	{
		ItemStack[] contents=inventory.getContents();
		for(int i=0;i<contents.length;i++)
		{
			if(contents[i].hasItemMeta()&&contents[i].getItemMeta().hasDisplayName()&&contents[i].getItemMeta().getDisplayName().equals(ItemExchangePlugin.INPUT_NAME))
			{
				if(contents[i+1].hasItemMeta()&&contents[i+1].getItemMeta().hasDisplayName()&&contents[i+1].getItemMeta().getDisplayName().equals(ItemExchangePlugin.OUTPUT_NAME))
				{
					ItemRule[] IO=new ItemRule[2];
					IO[0]=new ItemRule(contents[i].getItemMeta().getLore(),ItemExchangePlugin.VERSION);
					IO[1]=new ItemRule(contents[i+1].getItemMeta().getLore(),ItemExchangePlugin.VERSION);
					return IO;
				}
			}
		}
		return null;
	}
	
	/*
	 * Returns an input following the ItemRules from the inventory
	 */
	public static List<ItemStack> getItemStacks(Inventory inventory,ItemRule itemRule)
	{
		List<ItemStack> itemStacks=new ArrayList<>();
		//Gets the ItemStacks from the inventory to be transfered
		int requiredAmount=itemRule.getAmount();
		ItemStack[] contents=inventory.getContents();
		for(int i=0;i<contents.length && requiredAmount>0;i++)
		{
			ItemStack itemStack=contents[i];
			if(itemRule.followsRules(itemStack))
			{
				if(itemStack.getAmount()<=requiredAmount)
				{
					itemStacks.add(itemStack);
					requiredAmount-=itemStack.getAmount();
				}
				else
				{
					ItemStack itemStackClone=itemStack.clone();
					itemStackClone.setAmount(requiredAmount);
					itemStacks.add(itemStackClone);
				}
			}
		}
		return itemStacks;
	}
	
	/*
	 * Gets the amount of the ItemStack in the inventory
	 */
	public static int amountIn(Inventory inventory,ItemStack itemStack)
	{
		int amount=0;
		for(ItemStack invItemStack:inventory.all(itemStack).values())
		{
			amount+=invItemStack.getAmount();
		}
		return amount;
	}
	
	/*
	 * Checks if a set of items fits in an inventory
	 */
	public static boolean fitsIn(Inventory inventory,List<ItemStack> itemStacks)
	{
		return false;
	}
}