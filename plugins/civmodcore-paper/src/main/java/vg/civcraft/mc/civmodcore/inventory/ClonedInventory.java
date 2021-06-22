package vg.civcraft.mc.civmodcore.inventory;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.utilities.MoreArrayUtils;

/**
 * Wrapper for cloned inventories intended to ensure that ClonedInventories aren't themselves cloned.
 */
public final class ClonedInventory implements Inventory {

	private final Inventory inventory;

	private ClonedInventory(final Inventory inventory) {
		this.inventory = inventory;
	}

	@Override
	public int getSize() {
		return this.inventory.getSize();
	}

	@Override
	public int getMaxStackSize() {
		return this.inventory.getMaxStackSize();
	}

	@Override
	public void setMaxStackSize(final int size) {
		this.inventory.setMaxStackSize(size);
	}

	@Override
	public ItemStack getItem(final int index) {
		return this.inventory.getItem(index);
	}

	@Override
	public void setItem(final int index, final ItemStack item) {
		this.inventory.setItem(index, item);
	}

	@Override
	public HashMap<Integer, ItemStack> addItem(final ItemStack... items) throws IllegalArgumentException {
		return this.inventory.addItem(items);
	}

	@NotNull
	@Override
	public HashMap<Integer, ItemStack> removeItem(final ItemStack... items) throws IllegalArgumentException {
		return this.inventory.removeItem(items);
	}

	@NotNull
	@Override
	public HashMap<Integer, ItemStack> removeItemAnySlot(@NotNull ItemStack... items) throws IllegalArgumentException {
		return this.inventory.removeItemAnySlot(items);
	}

	@Override
	public ItemStack[] getContents() {
		return this.inventory.getContents();
	}

	@Override
	public void setContents(final ItemStack[] items) throws IllegalArgumentException {
		this.inventory.setContents(items);
	}

	@Override
	public ItemStack[] getStorageContents() {
		return this.inventory.getStorageContents();
	}

	@Override
	public void setStorageContents(final ItemStack[] items) throws IllegalArgumentException {
		this.inventory.setStorageContents(items);
	}

	@Override
	public boolean contains(@NotNull final Material material) throws IllegalArgumentException {
		return this.inventory.contains(material);
	}

	@Override
	public boolean contains(final ItemStack item) {
		return this.inventory.contains(item);
	}

	@Override
	public boolean contains(@NotNull final Material material, final int amount) throws IllegalArgumentException {
		return this.inventory.contains(material, amount);
	}

	@Override
	public boolean contains(final ItemStack item, final int amount) {
		return this.inventory.contains(item, amount);
	}

	@Override
	public boolean containsAtLeast(final ItemStack item, final int amount) {
		return this.inventory.containsAtLeast(item, amount);
	}

	@Override
	public HashMap<Integer, ? extends ItemStack> all(@NotNull final Material material) throws IllegalArgumentException {
		return this.inventory.all(material);
	}

	@Override
	public HashMap<Integer, ? extends ItemStack> all(final ItemStack item) {
		return this.inventory.all(item);
	}

	@Override
	public int first(@NotNull final Material material) throws IllegalArgumentException {
		return this.inventory.first(material);
	}

	@Override
	public int first(@NotNull final ItemStack item) {
		return this.inventory.first(item);
	}

	@Override
	public int firstEmpty() {
		return this.inventory.firstEmpty();
	}

	@Override
	public boolean isEmpty() {
		return this.inventory.isEmpty();
	}

	@Override
	public void remove(@NotNull final Material material) throws IllegalArgumentException {
		this.inventory.remove(material);
	}

	@Override
	public void remove(@NotNull final ItemStack item) {
		this.inventory.remove(item);
	}

	@Override
	public void clear(final int index) {
		this.inventory.clear(index);
	}

	@Override
	public void clear() {
		this.inventory.clear();
	}

	@Override
	public int close() {
		return this.inventory.close();
	}

	@Override
	public List<HumanEntity> getViewers() {
		return this.inventory.getViewers();
	}

	@Override
	public InventoryType getType() {
		return this.inventory.getType();
	}

	@Override
	public InventoryHolder getHolder() {
		return this.inventory.getHolder();
	}

	@Nullable
	@Override
	public InventoryHolder getHolder(boolean useSnapshot) {
		return this.inventory.getHolder(useSnapshot);
	}

	@NotNull
	@Override
	public ListIterator<ItemStack> iterator() {
		return this.inventory.iterator();
	}

	@Override
	public void forEach(final Consumer<? super ItemStack> action) {
		this.inventory.forEach(action);
	}

	@Override
	public Spliterator<ItemStack> spliterator() {
		return this.inventory.spliterator();
	}

	@Override
	public ListIterator<ItemStack> iterator(final int index) {
		return this.inventory.iterator(index);
	}

	@Override
	public Location getLocation() {
		return this.inventory.getLocation();
	}

	/**
	 * Gets the underlying inventory that was cloned.
	 *
	 * @return Returns the underlying inventory.
	 */
	public Inventory getInventory() {
		return this.inventory;
	}

	/**
	 * <p>Clones the given inventory for the purpose of test manipulating its contents.</p>
	 *
	 * <p>Note: Do not type check the inventory, it's JUST a contents copy within an inventory wrapper to provide the
	 * relevant and useful methods.</p>
	 *
	 * @param inventory The inventory to clone.
	 * @return Returns a clone of the given inventory.
	 */
	public static ClonedInventory cloneInventory(final Inventory inventory) {
		return cloneInventory(inventory, false);
	}

	/**
	 * <p>Clones the given inventory for the purpose of test manipulating its contents.</p>
	 *
	 * <p>Note: Do not type check the inventory, it's JUST a contents copy within an inventory wrapper to provide the
	 * relevant and useful methods.</p>
	 *
	 * @param inventory The inventory to clone.
	 * @param forceClone Determines whether the inventory should be cloned even if it's an already cloned inventory.
	 * @return Returns a clone of the given inventory.
	 */
	public static ClonedInventory cloneInventory(final Inventory inventory, final boolean forceClone) {
		if (inventory == null) {
			return null;
		}
		if (!forceClone && inventory instanceof ClonedInventory) {
			return (ClonedInventory) inventory;
		}
		Inventory clone;
		if (inventory.getType() == InventoryType.CHEST) {
			clone = Bukkit.createInventory(inventory.getHolder(), inventory.getSize());
		}
		else {
			clone = Bukkit.createInventory(inventory.getHolder(), inventory.getType());
		}
		final ItemStack[] contents = inventory.getContents();
		MoreArrayUtils.computeElements(contents, (item) -> item == null ? null : item.clone());
		clone.setContents(contents);
		return new ClonedInventory(clone);
	}

}
