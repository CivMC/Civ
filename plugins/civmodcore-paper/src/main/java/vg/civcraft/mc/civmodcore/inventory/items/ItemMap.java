package vg.civcraft.mc.civmodcore.inventory.items;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import org.apache.commons.collections4.CollectionUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Repairable;
import vg.civcraft.mc.civmodcore.nbt.NBTSerialization;
import vg.civcraft.mc.civmodcore.utilities.CivLogger;
import vg.civcraft.mc.civmodcore.utilities.MoreMath;

/**
 * Allows the storage and comparison of item stacks while ignoring their maximum possible stack sizes. This offers
 * various tools to compare inventories, to store recipe costs or to specify setup costs. Take great care when dealing
 * with item stacks with negative amounts, while this implementation should be consistent even with negative values,
 * they create possibly unexpected results. For example an empty inventory/item map will seem to contain items when
 * compared to a map containing negative values. Additionally this implementation allows durability "wild cards", if
 * you specify -1 as durability it will count as any given durability. When working with multiple ItemMaps this will
 * only work if all methods are executed on the instance containing items with a durability of -1.
 *
 * TODO: ItemMap is troubling because it manipulates and searches for items in ways that are not friendly to modern
 *       ways of cataloging items. For example, Bastion materials are all custom items, but they're only custom in the
 *       sense that they are named and lored.. but an Energizer will match with any other emerald unless you use any of
 *       the weirdly specific methods to weed out custom items. I think it would be better to refactor ItemMap to
 *       specifically support custom items, but I definitely need help in that regard. Or we can just keep it around
 *       for a little longer since they work fine enough and aren't in critical updates.
 */
public class ItemMap {

	private static final CivLogger LOGGER = CivLogger.getLogger(ItemMap.class);

	private final Object2IntMap<ItemStack> items;
	private int totalItems;

	/**
	 * Empty constructor to create empty item map
	 */
	public ItemMap() {
		this.items = new Object2IntOpenHashMap<>(0);
		this.items.defaultReturnValue(0);
		this.totalItems = 0;
	}

	/**
	 * Constructor to create an ItemMap based on a single ItemStack
	 *
	 * @param item ItemStack to start with
	 */
	public ItemMap(final ItemStack item) {
		this.items = new Object2IntOpenHashMap<>(1);
		this.items.defaultReturnValue(0);
		this.totalItems = 0;
		addItemStack(item);
	}

	/**
	 * Constructor to create an item map based on a collection of ItemStacks
	 *
	 * @param stacks Stacks to add to the map
	 */
	public ItemMap(final Collection<ItemStack> stacks) {
		this.items = new Object2IntOpenHashMap<>(stacks.size());
		this.items.defaultReturnValue(0);
		addAll(stacks);
	}

	/**
	 * Constructor to create an item map based on the content of an inventory. The ItemMap will not be in sync with the
	 * inventory, it will only update if it's explicitly told to do so.
	 *
	 * @param inventory Inventory to base the item map on
	 */
	public ItemMap(final Inventory inventory) {
		this.items = new Object2IntOpenHashMap<>(inventory.getSize());
		this.items.defaultReturnValue(0);
		this.totalItems = 0;
		update(inventory);
	}

	private static ItemStack INTERNAL_createKey(ItemStack item) {
		item = item.asOne(); // this also clones the stack
		ItemUtils.handleItemMeta(item, (Repairable meta) -> {
			meta.setRepairCost(0);
			return true;
		});
		return item;
	}




	//getAmount
	//getTotalItemAmount
	//getTotalUniqueItemAmount
	//getStacksByMaterial
	//getStacksByMaterialEnchant
	//getStacksByLore
	//getEntrySet
	//getItemStackRepresentation
	//getLoredItemCountRepresentation

	//addAll
	//addItemStack
	//addItemAmount
	//addToInventory
	//addToEntrySet

	//removeItemStack
	//removeItemStackCompletely
	//removeSafelyFrom

	//fitsIn
	//isContainedIn
	//containedExactlyIn
	//getMultiplesContainedIn
	//multiplyContent

	//createMapConformCopy
	//enrichWithNBT
	//clone
	//merge
	//update




