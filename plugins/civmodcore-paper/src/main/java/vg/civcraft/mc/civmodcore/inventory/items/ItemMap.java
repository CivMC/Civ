package vg.civcraft.mc.civmodcore.inventory.items;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
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
import java.util.function.BiFunction;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.keyvalue.UnmodifiableMapEntry;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;

/**
 * Allows the storage and comparison of item stacks while ignoring their maximum possible stack sizes. This offers
 * various tools to compare inventories, to store recipe costs or to specify setup costs. Take great care when dealing
 * with item stacks with negative amounts, while this implementation should be consistent even with negative values,
 * they create possibly unexpected results. For example an empty inventory/item map will seem to contain items when
 * compared to a map containing negative values. Additionally this implementation allows durability "wild cards", if
 * you specify -1 as durability it will count as any given durability. When working with multiple ItemMaps this will
 * only work if all methods are executed on the instance containing items with a durability of -1.
 * <p>
 * TODO: ItemMap is troubling because it manipulates and searches for items in ways that are not friendly to modern
 *       ways of cataloging items. For example, Bastion materials are all custom items, but they're only custom in the
 *       sense that they are named and lored.. but an Energizer will match with any other emerald unless you use any of
 *       the weirdly specific methods to weed out custom items. I think it would be better to refactor ItemMap to
 *       specifically support custom items, but I definitely need help in that regard. Or we can just keep it around
 *       for a little longer since they work fine enough and aren't in critical updates.
 */
public class ItemMap {

    private final Object2IntMap<ItemStack> items;
    private final Object2IntOpenHashMap<String> customItems;
	private int totalItems;

    /**
     * Empty constructor to create empty item map
     */
    public ItemMap() {
        this.items = new Object2IntOpenHashMap<>(0);
        this.items.defaultReturnValue(0);
        this.customItems = new Object2IntOpenHashMap<>();
        this.customItems.defaultReturnValue(0);
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
        this.customItems = new Object2IntOpenHashMap<>();
        this.customItems.defaultReturnValue(0);
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
        this.customItems = new Object2IntOpenHashMap<>();
        this.customItems.defaultReturnValue(0);
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
        this.customItems = new Object2IntOpenHashMap<>();
        this.customItems.defaultReturnValue(0);
        this.totalItems = 0;
        update(inventory);
    }

    private static ItemStack INTERNAL_createKey(ItemStack item) {
        return item.asOne();
    }

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
        BiFunction<Object, Integer, Integer> addFn = (key, amount) ->
            amount == null ? input.getAmount() : amount + input.getAmount();
        String customItemKey = CustomItem.getCustomItemKey(input);
        if (customItemKey != null) {
            this.customItems.computeInt(customItemKey, addFn);
        } else {
            this.items.computeInt(INTERNAL_createKey(input), addFn);
        }
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
        BiFunction<Object, Integer, Integer> subFun = (_key, amount) -> (amount -= input.getAmount()) <= 0 ? null : amount;
        String customItemKey = CustomItem.getCustomItemKey(input);
        if (customItemKey != null) {
            this.customItems.computeIntIfPresent(customItemKey, subFun);
        } else {
            this.items.computeIntIfPresent(key, subFun);
        }
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
        return Objects.hash(this.items.hashCode(), this.customItems.hashCode());
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
        for (Entry<ItemStack, Integer> entry : im.items.object2IntEntrySet()) {
            addItemAmount(entry.getKey(), entry.getValue());
        }
        for (Entry<String, Integer> entry : im.customItems.object2IntEntrySet()) {
            addItemAmount(CustomItem.getCustomItem(entry.getKey()), entry.getValue());
        }
    }

    public void update(final Inventory inventory) {
        this.items.clear();
        this.customItems.clear();
        this.totalItems = 0;
        for (int i = 0; i < inventory.getSize(); i++) {
            final ItemStack item = inventory.getItem(i);
            if (item != null) {
                addItemStack(item);
            }
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
     * matches the given one
     */
    public ItemMap getStacksByMaterial(Material m) {
        ItemMap result = new ItemMap();
        for (ItemStack is : items.keySet()) {
            if (is.getType() == m) {
                result.addItemAmount(is.clone(), items.get(is));
            }
        }
        for (String is : customItems.keySet()) {
            ItemStack item = CustomItem.getCustomItem(is);
            if (item != null && item.getType() == m) {
                result.addItemAmount(item, customItems.get(is));
            }
        }
        return result;
    }

    public ItemMap getStacksByMaterial(ItemStack is) {
        return getStacksByMaterial(is.getType());
    }

    /**
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
        for (Entry<ItemStack, Integer> entry : matSubMap.items.object2IntEntrySet()) {
            ItemStack current = entry.getKey();
            if (MetaUtils.areMetasEqual(is.getItemMeta(), current.getItemMeta())) {
                amount += entry.getValue();
            }
        }
        for (Entry<String, Integer> entry : matSubMap.customItems.object2IntEntrySet()) {
            if (entry.getKey().equals(CustomItem.getCustomItemKey(is))) {
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
        return items.keySet().size() + customItems.keySet().size();
    }

    public Map<ItemStack, Integer> getAllItems() {
        Object2IntOpenHashMap<ItemStack> map = new Object2IntOpenHashMap<>(this.items);
        for (Object2IntMap.Entry<String> entry : this.customItems.object2IntEntrySet()) {
            map.put(CustomItem.getCustomItem(entry.getKey()), entry.getIntValue());
        }
        return map;
    }

    public Object2IntMap<ItemStack> getItems() {
        return items;
    }

    public Object2IntOpenHashMap<String> getCustomItems() {
        return customItems;
    }

    /**
     * Checks whether an inventory contains exactly what's described in this ItemMap
     *
     * @param i Inventory to compare
     * @return True if the inventory is identical with this instance, false if not
     */
    public boolean containedExactlyIn(Inventory i) {
        ItemMap invMap = new ItemMap(i);
        for (Entry<ItemStack, Integer> entry : getAllItems().entrySet()) {
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
        for (Entry<ItemStack, Integer> entry : getAllItems().entrySet()) {
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
        for (Entry<ItemStack, Integer> entry : getAllItems().entrySet()) {
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
        for (Entry<ItemStack, Integer> entry : this.items.object2IntEntrySet()) {
            items.put(entry.getKey(), (int) (entry.getValue() * multiplier));
            totalItems += (int) (entry.getValue() * multiplier);
        }
        for (Entry<String, Integer> entry : this.customItems.object2IntEntrySet()) {
            customItems.put(entry.getKey(), (int) (entry.getValue() * multiplier));
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
        for (Entry<ItemStack, Integer> entry : getAllItems().entrySet()) {
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
    @Override
    public ItemMap clone() {
        ItemMap clone = new ItemMap();
        for (Entry<ItemStack, Integer> entry : this.getAllItems().entrySet()) {
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
        Set<Entry<ItemStack, Integer>> entrySet = getAllItems().entrySet();
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
        for (Entry<ItemStack, Integer> entry : getAllItems().entrySet()) {
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
				return im.items.equals(items) && im.customItems.equals(customItems);
			}
		}
		return false;
	}
}
