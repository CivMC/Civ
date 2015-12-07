package com.github.igotyou.FactoryMod.utility;

import java.util.HashMap;
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
	 * The ItemMap will be in sync with the inventory, it will only update if
	 * it's explicitly told to do so
	 * 
	 * @param inv
	 *            Inventory to base the item map on
	 */
	public ItemMap(Inventory inv) {
		update(inv);
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
	 * Utility method, which has the amount of items to add as parameter. This
	 * method doesnt clone the ItemStack, so dont use this on ItemStacks which
	 * exist in the world
	 * 
	 * @param input
	 *            ItemStack to sort into the map
	 * @param amount
	 *            Amount associated with the given ItemStack
	 */
	public void addItemAmount(ItemStack input, int amount) {
		input.setAmount(amount);
		addItemStack(input);
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
	 * Utility to not mess with stacks directly taken from inventories
	 * 
	 * @param is
	 *            Template ItemStack
	 * @return Cloned ItemStack with its amount set to 1
	 */
	private ItemStack createMapConformCopy(ItemStack is) {
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
