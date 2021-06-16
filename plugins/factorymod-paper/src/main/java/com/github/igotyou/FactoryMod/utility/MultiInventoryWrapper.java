package com.github.igotyou.FactoryMod.utility;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * @author caucow
 */
public class MultiInventoryWrapper implements Inventory {

	private final Inventory[] wrapped;
	private final int size;
	private final int[] slotOffsets;

	public MultiInventoryWrapper(Inventory... wrapped) {
		this(new ArrayList<>(Arrays.asList(wrapped)));
	}

	public MultiInventoryWrapper(ArrayList<Inventory> wrapped) {
		Inventory[] wrappedArr = uniquify(wrapped);
		this.wrapped = wrappedArr;
		int lsize = 0;
		int[] lslotOffsets = new int[wrappedArr.length];
		for (int i = 0; i < wrappedArr.length; i++) {
			Inventory inv = wrappedArr[i];
			lslotOffsets[i] = lsize;
			lsize += inv.getSize();
			if (inv.getMaxStackSize() != 64) {
				throw new IllegalArgumentException("Inventory max stack size must be 64");
			}
		}
		this.size = lsize;
		this.slotOffsets = lslotOffsets;
	}

	private int getInventoryIndex(int combinedSlot) {
		for (int i = 0; i < wrapped.length - 1; i++) {
			if (combinedSlot < slotOffsets[i + 1]) {
				return i;
			}
		}
		if (combinedSlot < size) {
			return wrapped.length - 1;
		}
		throw new IndexOutOfBoundsException("Slot index " + combinedSlot
				+ " is outside the bounds of the multi-inventory");
	}

	@Override
	public int close() {
		List<HumanEntity> viewers = getViewers();
		int num = viewers.size();
		viewers.forEach(HumanEntity::closeInventory);
		return num;
	}

	@Override
	public int getSize() {
		return size;
	}

	@Override
	public int getMaxStackSize() {
		return 64;
	}

	@Override
	public void setMaxStackSize(int i) {
		throw new UnsupportedOperationException();
	}

	@Override
	public @Nullable ItemStack getItem(int i) {
		int invi = getInventoryIndex(i);
		return wrapped[invi].getItem(i - slotOffsets[invi]);
	}

	@Override
	public void setItem(int i, @Nullable ItemStack itemStack) {
		int invi = getInventoryIndex(i);
		wrapped[invi].setItem(i - slotOffsets[invi], itemStack);
	}

	@Override
	public @NotNull HashMap<Integer, ItemStack> addItem(@NotNull ItemStack... itemStacks) throws IllegalArgumentException {
		HashMap<Integer, ItemStack> result = null;
		for (int invi = 0; invi < wrapped.length; invi++) {
			result = wrapped[invi].addItem(itemStacks);
			itemStacks = new ItemStack[result.size()];
			int itemi = 0;
			Iterator<Map.Entry<Integer, ItemStack>> eit = result.entrySet().iterator();
			for (; eit.hasNext() && itemi < itemStacks.length; itemi++) {
				Map.Entry<Integer, ItemStack> entry = eit.next();
				itemStacks[itemi] = entry.getValue();
			}
		}
		return result == null ? new HashMap<>() : result;
	}

	@Override
	public @NotNull HashMap<Integer, ItemStack> removeItem(@NotNull ItemStack... itemStacks) throws IllegalArgumentException {
		HashMap<Integer, ItemStack> result = null;
		for (int invi = 0; invi < wrapped.length; invi++) {
			result = wrapped[invi].removeItem(itemStacks);
			itemStacks = new ItemStack[result.size()];
			int itemi = 0;
			Iterator<Map.Entry<Integer, ItemStack>> eit = result.entrySet().iterator();
			for (; eit.hasNext() && itemi < itemStacks.length; itemi++) {
				Map.Entry<Integer, ItemStack> entry = eit.next();
				itemStacks[itemi] = entry.getValue();
			}
		}
		return result == null ? new HashMap<>() : result;
	}