	/**
	 * Clones the given item stack, sets its amount to one and checks whether a stack equaling the created one exists
	 * in the item map. If yes the amount of the given stack (before the amount was set to 1) will be added to the
	 * current amount in the item map, if not a new entry in the map with the correct amount will be created.
	 *
	 * @param input ItemStack to insert
	 */
	public void addItemStack(final ItemStack input) {
		if (!ItemUtils.isValidItemIgnoringAmount(input)) {
			return;
		}
		this.items.computeInt(INTERNAL_createKey(input), (key, amount) ->
				amount == null ? input.getAmount() : amount + input.getAmount());
		this.totalItems += input.getAmount();
	}

	/**
	 * Adds all the items contained in this instance to the given inventory
	 *
	 * @param inventory Inventory to add items to
	 */
	public void addToInventory(Inventory inventory) {
		for (ItemStack is : getItemStackRepresentation()) {
			inventory.addItem(is);
		}
	}

	/**
	 * Removes the given ItemStack from this map. Only the amount of the given ItemStack will be removed, not all of
	 * them. If the amount of the given item stack is bigger than the existing ones in this map, not more than the
	 * amount in this map will be removed
	 *
	 * @param input ItemStack to remove
	 */
	public void removeItemStack(final ItemStack input) {
		if (input.getAmount() <= 0) {
			return;
		}
		final ItemStack key = INTERNAL_createKey(input);
		if (key == null) {
			return;
		}
		this.items.computeIntIfPresent(key, (_key, amount) -> (amount -= input.getAmount()) <= 0 ? null : amount);
	}

	/**
	 * Completely removes the given item stack of this item map, completely independent of its amount.
	 *
	 * @param input ItemStack to remove
	 */
	public void removeItemStackCompletely(final ItemStack input) {
		final ItemStack key = INTERNAL_createKey(input);
		if (key != null) {
			this.items.removeInt(key);
		}
	}

	@Override
	public int hashCode() {
		return this.items.hashCode();
	}

	/**
	 * Adds all the stacks given in the collection to this map
	 *
	 * @param stacks Stacks to add
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
	 * @param im ItemMap to merge
	 */
	public void merge(ItemMap im) {
		for (Entry<ItemStack, Integer> entry : im.getEntrySet()) {
			addItemAmount(entry.getKey(), entry.getValue());
		}
	}

