package vg.civcraft.mc.civmodcore.itemHandling;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import net.minecraft.server.v1_13_R2.NBTTagCompound;
import net.minecraft.server.v1_13_R2.NBTTagList;

/**
 * Allows the storage and comparison of itemstacks while ignoring their maximum possible stack sizes. This offers
 * various tools to compare inventories, to store recipe costs or to specify setupcosts. Take great care when dealing
 * with itemstacks with negative amounnts, while this implementation should be consistent even with negative values,
 * they create possibly unexpected results. For example an empty inventory/itemmap will seem to contain items when
 * compared to a map containing negative values. Additionally this implementation allows durability "wild cards", if you
 * specify -1 as durability it will count as any given durability. When working with multiple ItemMaps this will only
 * work if all methods are executed on the instance containing items with a durability of -1.
 */
public class ItemMap {

	private static final Logger log = Bukkit.getLogger();

	private HashMap<ItemStack, Integer> items;

	private int totalItems;

	/**
	 * Empty constructor to create empty item map
	 */
	public ItemMap() {
		items = new HashMap<>();
		totalItems = 0;
	}

	/**
	 * Constructor to create an item map based on the content of an inventory. The ItemMap will not be in sync with the
	 * inventory, it will only update if it's explicitly told to do so
	 *
	 * @param inv
	 *            Inventory to base the item map on
	 */
	public ItemMap(Inventory inv) {
		totalItems = 0;
		update(inv);
	}

	/**
	 * Constructor to create an ItemMap based on a single ItemStack
	 *
	 * @param is
	 *            ItemStack to start with
	 */
	public ItemMap(ItemStack is) {
		items = new HashMap<>();
		totalItems = 0;
		addItemStack(is);
	}

	/**
	 * Constructor to create an item map based on a collection of ItemStacks
	 *
	 * @param stacks
	 *            Stacks to add to the map
	 */
	public ItemMap(Collection<ItemStack> stacks) {
		items = new HashMap<>();
		addAll(stacks);
	}

	/**
	 * Clones the given itemstack, sets its amount to one and checks whether a stack equaling the created one exists in
	 * the item map. If yes the amount of the given stack (before the amount was set to 1) will be added to the current
	 * amount in the item map, if not a new entry in the map with the correct amount will be created
	 *
	 * @param input
	 *            ItemStack to insert
	 */
	public void addItemStack(ItemStack input) {
		if (input != null) {
			// log().info("Adding {0} as ItemStack", input.toString());
			ItemStack is = createMapConformCopy(input);
			// log().info("  Conform Copy: {0}", is.toString());
			if (is == null) {
				return;
			}
			Integer i;
			if ((i = items.get(is)) != null) {
				items.put(is, i + input.getAmount());
			} else {
				items.put(is, input.getAmount());
			}
			totalItems += input.getAmount();
		}
	}

	/**
	 * Removes the given ItemStack from this map. Only the amount of the given ItemStack will be removed, not all of
	 * them. If the amount of the given itemstack is bigger than the existing ones in this map, not more than the amount
	 * in this map will be removed
	 *
	 * @param input
	 *            ItemStack to remove
	 */
	public void removeItemStack(ItemStack input) {
		ItemStack is = createMapConformCopy(input);
		if (is == null) {
			return;
		}
		Integer value = items.get(is);
		if (value != null) {
			int newVal = value - input.getAmount();
			if (newVal > 0) {
				items.put(is, newVal);
			} else {
				items.remove(is);
			}
		}
	}

	/**
	 * Completly removes the given itemstack of this item map, completly independent of its amount
	 *
	 * @param input
	 *            ItemStack to remove
	 */
	public void removeItemStackCompletly(ItemStack input) {
		ItemStack is = createMapConformCopy(input);
		if (is != null) {
			items.remove(is);
		}
	}

