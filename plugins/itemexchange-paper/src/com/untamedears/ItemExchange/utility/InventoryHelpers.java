/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.untamedears.ItemExchange.utility;

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
	public static ItemStack[] getItemStacks(Inventory inventory, ExchangeRule itemRule) {
		List<ItemStack> itemStacks = new ArrayList<ItemStack>();
		//Gets the ItemStacks from the inventory to be transfered
		int requiredAmount = itemRule.getAmount();
		ItemStack[] contents = inventory.getContents();
		for (int i = 0; i < contents.length && requiredAmount > 0; i++) {
			ItemStack itemStack = contents[i];
			if (itemStack != null && itemRule.followsRules(itemStack)) {
				if (itemStack.getAmount() <= requiredAmount) {
					itemStacks.add(itemStack);
					
					requiredAmount -= itemStack.getAmount();
				}
				else {
					ItemStack itemStackClone = itemStack.clone();
					itemStackClone.setAmount(requiredAmount);
					itemStacks.add(itemStackClone);
					
					requiredAmount = 0;
				}
			}
		}
		ItemStack[] itemStacksArray = itemStacks.toArray(new ItemStack[itemStacks.size()]);
		return itemStacksArray;
	}

	/*
	 * Gets the amount of the ItemStack in the inventory
	 */
	public static int amountIn(Inventory inventory, ItemStack itemStack) {
		int amount = 0;
		for (ItemStack invItemStack : inventory.all(itemStack).values()) {
			amount += invItemStack.getAmount();
		}
		return amount;
	}

	/*
	 * Returns a deepCopy of an inventory, which creates new ItemStack objects.
	 */
	public static ItemStack[] deepCopy(Inventory inventory) {
		ItemStack[] deepCopy = inventory.getContents();
		for (int i = 0; i < deepCopy.length; i++) {
			if (deepCopy[i] != null) {
				deepCopy[i] = deepCopy[i].clone();
			}
		}
		return deepCopy;
	}
}