	public void update(final Inventory inventory) {
		this.items.clear();
		this.totalItems = 0;
		for (int i = 0; i < inventory.getSize(); i++) {
			final ItemStack item = inventory.getItem(i);
			if (item != null) {
				addItemStack(item);
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
	 * @param input  ItemStack to sort into the map
	 * @param amount Amount associated with the given ItemStack
	 */
	public void addItemAmount(ItemStack input, int amount) {
		ItemStack copy = INTERNAL_createKey(input);
		if (copy == null) {
			return;
		}
		copy.setAmount(amount);
		addItemStack(copy);
	}

	/**
	 * Gets a submap of this instance which contains all stacks with the same material as the given one and their
	 * respective amounts.
	 *
	 * @param m Material to search for
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
	 * Gets a submap of this instance which contains all stacks with the same material and enchants as the given one
	 * and their respective amounts.
	 *
	 * @param m Material to search for
	 * @param enchants Enchants to search for
	 * @return New ItemMap with all ItemStack and their amount whose material and enchants matches the given one
	 */
	public ItemMap getStacksByMaterialEnchants(Material m, Map<Enchantment, Integer> enchants) {
		ItemMap result = new ItemMap();
		for (ItemStack is : items.keySet()) {
			if (is.getType() == m && is.getItemMeta() != null && is.getItemMeta().getEnchants().equals(enchants)) {
				result.addItemAmount(is.clone(), items.get(is));
			}
		}
		return result;
	}

	public ItemMap getStacksByMaterialEnchants(ItemStack is) {
		if (is.getItemMeta() != null) {
			return getStacksByMaterialEnchants(is.getType(), is.getItemMeta().getEnchants());
		} else {
			return getStacksByMaterialEnchants(is.getType(), new HashMap<>());
		}
	}

	/**
	 * Gets a submap of this instance which contains all stacks with the same lore as the given and their respective
	 * amount.
	 *
	 * @param lore Lore to search for
	 * @return New ItemMap with all ItemStacks and their amount whose lore matches the given one
	 */
	public ItemMap getStacksByLore(final List<String> lore) {
		final boolean gaveLore = CollectionUtils.isNotEmpty(lore);
		final ItemMap result = new ItemMap();
		for (final ItemStack key : this.items.keySet()) {
			if (!key.hasItemMeta()) {
				continue;
			}
			final var keyMeta = key.getItemMeta();
			if (gaveLore != keyMeta.hasLore()) {
				continue;
			}
			final var keyLore = keyMeta.getLore();
			if (!Objects.equals(lore, keyLore)) {
				continue;
			}
			result.addItemAmount(key.clone(), this.items.getInt(key));
		}
		return result;
	}

	/**
	 * Gets how many items of the given stack are in this map. Be aware that if a stack doesnt equal with the given one,
	 * for example because of mismatched NBT tags, it wont be included in the result
	 *
	 * @param is Exact ItemStack to search for
	 * @return amount of items like the given stack in this map
	 */
	public int getAmount(ItemStack is) {
		ItemMap matSubMap = getStacksByMaterial(is);
		int amount = 0;
		for (Entry<ItemStack, Integer> entry : matSubMap.getEntrySet()) {
			ItemStack current = entry.getKey();
			if (MetaUtils.areMetasEqual(is.getItemMeta(), current.getItemMeta())) {
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

	@SuppressWarnings("deprecation")
	public Set<Entry<ItemStack, Integer>> getEntrySet() {
		return this.items.entrySet();
	}

	/**
	 * Checks whether an inventory contains exactly what's described in this ItemMap
	 *
	 * @param i Inventory to compare
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
	 * Checks whether this instance is completely contained in the given inventory, which means every stack in this
	 * instance is also in the given inventory and the amount in the given inventory is either the same or bigger as in
	 * this instance
	 *
	 * @param i inventory to check
	 * @return true if this instance is completely contained in the given inventory, false if not
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
		StringBuilder res = new StringBuilder();
		for (ItemStack is : getItemStackRepresentation()) {
			res.append(is.toString()).append(";");
		}
		return res.toString();
	}

	/**
	 * Checks how often this ItemMap is contained in the given ItemMap or how often this ItemMap could be removed from
	 * the given one before creating negative stacks
	 *
	 * @param i ItemMap to check
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
	 * @param multiplier Multiplier to scale the amount of the contained items with
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
	public List<ItemStack> getItemStackRepresentation() {
		List<ItemStack> result = new ArrayList<>();
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
	 * @param i Inventory to check
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
	 * Instead of converting into many stacks of maximum size, this creates a stack with an amount of one for each
	 * entry and adds the total item amount and stack count as lore, which is needed to display larger ItemMaps in
	 * inventories
	 *
	 * @return UI representation of large ItemMap
	 */
	public List<ItemStack> getLoredItemCountRepresentation() {
		Set<Entry<ItemStack, Integer>> entrySet = getEntrySet();
		List<ItemStack> items = new ArrayList<>(entrySet.size());
		for (Entry<ItemStack, Integer> entry : entrySet) {
			ItemStack is = entry.getKey().clone();
			ItemUtils.addLore(is, ChatColor.GOLD + "Total item count: " + entry.getValue());
			if (entry.getValue() > entry.getKey().getType().getMaxStackSize()) {
				int stacks = entry.getValue() / is.getType().getMaxStackSize();
				int extra = entry.getValue() % is.getType().getMaxStackSize();
				StringBuilder out = new StringBuilder(ChatColor.GOLD.toString());
				if (stacks != 0) {
					out.append(stacks).append(" stack").append(stacks == 1 ? "" : "s");
				}
				if (extra != 0) {
					out.append(" and ").append(extra);
					out.append(" item").append(extra == 1 ? "" : "s");
				}
				ItemUtils.addLore(is, out.toString());
			}
			items.add(is);
		}
		return items;
	}

	/**
	 * Attempts to remove the content of this ItemMap from the given inventory. If it fails to find all the required
	 * items it will stop and return false
	 *
	 * @param i Inventory to remove from
	 * @return True if everything was successfully removed, false if not
	 */
	public boolean removeSafelyFrom(Inventory i) {
		for (Entry<ItemStack, Integer> entry : getEntrySet()) {
			int amountToRemove = entry.getValue();
			ItemStack is = entry.getKey();
			for (ItemStack inventoryStack : i.getStorageContents()) {
				if (inventoryStack == null) {
					continue;
				}
				if (inventoryStack.getType() == is.getType()) {
					ItemMap compareMap = new ItemMap(inventoryStack);
					int removeAmount = Math.min(amountToRemove, compareMap.getAmount(is));
					if (removeAmount != 0) {
						ItemStack cloneStack = inventoryStack.clone();
						cloneStack.setAmount(removeAmount);
						if (!i.removeItem(cloneStack).isEmpty()) {
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
				if (i instanceof PlayerInventory pInv) {
					ItemStack offHand = pInv.getItemInOffHand();
					if (offHand == null) {
						return false;
					}
					if (offHand.getType() == is.getType()) {
						ItemMap compareMap = new ItemMap(offHand);
						int removeAmount = Math.min(amountToRemove, compareMap.getAmount(is));
						int updatedCount = Math.max(0, offHand.getAmount() - removeAmount);
						amountToRemove -= removeAmount;
						if (updatedCount == 0) {
							pInv.setItemInOffHand(null);
						} else {
							offHand.setAmount(updatedCount);
						}
					}

				}
				if (amountToRemove > 0) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ItemMap im) {
			if (im.getTotalItemAmount() == getTotalItemAmount()) {
				return im.getEntrySet().equals(getEntrySet());
			}
		}
		return false;
	}

	/**
	 * Utility to add NBT tags to an item and produce a custom stack size
	 *
	 * @param item  Template Bukkit ItemStack
	 * @param amount Output Stack Size
	 * @param map Java Maps and Lists representing NBT data
	 * @return Cloned ItemStack with amount set to amt and NBT set to map.
	 */
	public static ItemStack enrichWithNBT(ItemStack item, int amount, Map<String, Object> map) {
		LOGGER.fine("Received request to enrich " + item.toString());
		item = item.clone();
		item.setAmount(MoreMath.clamp(amount, 1, item.getMaxStackSize()));
		item = NBTSerialization.processItem(item, (nbt) -> mapToNBT(nbt, map));
		return item;
	}

	@SuppressWarnings("unchecked")
	public static NBTTagCompound mapToNBT(NBTTagCompound base, Map<String, Object> map) {
		LOGGER.fine("Representing map --> NBTTagCompound");
		if (map == null || base == null) {
			return base;
		}
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			Object object = entry.getValue();
			if (object instanceof Map) {
				LOGGER.fine("Adding map at key " + entry.getKey());
				base.set(entry.getKey(), mapToNBT(new NBTTagCompound(), (Map<String, Object>) object));
			} else if (object instanceof MemorySection) {
				LOGGER.fine("Adding map from MemorySection at key " + entry.getKey());
				base.set(entry.getKey(), mapToNBT(new NBTTagCompound(), ((MemorySection) object).getValues(true)));
			} else if (object instanceof List) {
				LOGGER.fine("Adding list at key " + entry.getKey());
				base.set(entry.getKey(), listToNBT(new NBTTagList(), (List<Object>) object));
			} else if (object instanceof String) {
				LOGGER.fine("Adding String " + object + " at key " + entry.getKey());
				base.setString(entry.getKey(), (String) object);
			} else if (object instanceof Double) {
				LOGGER.fine("Adding Double " + object + " at key " + entry.getKey());
				base.setDouble(entry.getKey(), (Double) object);
			} else if (object instanceof Float) {
				LOGGER.fine("Adding Float " + object + " at key " + entry.getKey());
				base.setFloat(entry.getKey(), (Float) object);
			} else if (object instanceof Boolean) {
				LOGGER.fine("Adding Boolean " + object + " at key " + entry.getKey());
				base.setBoolean(entry.getKey(), (Boolean) object);
			} else if (object instanceof Byte) {
				LOGGER.fine("Adding Byte " + object + " at key " + entry.getKey());
				base.setByte(entry.getKey(), (Byte) object);
			} else if (object instanceof Short) {
				LOGGER.fine("Adding Byte " + object + " at key " + entry.getKey());
				base.setShort(entry.getKey(), (Short) object);
			} else if (object instanceof Integer) {
				LOGGER.fine("Adding Integer " + object + " at key " + entry.getKey());
				base.setInt(entry.getKey(), (Integer) object);
			} else if (object instanceof Long) {
				LOGGER.fine("Adding Long " + object + " at key " + entry.getKey());
				base.setLong(entry.getKey(), (Long) object);
			} else if (object instanceof byte[]) {
				LOGGER.fine("Adding bytearray at key " + entry.getKey());
				base.setByteArray(entry.getKey(), (byte[]) object);
			} else if (object instanceof int[]) {
				LOGGER.fine("Adding intarray at key " + entry.getKey());
				base.setIntArray(entry.getKey(), (int[]) object);
			} else if (object instanceof UUID) {
				LOGGER.fine("Adding UUID " + object + " at key " + entry.getKey());
				base.a(entry.getKey(), (UUID) object);
			} else if (object instanceof NBTBase) {
				LOGGER.fine("Adding nbtobject at key " + entry.getKey());
				base.set(entry.getKey(), (NBTBase) object);
			} else {
				LOGGER.warning("Unrecognized entry in map-->NBT: " + object.toString());
			}
		}
		return base;
	}

	@SuppressWarnings("unchecked")
	public static NBTTagList listToNBT(NBTTagList base, List<Object> list) {
		LOGGER.fine("Representing list --> NBTTagList");
		if (list == null || base == null) {
			return base;
		}
		for (Object object : list) {
			if (object instanceof Map) {
				LOGGER.fine("Adding map to list");
				base.add(mapToNBT(new NBTTagCompound(), (Map<String, Object>) object));
			} else if (object instanceof MemorySection) {
				LOGGER.fine("Adding map from MemorySection to list");
				base.add(mapToNBT(new NBTTagCompound(), ((MemorySection) object).getValues(true)));
			} else if (object instanceof List) {
				LOGGER.fine("Adding list to list");
				base.add(listToNBT(new NBTTagList(), (List<Object>) object));
			} else if (object instanceof String) {
				LOGGER.fine("Adding string " + object + " to list");
				base.add(NBTTagString.a((String) object));
			} else if (object instanceof Double) {
				LOGGER.fine("Adding double " + object + " to list");
				base.add(NBTTagDouble.a((Double) object));
			} else if (object instanceof Float) {
				LOGGER.fine("Adding float " + object + " to list");
				base.add(NBTTagFloat.a((Float) object));
			} else if (object instanceof Byte) {
				LOGGER.fine("Adding byte " + object + " to list");
				base.add(NBTTagByte.a((Byte) object));
			} else if (object instanceof Short) {
				LOGGER.fine("Adding short " + object + " to list");
				base.add(NBTTagShort.a((Short) object));
			} else if (object instanceof Integer) {
				LOGGER.fine("Adding integer " + object + " to list");
				base.add(NBTTagInt.a((Integer) object));
			} else if (object instanceof Long) {
				LOGGER.fine("Adding long " + object + " to list");
				base.add(NBTTagLong.a((Long) object));
			} else if (object instanceof byte[]) {
				LOGGER.fine("Adding byte array to list");
				base.add(new NBTTagByteArray((byte[]) object));
			} else if (object instanceof int[]) {
				LOGGER.fine("Adding int array to list");
				base.add(new NBTTagIntArray((int[]) object));
			} else if (object instanceof NBTBase) {
				LOGGER.fine("Adding nbt object to list");
				base.add((NBTBase) object);
			} else {
				LOGGER.warning("Unrecognized entry in list-->NBT: " + base);
			}
		}
		return base;
	}

}
