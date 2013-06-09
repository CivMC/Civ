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
import org.bukkit.Material;
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
	public static ItemStack[] getItemStacks(Inventory inventory,ExchangeRule itemRule)
	{
		List<ItemStack> itemStacks=new ArrayList<>();
		//Gets the ItemStacks from the inventory to be transfered
		int requiredAmount=itemRule.getAmount();
		ItemStack[] contents=inventory.getContents();
		for(int i=0;i<contents.length && requiredAmount>0;i++)
		{
			ItemStack itemStack=contents[i];
			if(itemStack!=null&&itemRule.followsRules(itemStack))
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
		ItemStack[] itemStacksArray=itemStacks.toArray(new ItemStack[itemStacks.size()]);
		return itemStacksArray;
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
		ItemStack[] contents=inventory.getContents();
		ItemStack[] copy=new ItemStack[contents.length];
		for(int i=0;i<contents.length;i++){
			if(contents[i]!=null){
				ItemExchangePlugin.sendConsoleMessage(contents[i].toString());
				copy[i]=contents[i].clone();
			}
		}
		inventory.addItem(new ItemStack(Material.COOKED_BEEF));
		inventory.setContents(copy);
		return false;
	}
	/*
	 * Returns a deepCopy of an inventory.
	 */
	public static ItemStack[] deepCopy(Inventory inventory) {
		ItemStack[] deepCopy=inventory.getContents();
		for(int i=0;i<deepCopy.length;i++){
			if(deepCopy[i]!=null){
				deepCopy[i]=deepCopy[i].clone();
			}
		}
		return deepCopy;
	}
}