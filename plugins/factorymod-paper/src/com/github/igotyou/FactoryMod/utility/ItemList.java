/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.igotyou.FactoryMod.utility;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Brian Landry
 */
public class ItemList<E extends NamedItemStack> extends ArrayList<E> {
	public boolean exactlyIn(Inventory inventory)
	{
		boolean returnValue=true;
		for(ItemStack itemStack:this)
		{
			returnValue=returnValue&&(amountAvailable(inventory,itemStack)==itemStack.getAmount());
		}
		return returnValue;
	}
	public boolean oneIn(Inventory inventory)
	{
		if(this.isEmpty())
		{
			return true;
		}
		else
		{
			for(ItemStack itemStack:this)
			{
				if (amountAvailable(inventory, itemStack)>=itemStack.getAmount())
				{
					return true;
				}
			}
			return false;
		}
	}
	public boolean allIn(Inventory inventory)
	{
		for(ItemStack itemStack:this)
		{
			if (amountAvailable(inventory, itemStack)<itemStack.getAmount())
			{
				return false;
			}
		}
		return true;
	}

	public boolean removeFrom(Inventory inventory)
	{
		boolean returnValue=true;
		if(allIn(inventory))
		{
			for(ItemStack itemStack:this)
			{
				returnValue=returnValue&&removeItemStack(inventory,itemStack);
			}
		}
		else
		{
			returnValue=false;
		}
		return returnValue;
	}
	public int removeMaxFrom(Inventory inventory,int maxAmount)
	{
		int amountRemoved=0;
		while(size()!=0&&allIn(inventory)&&amountRemoved<=maxAmount)
		{
			if(removeFrom(inventory))
			{
				amountRemoved++;
			}
		}
		return amountRemoved;
	}
	public ItemList<NamedItemStack> removeOneFrom(Inventory inventory)
	{
		ItemList<NamedItemStack> itemList=new ItemList<>();
		for(NamedItemStack itemStack:this)
		{
			if(removeItemStack(inventory,itemStack))
			{
				itemList.add(itemStack);
				break;
			}
		}
		return itemList;
	}
	public ItemList<NamedItemStack> getDifference(Inventory inventory)
	{
		ItemList<NamedItemStack> missingItems=new ItemList<>();
		for(NamedItemStack itemStack:this)
		{
			int difference=itemStack.getAmount()-amountAvailable(inventory, itemStack);
			if (difference>0)
			{
				NamedItemStack clonedItemStack=itemStack.clone();
				clonedItemStack.setAmount(difference);
				missingItems.add(clonedItemStack);
			}
		}
		return missingItems;
	}
	public int amountAvailable(Inventory inventory)
	{
		int amountAvailable=0;
		for(ItemStack itemStack:this)
		{
			int currentAmountAvailable=amountAvailable(inventory,itemStack);
			amountAvailable=amountAvailable>currentAmountAvailable ? amountAvailable : currentAmountAvailable;
		}
		return amountAvailable;
	}
	public void putIn(Inventory inventory)
	{
		for(ItemStack itemStack:this)
		{
			int maxStackSize=itemStack.getMaxStackSize();
			int amount=itemStack.getAmount();
			while(amount>maxStackSize)
			{
				itemStack.setAmount(maxStackSize);
				inventory.addItem(itemStack);
				amount-=maxStackSize;
			}
			itemStack.setAmount(amount);
			inventory.addItem(itemStack);
		}
	}
	public ItemList<NamedItemStack> addEnchantments(Map<Enchantment,Integer> enchantments)
	{
		ItemList<NamedItemStack> clonedItemList=(ItemList<NamedItemStack>) this.clone();
		for(ItemStack itemStack:this)
		{
			itemStack.addEnchantments(enchantments);
		}
		return clonedItemList;
	}
	public String toString()
	{
		String returnString="";
		for(int i=0;i<size();i++)
		{
			String name=get(i).getItemMeta().hasDisplayName() ? get(i).getItemMeta().getDisplayName() : get(i).commonName;
			returnString+=String.valueOf(get(i).getAmount())+" "+name;
			if(i<size()-1)
			{
				returnString+=", ";
			}
		}
		return returnString;
	}
	//Returns the number of multiples of an ItemStack that are availible
	private int amountAvailable(Inventory inventory, ItemStack itemStack)
	{
		int totalMaterial = 0;
		for(ItemStack currentItemStack:inventory)
		{
			if(currentItemStack!=null)
			{
				/*For some reason I can't fathom the orientaion of the comparison
				 * of the two ItemStacks in the following statement matters.
				 * It likely has to do with the fact that itemStack is a NamedItemStack
				 * but I don't see why this should change its behavior...
				*/
				if (itemStack.isSimilar(currentItemStack) ||
					(itemStack.getType() == Material.NETHER_WARTS && currentItemStack.getType() == Material.NETHER_WARTS))
				{		
					totalMaterial += currentItemStack.getAmount();
				}
			}
		}
		return totalMaterial;
	}
	//Removes an itemstacks worth of material from an inventory
	private boolean removeItemStack(Inventory inventory,ItemStack itemStack)
	{		
		int materialsToRemove = itemStack.getAmount();
		ListIterator<ItemStack> iterator = inventory.iterator();
		while(iterator.hasNext())
		{
			ItemStack currentItemStack = iterator.next();
			if (itemStack.isSimilar(currentItemStack))
			{
				if (materialsToRemove <= 0)
				{
					break;
				}
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
		return materialsToRemove == 0;
	}
}
