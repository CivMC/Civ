package com.github.igotyou.FactoryMod.utility;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ItemMap {
	private HashMap<ItemStack, Integer> items;
	private int totalItems;

	/**
	 * Empty constructor to create empty item map
	 */
	public ItemMap() {
		items = new HashMap<ItemStack, Integer>();
		totalItems = 0;
	}

	/**
	 * Constructor to create an item map based on the content of an inventory.
	 * The ItemMap will not be in sync with the inventory, it will only update
	 * if it's explicitly told to do so
	 * 
	 * @param inv
	 *            Inventory to base the item map on
	 */
	public ItemMap(Inventory inv) {
		update(inv);
	}

	/**
	 * Constructor to create an ItemMap based on a single ItemStack
	 * 
	 * @param is
	 *            ItemStack to start with
	 */
	public ItemMap(ItemStack is) {
		addItemStack(is);
	}

	/**
	 * Constructor to create an item map based on a collection of ItemStacks
	 * 
	 * @param stacks
	 *            Stacks to add to the map
	 */
	public ItemMap(Collection<ItemStack> stacks) {
		addAll(stacks);
	}

	/**
	 * Clones the given itemstack, sets its amount to one and checks whether a
	 * stack equaling the created one exists in the item map. If yes the amount
	 * of the given stack (before the amount was set to 1) will be added to the
	 * current amount in the item map, if not a new entry in the map with the
	 * correct amount will be created
	 * 
	 * @param input
	 *            ItemStack to insert
	 */
	public void addItemStack(ItemStack input) {
		ItemStack is = createMapConformCopy(input);
		Integer i;
		if ((i = items.get(is)) != null) {
			items.put(is, i + input.getAmount());
		} else {
			items.put(is, input.getAmount());
		}
		totalItems += input.getAmount();
	}

	/**
	 * Removes the given ItemStack from this map. Only the amount of the given
	 * ItemStack will be removed, not all of them. Use the safe parameter to
	 * specify whether stacks with negative amounts as a result in the map
	 * should be allowed or not.
	 * 
	 * @param input
	 *            ItemStack to remove
	 * @param safe
	 *            true to forbid stacks with negative amounts, false to allow
	 *            them
	 */
	public void removeItemStack(ItemStack input, boolean safe) {
		ItemStack is = createMapConformCopy(input);
		Integer i;
		if ((i = items.get(is)) != null) {
			int newSum = i - input.getAmount();
			if ((safe && newSum > 0) || !safe) {
				items.put(is, i - input.getAmount());
				totalItems -= input.getAmount();
			}
		} else {
			if (!safe) {
				items.put(is, -1 * input.getAmount());
				totalItems -= input.getAmount();
			}
		}
	}

	/**
	 * Removes all the given ItemStacks from this map. Only the amount of the
	 * given ItemStack will be removed, not the complete instance from the map.
	 * Use the safe parameter to specify whether stacks with negative amounts as
	 * a result in the map should be allowed or not.
	 * 
	 * @param stacks
	 *            ItemStacks to remove
	 * @param safe
	 *            true to forbid stacks with negative amounts, false to allow
	 *            them
	 */
	public void removeAll(Collection<ItemStack> stacks, boolean safe) {
		for (ItemStack stack : stacks) {
			removeItemStack(stack, safe);
		}
	}

	/**
	 * Adds all the stacks given in the collection to this map
	 * 
	 * @param stacks
	 *            Stacks to add
	 */
	public void addAll(Collection<ItemStack> stacks) {
		for (ItemStack is : stacks) {
			if (is != null) {
				addItemStack(is);
			}
		}
	}

	/**
	 * Merges the given item map into this instance
	 * 
	 * @param im
	 *            ItemMap to merge
	 */
	public void merge(ItemMap im) {
		for (Entry<ItemStack, Integer> entry : im.getEntrySet()) {
			addItemAmount(entry.getKey(), entry.getValue());
		}
	}

	public void update(Inventory inv) {
		items = new HashMap<ItemStack, Integer>();
		totalItems = 0;
		for (int i = 0; i < inv.getSize(); i++) {
			ItemStack is = inv.getItem(i);
			if (is != null) {
				addItemStack(is);
			}
		}
	}

	/**
	 * Merges an existing item map into this instance
	 * 
	 * @param im
	 *            Map to merge
	 */
	public void addItemMap(ItemMap im) {
		addEntrySet(im.getEntrySet());
	}

	public void addEntrySet(Set<Entry<ItemStack, Integer>> entries) {
		for (Entry<ItemStack, Integer> entry : entries) {
			addItemAmount(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Utility method, which has the amount of items to add as parameter.
	 * 
	 * @param input
	 *            ItemStack to sort into the map
	 * @param amount
	 *            Amount associated with the given ItemStack
	 */
	public void addItemAmount(ItemStack input, int amount) {
		ItemStack copy = createMapConformCopy(input);
		copy.setAmount(amount);
		addItemStack(copy);
	}

	/**
	 * Gets a submap of this instance which contains all stacks with the same
	 * material as the given one and their respective amounts
	 * 
	 * @param m
	 *            Material to search for
	 * @return New ItemMap with all ItemStack and their amount whose material
	 *         matches the given one
	 */
	public ItemMap getStacksByMaterial(Material m) {
		ItemMap result = new ItemMap();
		for (ItemStack is : items.keySet()) {
			if (is.getType() == m) {
				result.addItemAmount(is.clone(), items.get(is));
			}
		}
		return result;
	}

	public ItemMap getStacksByMaterial(ItemStack is) {
		return getStacksByMaterial(is.getType());
	}

	/**
	 * Gets a submap of this instance which contains all stacks with the same
	 * material and durability as the given one and their respective amounts
	 * 
	 * @param m
	 *            Material to search for
	 * @param durability
	 *            Durability to search for
	 * @return New ItemMap with all ItemStack and their amount whose material
	 *         and durability matches the given one
	 */
	public ItemMap getStacksByMaterialDurability(Material m, int durability) {
		ItemMap result = new ItemMap();
		for (ItemStack is : items.keySet()) {
			if (is.getType() == m && is.getDurability() == durability) {
				result.addItemAmount(is.clone(), items.get(is));
			}
		}
		return result;
	}

	public ItemMap getStacksByMaterialDurability(ItemStack is) {
		return getStacksByMaterialDurability(is.getType(), is.getDurability());
	}

	/**
	 * Gets a submap of this instance which contains all stacks with the same
	 * material, durability and enchants as the given one and their respective
	 * amounts
	 * 
	 * @param m
	 *            Material to search for
	 * @param durability
	 *            Durability to search for
	 * @param enchants
	 *            Enchants to search for
	 * @return New ItemMap with all ItemStack and their amount whose material,
	 *         durability and enchants matches the given one
	 */
	public ItemMap getStacksByMaterialDurabilityEnchants(Material m,
			int durability, Map<Enchantment, Integer> enchants) {
		ItemMap result = new ItemMap();
		for (ItemStack is : items.keySet()) {
			if (is.getType() == m && is.getDurability() == durability
					&& is.getItemMeta() != null
					&& is.getItemMeta().getEnchants().equals(enchants)) {
				result.addItemAmount(is.clone(), items.get(is));
			}
		}
		return result;
	}

	public ItemMap getStacksByMaterialDurabilityEnchants(ItemStack is) {
		if (is.getItemMeta() != null) {
			return getStacksByMaterialDurabilityEnchants(is.getType(),
					(int) is.getDurability(), is.getItemMeta().getEnchants());
		} else {
			return getStacksByMaterialDurabilityEnchants(is.getType(),
					(int) is.getDurability(),
					new HashMap<Enchantment, Integer>());
		}
	}

	/**
	 * Gets a submap of this instance which contains all stacks with the same
	 * lore as the given and their respective amount
	 * 
	 * @param lore
	 *            Lore to search for
	 * @return New ItemMap with all ItemStacks and their amount whose lore
	 *         matches the given one
	 */
	public ItemMap getStacksByLore(List<String> lore) {
		ItemMap result = new ItemMap();
		for (ItemStack is : items.keySet()) {
			if (is.getItemMeta() != null
					&& is.getItemMeta().getLore().equals(lore)) {
				result.addItemAmount(is.clone(), items.get(is));
			}
		}
		return result;
	}

	/**
	 * Gets how many items of the given stack are in this map. Be aware that if
	 * a stack doesnt equal with the given one, for example because of
	 * mismatched NBT tags, it wont be included in the result
	 * 
	 * @param is
	 *            Exact ItemStack to search for
	 * @return amount of items like the given stack in this map
	 */
	public Integer getAmount(ItemStack is) {
		return items.get(createMapConformCopy(is));
	}

	/**
	 * @return How many items are stored in this map total
	 */
	public int getTotalItemAmount() {
		return totalItems;
	}

	/**
	 * @return How many unique items are stored in this map
	 */
	public int getTotalUniqueItemAmount() {
		return items.keySet().size();
	}

	public Set<Entry<ItemStack, Integer>> getEntrySet() {
		return ((HashMap<ItemStack, Integer>) items.clone()).entrySet();
	}

	/**
	 * Compares this ItemMap to a given one and returns a map containing the
	 * exact differences. For example if this instance has 5 stone and the given
	 * ItemMap has 10 stone, then the result will contain 5 stone. If instance
	 * and argument were switched around, the result would contain -5 stone,
	 * which expresses that the given ItemMap has 5 stone less than this
	 * instance. If an amount is identical it wont be added to the diff map
	 * 
	 * @param im
	 *            ItemMap to compare to
	 * @return ItemMap containing the exact differences between this instance
	 *         and a given map
	 */
	public ItemMap getDifference(ItemMap im) {
		ItemMap result = new ItemMap();
		Set<Entry<ItemStack, Integer>> firstSet = getEntrySet();
		Set<Entry<ItemStack, Integer>> secondSet = im.getEntrySet();
		for (Entry<ItemStack, Integer> entry : firstSet) {
			Integer pulled = im.getAmount(entry.getKey());
			if (pulled != null) {
				if ((pulled - entry.getValue()) != 0) {
					result.addItemAmount(entry.getKey(),
							pulled - entry.getValue());
				}
				clearEntrySetFromStack(secondSet, entry.getKey());
			} else {
				result.addItemAmount(entry.getKey(), entry.getValue() * -1);
			}
			clearEntrySetFromStack(firstSet, entry.getKey());

		}
		result.addEntrySet(secondSet);
		return result;
	}

	/**
	 * Checks whether an inventory contains exactly what's described in this
	 * ItemMap
	 * 
	 * @param i
	 *            Inventory to compare
	 * @return True if the inventory is identical with this instance, false if
	 *         not
	 */
	public boolean containedExactlyIn(Inventory i) {
		return getDifference(new ItemMap(i)).getTotalItemAmount() == 0;
	}

	/**
	 * Checks whether this instance is completly contained in the given ItemMap,
	 * which means every stack in this instance is also in the given map and the
	 * amount in the given map is either the same or bigger as in this instance
	 * 
	 * @param im
	 *            ItemStack to check
	 * @return true if this instance is completly contained in the given stack,
	 *         false if not
	 */
	public boolean isContainedIn(ItemMap im) {
		for (Entry<ItemStack, Integer> entry : getDifference(im).getEntrySet()) {
			if (entry.getValue() < 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks how often this ItemMap is contained in the given ItemMap or how
	 * often this ItemMap could be removed from the given one before creating
	 * negative stacks
	 * 
	 * @param im
	 *            ItemMap to check
	 * @return How often this map is contained in the given one or
	 *         Integer.MAX_VALUE if this instance is empty
	 */
	public int getMultiplesContainedIn(ItemMap im) {
		int res = Integer.MAX_VALUE;
		for (Entry<ItemStack, Integer> entry : getEntrySet()) {
			int pulledAmount = im.getAmount(entry.getKey()) != null ? im
					.getAmount(entry.getKey()) : 0;
			res = Math.min(res, pulledAmount);
		}
		return res;
	}

	/**
	 * Multiplies the whole content of this instance by the given multiplier
	 * 
	 * @param multiplier
	 *            Multiplier to scale the amount of the contained items with
	 */
	public void multiplyContent(double multiplier) {
		totalItems = 0;
		for (Entry<ItemStack, Integer> entry : getEntrySet()) {
			items.put(entry.getKey(), (int) (entry.getValue() * multiplier));
			totalItems += (int) (entry.getValue() * multiplier);
		}
	}

	/**
	 * Checks whether this instance completly contains the given ItemMap, which
	 * means every stack in the given map also exists in this instance and the
	 * amount in the given map is either the same or smaller compared to the one
	 * in this instance
	 * 
	 * @param im
	 *            ItemStack to check
	 * @return true if this instance completly contains the given ItemStack,
	 *         false if not
	 */
	public boolean contains(ItemMap im) {
		return im.isContainedIn(this);
	}

	/**
	 * Turns this item map into a list of ItemStacks, with amounts that do not
	 * surpass the maximum allowed stack size for each ItemStack
	 * 
	 * @return List of stacksize conform ItemStacks
	 */
	public LinkedList<ItemStack> getItemStackRepresentation() {
		LinkedList<ItemStack> result = new LinkedList<ItemStack>();
		for (Entry<ItemStack, Integer> entry : getEntrySet()) {
			ItemStack is = entry.getKey();
			Integer amount = entry.getValue();
			while (amount != 0) {
				ItemStack toAdd = is.clone();
				int addAmount = Math.min(amount, is.getMaxStackSize());
				toAdd.setAmount(addAmount);
				result.add(toAdd);
				amount -= addAmount;
			}
		}
		return result;
	}

	/**
	 * Clones this map
	 */
	public ItemMap clone() {
		ItemMap clone = new ItemMap();
		for (Entry<ItemStack, Integer> entry : getEntrySet()) {
			clone.addItemAmount(entry.getKey(), entry.getValue());
		}
		return clone;
	}

	/**
	 * Checks whether this instance would completly fit into the given inventory
	 * 
	 * @param i
	 *            Inventory to check
	 * @return True if this ItemMap's item representation would completly fit in
	 *         the inventory, false if not
	 */
	public boolean fitsIn(Inventory i) {
		ItemMap invCopy = new ItemMap(i);
		ItemMap instanceCopy = this.clone();
		instanceCopy.merge(invCopy);
		return instanceCopy.getItemStackRepresentation().size() <= i.getSize();
	}

	public boolean equals(Object o) {
		if (o instanceof ItemMap) {
			ItemMap im = (ItemMap) o;
			if (im.getTotalItemAmount() == getTotalItemAmount()) {
				ItemMap diff = im.getDifference(this);
				if (diff.getEntrySet().size() == 0) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Utility to not mess with stacks directly taken from inventories
	 * 
	 * @param is
	 *            Template ItemStack
	 * @return Cloned ItemStack with its amount set to 1
	 */
	private static ItemStack createMapConformCopy(ItemStack is) {
		ItemStack copy = is.clone();
		copy.setAmount(1);
		return copy;
	}

	private Set<Entry<ItemStack, Integer>> clearEntrySetFromStack(
			Set<Entry<ItemStack, Integer>> set, ItemStack is) {
		for (Entry<ItemStack, Integer> entry : set) {
			if (entry.getKey().equals(is)) {
				set.remove(entry);
			}
		}
		return set;
	}
}