	@Override
	public @NotNull HashMap<Integer, ItemStack> removeItemAnySlot(@NotNull ItemStack... itemStacks) throws IllegalArgumentException {
		HashMap<Integer, ItemStack> result = null;
		for (int i = 0; i < wrapped.length; i++) {
			result = wrapped[i].removeItemAnySlot(itemStacks);
			itemStacks = new ItemStack[result.size()];
			int index = 0;
			Iterator<Map.Entry<Integer, ItemStack>> eit = result.entrySet().iterator();
			for (; eit.hasNext() && index < itemStacks.length; index++) {
				Map.Entry<Integer, ItemStack> entry = eit.next();
				itemStacks[i] = entry.getValue();
			}
		}
		return result == null ? new HashMap<>() : result;
	}

	@Override
	public @org.checkerframework.checker.nullness.qual.Nullable ItemStack @NonNull [] getContents() {
		ItemStack[] combinedContents = new ItemStack[size];
		for (int inv = 0, slot = 0; inv < wrapped.length; inv++) {
			ItemStack[] sub = wrapped[inv].getContents();
			System.arraycopy(sub, 0, combinedContents, slot, sub.length);
			slot += sub.length;
		}
		return combinedContents;
	}

	@Override
	public void setContents(@NotNull ItemStack[] combinedContents) throws IllegalArgumentException {
		for (int inv = 0, slot = 0; inv < wrapped.length && slotOffsets[inv] < combinedContents.length; inv++) {
			ItemStack[] sub = new ItemStack[wrapped[inv].getSize()];
			System.arraycopy(combinedContents, slotOffsets[inv], sub, 0, Math.min(sub.length, slot - slotOffsets[inv]));
			wrapped[inv].setContents(sub);
			slot += sub.length;
		}
	}

	@Override
	public @NotNull ItemStack[] getStorageContents() {
		return getContents();
	}

	@Override
	public void setStorageContents(@NotNull ItemStack[] combinedContents) throws IllegalArgumentException {
		setContents(combinedContents);
	}

