package vg.civcraft.mc.civmodcore.inventory.items;

import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/// This is explicitly intended for using items as map-keys.
public final class ItemKey {
	/// Remember not to modify this!
	public final ItemStack item;
	private final int hash;

	/// @apiNote The given item should **NOT** under any circumstances be modified after being used as a map-key.
	public ItemKey(
		final @NotNull ItemStack item
	) {
		this.item = ItemUtils.requireNotEmpty(item);
		/** Check how {@link CraftItemStack#hashCode()} hashes the item without its amount */
		this.hash = net.minecraft.world.item.ItemStack.hashItemAndComponents(CraftItemStack.unwrap(this.item));
	}

	@Override
	public boolean equals(
		final Object obj
	) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof final ItemKey other) {
			return this.item.isSimilar(other.item);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.hash;
	}
}
