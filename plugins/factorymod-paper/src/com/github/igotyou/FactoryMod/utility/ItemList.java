/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.igotyou.FactoryMod.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.recipes.EnchantmentOptions;
import com.github.igotyou.FactoryMod.recipes.ProbabilisticEnchantment;

/**
 *
 * @author Brian Landry
 */
public class ItemList<E extends NamedItemStack> extends ArrayList<E> {
	
	private static final long serialVersionUID = -5975973806251278120L;
	
	public boolean exactlyIn(Inventory inventory)
	{
		//TODO: This is pretty broken too; won't handle split stacks right, a number of
		//     other edge cases; we've been very lucky so far. Basically only lucky b/c
		//     the "stack size" of input items is not divided, the requirement comes in as 
		//     a single "stack" of say 96 items, etc. so this works.
		//     So not exactly "broken" but not clean, either.
		boolean returnValue=true;
		//Checks that the ItemList ItemStacks are contained in the inventory
		for(ItemStack itemStack:this)
		{
			returnValue=returnValue&&(amountAvailable(inventory,itemStack)==itemStack.getAmount());
		}
		//Checks that inventory has not ItemStacks in addition to the ones in the itemList
		for(ItemStack invItemStack:inventory.getContents())
		{
			if(invItemStack!=null)
			{
				boolean itemPresent=false;
				for(ItemStack itemStack:this)
				{
					if(itemStack.isSimilar(invItemStack))
					{
						itemPresent=true;
					}
				}
				returnValue=returnValue&&itemPresent;
			}
		}
		return returnValue;
	}
	// TODO: Same issues exist here, I need to fix these.
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
			// TODO: Naive check. What about if you have 2 stacks of the same item,
			//    but only 1 in the chest? This test will return true for both stacks, 
			//    which is the wrong answer.
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
		ItemList<NamedItemStack> itemList=new ItemList<NamedItemStack>();
		for(NamedItemStack itemStack:this)
		{
			if(removeItemStack(inventory,itemStack))
			{
				itemList.add(itemStack.clone());
				break;
			}
		}
		return itemList;
	}
	// TODO: Same risks here; these need to be addressed. 
	public ItemList<NamedItemStack> getDifference(Inventory inventory)
	{
		ItemList<NamedItemStack> missingItems=new ItemList<NamedItemStack>();
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
		//TODO This is a very broken, so review:
		//   basically won't count correctly for repairs where a single-item repair cost is greater than a stack.
		int amountAvailable=0;
		for(ItemStack itemStack:this)
		{
			int currentAmountAvailable=amountAvailable(inventory,itemStack);
			amountAvailable=amountAvailable>currentAmountAvailable ? amountAvailable : currentAmountAvailable;
		}
		return amountAvailable;
	}
	
	public boolean putIn(Inventory inventory) {
		return putIn(inventory, new ArrayList<ProbabilisticEnchantment>(), EnchantmentOptions.DEFAULT);
	}
	
	public boolean putIn(Inventory inventory,List<ProbabilisticEnchantment> probabilisticEnchaments, EnchantmentOptions enchantmentOptions) {
		boolean putFailed = false;
		for(ItemStack itemStack:this) {
			// Terrifying hardcode, but I think sometimes itemStack.maxsize == 0, yikes!
			if (itemStack.getMaxStackSize() <= 0) {
				Bukkit.getLogger().warning("Item Stack has maxsize of 0, something is very wrong.");
			}
			int maxStackSize=(itemStack.getMaxStackSize() == 0 ? 64 : itemStack.getMaxStackSize());
			int amount=itemStack.getAmount();
			while(amount>maxStackSize) {
				ItemStack itemClone=itemStack.clone();
				Map<Enchantment,Integer> enchantments=getEnchantments(probabilisticEnchaments, enchantmentOptions);
				for(Enchantment enchantment:enchantments.keySet()) {
					if(enchantment.canEnchantItem(itemStack)) {
						itemClone.addUnsafeEnchantment(enchantment,enchantments.get(enchantment));
					}
				}
				itemClone.setAmount(maxStackSize);
				HashMap<Integer, ItemStack> leftover = inventory.addItem(itemClone);
				if (!leftover.isEmpty()) {
					putFailed = true;
				}
				amount-=maxStackSize;
			}
			ItemStack itemClone=itemStack.clone();
			Map<Enchantment,Integer> enchantments=getEnchantments(probabilisticEnchaments, enchantmentOptions);
			for(Enchantment enchantment:enchantments.keySet()) {
				if(enchantment.canEnchantItem(itemStack)) {
					itemClone.addUnsafeEnchantment(enchantment,enchantments.get(enchantment));
				}
			}
			itemClone.setAmount(amount);
			HashMap<Integer, ItemStack> leftover = inventory.addItem(itemClone);
			if (!leftover.isEmpty()) {
				putFailed = true;
			}
		}
		return !putFailed;
	}
	
	/**
	 * Attempts to pick a subset from available enchantments, using independent probabilities.
	 * If enchantment_options:ensure_one is set on the recipe, will pick one using 
	 * cumulative probabilities if the independent probability selection fails to pick any.
	 * if *that* fails, it picks one at random from the full set.
	 *  
	 * @param probabilisticEnchantments The set of applicable enchantments.
	 * @param enchantmentOptions The options for applying enchantments
	 * @return a set of enchantments to apply.
	 */
	public HashMap<Enchantment, Integer> getEnchantments(List<ProbabilisticEnchantment> probabilisticEnchantments, EnchantmentOptions enchantmentOptions)
	{
		HashMap<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
		Random rand = new Random();
		double sum = 0.0d;
		for(ProbabilisticEnchantment pe : probabilisticEnchantments) {
			sum += pe.getProbability(); 
			if(pe.getProbability()>=rand.nextDouble()) {
				// Logic fun: go ahead and add if safe only is false, otherwise check if safe.
				if (!enchantmentOptions.getSafeOnly() || checkSafe(enchantments.keySet(), pe.getEnchantment())) {
					enchantments.put(pe.getEnchantment(),pe.getLevel());
				}
			}
		}
		// Force at least one, try to pick fairly (based on cumulative distribution first)
		if (enchantmentOptions.getEnsureOne() && enchantments.size() == 0) {
			double which = rand.nextDouble() * sum;
			double sofar = 0.0d;
			for (ProbabilisticEnchantment pe : probabilisticEnchantments) {
				if (pe.getProbability() + sofar >= which) {
					enchantments.put(pe.getEnchantment(), pe.getLevel());
					break;
				} else {
					sofar += pe.getProbability();
				}
			}
			if (enchantments.size() == 0) { // someone forgot to give any probabilities?
				int i = rand.nextInt(probabilisticEnchantments.size());
				enchantments.put(probabilisticEnchantments.get(i).getEnchantment(),probabilisticEnchantments.get(i).getLevel());
			}
		}
		return enchantments;
	}
	
	/**
	 * Test function to ensure "safe" enchantment sets. Call before adding a new enchantment to a set
	 * of enchantments.
	 * 
	 * @param current The current safe set
	 * @param test The new enchantment to test
	 * @return True if this new enchantment does not conflict with any prior enchantments, false otherwise.
	 */
	private boolean checkSafe(Set<Enchantment> current, Enchantment test) {
		for (Enchantment ench : current) {
			if (test.conflictsWith(ench)) {
				return false;
			}
		}
		return true;
	}
	
	
	public String toString()
	{
		String returnString="";
		for(int i=0;i<size();i++)
		{
			String name=get(i).getItemMeta().hasDisplayName() ? get(i).getItemMeta().getDisplayName() : get(i).getCommonName();
			returnString+=String.valueOf(get(i).getAmount())+" "+name;
			if(i<size()-1)
			{
				returnString+=", ";
			}
		}
		return returnString;
	}
	//Returns the number of an ItemStack's MATERIAL that are available. 
	//  TODO: Does not return multiple as previously advertised.
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
					totalMaterial += currentItemStack.getAmount(); // not multiples
					//totalMaterial += (int) Math.floor((double)currentItemStack.getAmount() / (double)itemStack.getAmount()); // return the actual number of multiples ...
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
	/* TODO: int version is just wrong. Use double version. */
	public ItemList<NamedItemStack> getMultiple(int multiplier)
	{
		ItemList<NamedItemStack> multipliedItemList=new ItemList<NamedItemStack>();
		for (NamedItemStack itemStack:this)
		{
			NamedItemStack itemStackClone=itemStack.clone();
			itemStackClone.setAmount(itemStack.getAmount()*multiplier);
			multipliedItemList.add(itemStackClone);
		}
		return multipliedItemList;
	}
	public ItemList<NamedItemStack> getMultiple(double multiplier) 
	{
		ItemList<NamedItemStack> multipliedItemList=new ItemList<NamedItemStack>();
		for (NamedItemStack itemStack:this)
		{
			NamedItemStack itemStackClone=itemStack.clone();
			long newAmount = (long) Math.round(itemStackClone.getAmount()*multiplier);
			if (newAmount > 64)
			{
				for (;newAmount > 64; newAmount = newAmount-64)
				{
					NamedItemStack newItemStack = itemStack.clone();
					newItemStack.setAmount(64);
					multipliedItemList.add(newItemStack);
				}
			}
			itemStackClone.setAmount((int) newAmount);
			multipliedItemList.add(itemStackClone);
		}
		return multipliedItemList;
	}
	
	public boolean testPutIn(Inventory inventory) {
		return testPutIn(inventory, new ArrayList<ProbabilisticEnchantment>(), EnchantmentOptions.DEFAULT);
	}
	
	public boolean testPutIn(Inventory inventory, List<ProbabilisticEnchantment> probabilisticEnchantments,
			EnchantmentOptions enchantmentOptions) {
		if (this.isEmpty()) { // "fail" fast.
			return true;
		}
		
		// Bukkit API lacks a way to test adding stuff. That sucks.
		// Instead, we'll create a temporary "merged" inventory, and
		// see if we can add everything into it without overflowing
		Inventory merger = FactoryModPlugin.getPlugin().getServer().createInventory(null, inventory.getSize());
		for (ItemStack slot : inventory) {
			if (slot != null) {
				HashMap<Integer, ItemStack> k = merger.addItem(slot);
			
				if (!k.isEmpty()) {
					// big problem. Try to find out where; someone has an overloaded chest/inventory
					// that violates MC's stacking rules.
					Location loc = null;
					InventoryHolder held = inventory.getHolder();
					if (held instanceof DoubleChest) {
						loc = ((DoubleChest) held).getLocation();
					} else if (held instanceof BlockState) {
						loc = ((BlockState) held).getLocation();
					} else if (held instanceof Entity) {
						loc = ((Entity) held).getLocation();
					}
					Bukkit.getLogger().severe("Factory inventory cannot fit into itself, SEVERE at " + StringUtils.formatCoords(loc));
					return false;
				}
			}
		}
		return this.putIn(merger, probabilisticEnchantments, enchantmentOptions);
	}
}