	@Override
	public boolean contains(@NotNull Material material) throws IllegalArgumentException {
		for (Inventory inv : wrapped) {
			if (inv.contains(material)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean contains(@Nullable ItemStack itemStack) {
		for (Inventory inv : wrapped) {
			if (inv.contains(itemStack)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean contains(@NotNull Material material, int i) throws IllegalArgumentException {
		int invi = getInventoryIndex(i);
		return wrapped[invi].contains(material, i - slotOffsets[invi]);
	}

	@Override
	public boolean contains(@Nullable ItemStack itemStack, int i) {
		int invi = getInventoryIndex(i);
		return wrapped[invi].contains(itemStack, i - slotOffsets[invi]);
	}

	@Override
	public boolean containsAtLeast(@Nullable ItemStack itemStack, int i) {
		int invi = getInventoryIndex(i);
		return wrapped[invi].containsAtLeast(itemStack, i - slotOffsets[invi]);
	}

	@Override
	public @NotNull HashMap<Integer, ? extends ItemStack> all(@NotNull Material material) throws IllegalArgumentException {
		HashMap<Integer, ItemStack> result = new HashMap<>();
		for (int i = 0; i < wrapped.length; i++) {
			HashMap<Integer, ? extends ItemStack> partial = wrapped[i].all(material);
			for (Map.Entry<Integer, ? extends ItemStack> entry : partial.entrySet()) {
				result.put(entry.getKey() + slotOffsets[i], entry.getValue());
			}
		}
		return result;
	}

	@Override
	public @NotNull HashMap<Integer, ? extends ItemStack> all(@Nullable ItemStack itemStack) {
		HashMap<Integer, ItemStack> result = new HashMap<>();
		for (int i = 0; i < wrapped.length; i++) {
			HashMap<Integer, ? extends ItemStack> partial = wrapped[i].all(itemStack);
			for (Map.Entry<Integer, ? extends ItemStack> entry : partial.entrySet()) {
				result.put(entry.getKey() + slotOffsets[i], entry.getValue());
			}
		}
		return result;
	}

	@Override
	public int first(@NotNull Material material) throws IllegalArgumentException {
		for (int i = 0; i < wrapped.length; i++) {
			int result = wrapped[i].first(material);
			if (result != -1) {
				return result + slotOffsets[i];
			}
		}
		return -1;
	}

	@Override
	public int first(@NotNull ItemStack itemStack) {
		for (int i = 0; i < wrapped.length; i++) {
			int result = wrapped[i].first(itemStack);
			if (result != -1) {
				return result + slotOffsets[i];
			}
		}
		return -1;
	}

	@Override
	public int firstEmpty() {
		for (int i = 0; i < wrapped.length; i++) {
			int result = wrapped[i].firstEmpty();
			if (result != -1) {
				return result + slotOffsets[i];
			}
		}
		return -1;
	}

	@Override
	public boolean isEmpty() {
		for (Inventory inv : wrapped) {
			if (!inv.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void remove(@NotNull Material material) throws IllegalArgumentException {
		for (Inventory inv : wrapped) {
			inv.remove(material);
		}
	}

	@Override
	public void remove(@NotNull ItemStack itemStack) {
		for (Inventory inv : wrapped) {
			inv.remove(itemStack);
		}
	}

	@Override
	public void clear(int i) {
		int invi = getInventoryIndex(i);
		wrapped[invi].clear(i - slotOffsets[invi]);
	}

	@Override
	public void clear() {
		for (Inventory inv : wrapped) {
			inv.clear();
		}
	}

	@Override
	public @NotNull List<HumanEntity> getViewers() {
		ArrayList<HumanEntity> list = new ArrayList<>();
		for (Inventory inv : wrapped) {
			list.addAll(inv.getViewers());
		}
		return list;
	}

	@Override
	public @NotNull InventoryType getType() {
		return InventoryType.CHEST;
	}

	@Override
	public @Nullable InventoryHolder getHolder() {
		return null;
	}

	@Override
	public @Nullable InventoryHolder getHolder(boolean b) {
		return null;
	}

	@Override
	public @NotNull ListIterator<ItemStack> iterator() {
		throw new UnsupportedOperationException("More effort to implement than can be bothered atm. Feel free to CIY");
	}

	@Override
	public @NotNull ListIterator<ItemStack> iterator(int i) {
		throw new UnsupportedOperationException("More effort to implement than can be bothered atm. Feel free to CIY");
	}

	@Override
	public @Nullable Location getLocation() {
		return null;
	}

	private static Inventory[] uniquify(List<Inventory> wrapped) {
		ListIterator<Inventory> inverator = wrapped.listIterator();
		while (inverator.hasNext()) {
			Inventory next = inverator.next();
			if (next instanceof MultiInventoryWrapper) {
				inverator.remove();
				for (Inventory inv : ((MultiInventoryWrapper) next).wrapped) {
					inverator.add(inv);
					inverator.previous();
				}
			}
		}
		List<InventoryHolder> invHolders = new ArrayList<>(wrapped.size());
		List<Inventory> uniqueInvs = new ArrayList<>(wrapped.size());
		for (Inventory inv : wrapped) {

			InventoryHolder holder = inv.getHolder();
			if (holder instanceof DoubleChest) {
				DoubleChest dch = (DoubleChest) holder;
				InventoryHolder hleft = dch.getLeftSide();
				InventoryHolder hright = dch.getRightSide();
				if (invHolders.contains(dch) || invHolders.contains(hleft) || invHolders.contains(hright)) {
					continue;
				}
				invHolders.add(hleft);
				invHolders.add(hright);
			}
			invHolders.add(holder);
			uniqueInvs.add(inv);
		}
		Inventory[] wrappedArr = uniqueInvs.toArray(new Inventory[uniqueInvs.size()]);
		return wrappedArr;
	}
}