	@Override
	public int hashCode() {
		int res = 0;
		for (Entry<ItemStack, Integer> entry : items.entrySet()) {
			res += entry.hashCode();
		}
		return res;
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
		items = new HashMap<>();
		totalItems = 0;
		for (int i = 0; i < inv.getSize(); i++) {
			ItemStack is = inv.getItem(i);
			if (is != null) {
				addItemStack(is);
			}
		}
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
		if (copy == null) {
			return;
		}
		copy.setAmount(amount);
		addItemStack(copy);
	}

	/**
	 * Gets a submap of this instance which contains all stacks with the same material as the given one and their
	 * respective amounts
	 *
	 * @param m
	 *            Material to search for
	 * @return New ItemMap with all ItemStack and their amount whose material matches the given one
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
	 * Gets a submap of this instance which contains all stacks with the same material and durability as the given one
	 * and their respective amounts
	 *
	 * @param m
	 *            Material to search for
	 * @param durability
	 *            Durability to search for
	 * @return New ItemMap with all ItemStack and their amount whose material and durability matches the given one
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
	 * Gets a submap of this instance which contains all stacks with the same material, durability and enchants as the
	 * given one and their respective amounts
	 *
	 * @param m
	 *            Material to search for
	 * @param durability
	 *            Durability to search for
	 * @param enchants
	 *            Enchants to search for
	 * @return New ItemMap with all ItemStack and their amount whose material, durability and enchants matches the given
	 *         one
	 */
	public ItemMap getStacksByMaterialDurabilityEnchants(Material m, int durability, Map<Enchantment, Integer> enchants) {
		ItemMap result = new ItemMap();
		for (ItemStack is : items.keySet()) {
			if (is.getType() == m && is.getDurability() == durability && is.getItemMeta() != null
					&& is.getItemMeta().getEnchants().equals(enchants)) {
				result.addItemAmount(is.clone(), items.get(is));
			}
		}
		return result;
	}

	public ItemMap getStacksByMaterialDurabilityEnchants(ItemStack is) {
		if (is.getItemMeta() != null) {
			return getStacksByMaterialDurabilityEnchants(is.getType(), is.getDurability(), is.getItemMeta()
					.getEnchants());
		} else {
			return getStacksByMaterialDurabilityEnchants(is.getType(), is.getDurability(),
					new HashMap<Enchantment, Integer>());
		}
	}

	/**
	 * Gets a submap of this instance which contains all stacks with the same lore as the given and their respective
	 * amount
	 *
	 * @param lore
	 *            Lore to search for
	 * @return New ItemMap with all ItemStacks and their amount whose lore matches the given one
	 */
	public ItemMap getStacksByLore(List<String> lore) {
		ItemMap result = new ItemMap();
		for (ItemStack is : items.keySet()) {
			if (is.getItemMeta() != null && is.getItemMeta().getLore().equals(lore)) {
				result.addItemAmount(is.clone(), items.get(is));
			}
		}
		return result;
	}

