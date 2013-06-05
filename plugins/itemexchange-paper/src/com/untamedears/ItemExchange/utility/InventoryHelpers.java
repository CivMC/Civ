/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.untamedears.ItemExchange.utility;

import com.untamedears.ItemExchange.ItemExchangePlugin;
import com.untamedears.ItemExchange.utility.ExchangeRule.RuleType;
import com.untamedears.ItemExchange.exceptions.ExchangeRuleParseException;
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
	 * Returns an input following the ItemRules from the inventory
	 */
	public static List<ItemStack> getItemStacks(Inventory inventory,ExchangeRule itemRule)
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