	/**
	 * Gets how many items of the given stack are in this map. Be aware that if a stack doesnt equal with the given one,
	 * for example because of mismatched NBT tags, it wont be included in the result
	 *
	 * @param is
	 *            Exact ItemStack to search for
	 * @return amount of items like the given stack in this map
	 */
	public int getAmount(ItemStack is) {
		ItemMap matSubMap = getStacksByMaterial(is);
		int amount = 0;
		for (Entry<ItemStack, Integer> entry : matSubMap.getEntrySet()) {
			ItemStack current = entry.getKey();
			if ((is.getDurability() == -1 || is.getDurability() == current.getDurability())
					&& is.getItemMeta().equals(current.getItemMeta())) {
				amount += entry.getValue();
			}
		}
		return amount;
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

	@SuppressWarnings("unchecked")
	public Set<Entry<ItemStack, Integer>> getEntrySet() {
		return ((HashMap<ItemStack, Integer>) items.clone()).entrySet();
	}

	/**
	 * Checks whether an inventory contains exactly what's described in this ItemMap
	 *
	 * @param i
	 *            Inventory to compare
	 * @return True if the inventory is identical with this instance, false if not
	 */
	public boolean containedExactlyIn(Inventory i) {
		ItemMap invMap = new ItemMap(i);
		for (Entry<ItemStack, Integer> entry : getEntrySet()) {
			if (!entry.getValue().equals(invMap.getAmount(entry.getKey()))) {
				return false;
			}
		}
		for (ItemStack is : i.getContents()) {
			if (is == null) {
				continue;
			}
			if (getStacksByMaterial(is).getTotalUniqueItemAmount() == 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks whether this instance is completly contained in the given inventory, which means every stack in this
	 * instance is also in the given inventory and the amount in the given inventory is either the same or bigger as in
	 * this instance
	 *
	 * @param i
	 *            inventory to check
	 * @return true if this instance is completly contained in the given inventory, false if not
	 */
	public boolean isContainedIn(Inventory i) {
		ItemMap invMap = new ItemMap(i);
		for (Entry<ItemStack, Integer> entry : getEntrySet()) {
			if (entry.getValue() > invMap.getAmount(entry.getKey())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		String res = "";
		for (ItemStack is : getItemStackRepresentation()) {
			res += is.toString() + ";";
		}
		return res;
	}

	/**
	 * Checks how often this ItemMap is contained in the given ItemMap or how often this ItemMap could be removed from
	 * the given one before creating negative stacks
	 *
	 * @param i
	 *            ItemMap to check
	 * @return How often this map is contained in the given one or Integer.MAX_VALUE if this instance is empty
	 */
	public int getMultiplesContainedIn(Inventory i) {
		ItemMap invMap = new ItemMap(i);
		int res = Integer.MAX_VALUE;
		for (Entry<ItemStack, Integer> entry : getEntrySet()) {
			int pulledAmount = invMap.getAmount(entry.getKey());
			int multiples = pulledAmount / entry.getValue();
			res = Math.min(res, multiples);
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
	 * Turns this item map into a list of ItemStacks, with amounts that do not surpass the maximum allowed stack size
	 * for each ItemStack
	 *
	 * @return List of stacksize conform ItemStacks
	 */
	public LinkedList<ItemStack> getItemStackRepresentation() {
		LinkedList<ItemStack> result = new LinkedList<>();
		for (Entry<ItemStack, Integer> entry : getEntrySet()) {
			ItemStack is = entry.getKey();
			Integer amount = entry.getValue();
			while (amount != 0) {
				ItemStack toAdd = is.clone();
				int addAmount = Math.min(amount, is.getMaxStackSize());
				toAdd.setAmount(addAmount);
				// log.info("Adding {0} as ItemStack", toAdd.toString());
				result.add(toAdd);
				amount -= addAmount;
			}
		}
		return result;
	}

	/**
	 * Clones this map
	 */
	@Override
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
	 * @return True if this ItemMap's item representation would completly fit in the inventory, false if not
	 */
	public boolean fitsIn(Inventory i) {
		int size;
		if (i instanceof PlayerInventory) {
			size = 36;
		} else {
			size = i.getSize();
		}
		ItemMap invCopy = new ItemMap();
		for (ItemStack is : i.getStorageContents()) {
			invCopy.addItemStack(is);
		}
		ItemMap instanceCopy = this.clone();
		instanceCopy.merge(invCopy);
		return instanceCopy.getItemStackRepresentation().size() <= size;
	}

	/**
	 * Instead of converting into many stacks of maximum size, this creates a stack with an amount of one for each entry
	 * and adds the total item amount and stack count as lore, which is needed to display larger ItemMaps in inventories
	 *
	 * @return UI representation of large ItemMap
	 */
	public List<ItemStack> getLoredItemCountRepresentation() {
		List<ItemStack> items = new LinkedList<>();
		for (Entry<ItemStack, Integer> entry : getEntrySet()) {
			ItemStack is = entry.getKey().clone();
			ISUtils.addLore(is, ChatColor.GOLD + "Total item count: " + entry.getValue());
			if (entry.getValue() > entry.getKey().getType().getMaxStackSize()) {
				int stacks = entry.getValue() / is.getType().getMaxStackSize();
				int extra = entry.getValue() % is.getType().getMaxStackSize();
				StringBuilder out = new StringBuilder(ChatColor.GOLD.toString());
				if (stacks != 0) {
					out.append(stacks + " stack" + (stacks == 1 ? "" : "s"));
				}
				if (extra != 0) {
					out.append(" and " + extra);
					out.append(" item" + (extra == 1 ? "" : "s"));
				}
				ISUtils.addLore(is, out.toString());
			}
			items.add(is);
		}
		return items;
	}

	/**
	 * Attempts to remove the content of this ItemMap from the given inventory. If it fails to find all the required
	 * items it will stop and return false
	 *
	 * @param i
	 *            Inventory to remove from
	 * @return True if everything was successfully removed, false if not
	 */
	public boolean removeSafelyFrom(Inventory i) {
		for (Entry<ItemStack, Integer> entry : getEntrySet()) {
			int amountToRemove = entry.getValue();
			ItemStack is = entry.getKey();
			for (ItemStack inventoryStack : i.getContents()) {
				if (inventoryStack == null) {
					continue;
				}
				if (inventoryStack.getType() == is.getType()) {
					ItemMap compareMap = new ItemMap(inventoryStack);
					int removeAmount = Math.min(amountToRemove, compareMap.getAmount(is));
					if (removeAmount != 0) {
						ItemStack cloneStack = inventoryStack.clone();
						cloneStack.setAmount(removeAmount);
						if (i.removeItem(cloneStack).values().size() != 0) {
							return false;
						} else {
							amountToRemove -= removeAmount;
							if (amountToRemove <= 0) {
								break;
							}
						}
					}
				}
			}
			if (amountToRemove > 0) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ItemMap) {
			ItemMap im = (ItemMap) o;
			if (im.getTotalItemAmount() == getTotalItemAmount()) {
				return im.getEntrySet().equals(getEntrySet());
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
		net.minecraft.server.v1_13_R2.ItemStack s = CraftItemStack.asNMSCopy(copy);
		if (s == null) {
			log.info("Attempted to create map conform copy of " + copy.toString()
					+ ", but couldn't because this item can't be held in inventories since Minecraft 1.8");
			return null;
		}
		s.setRepairCost(0);
		copy = CraftItemStack.asBukkitCopy(s);
		return copy;
	}

	/**
	 * Utility to add NBT tags to an item and produce a custom stack size
	 *
	 * @param is
	 *            Template Bukkit ItemStack
	 * @param amt
	 *            Output Stack Size
	 * @param map
	 *            Java Maps and Lists representing NBT data
	 * @return Cloned ItemStack with amount set to amt and NBT set to map.
	 */
	public static ItemStack enrichWithNBT(ItemStack is, int amt, Map<String, Object> map) {
		log.info("Received request to enrich " + is.toString());
		ItemStack copy = is.clone();
		amt = (amt < 1 ? 1 : amt > is.getMaxStackSize() ? is.getMaxStackSize() : amt);
		copy.setAmount(amt);
		net.minecraft.server.v1_13_R2.ItemStack s = CraftItemStack.asNMSCopy(copy);
		if (s == null) {
			log.severe("Failed to create enriched copy of " + copy.toString());
			return null;
		}

		NBTTagCompound nbt = s.getTag();
		if (nbt == null) {
			nbt = new NBTTagCompound();
		}

		TagManager.mapToNBT(nbt, map);
		s.setTag(nbt);
		copy = CraftItemStack.asBukkitCopy(s);
		return copy;
	}

	public static NBTTagCompound mapToNBT(NBTTagCompound base, Map<String, Object> map) {
		return TagManager.mapToNBT(base, map);
	}

	public static NBTTagList listToNBT(NBTTagList base, List<Object> list) {
		return TagManager.listToNBT(base, list);
	}
}
