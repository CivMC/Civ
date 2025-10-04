package vg.civcraft.mc.civmodcore.inventory;

import java.util.Objects;
import java.util.function.Predicate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/// For reasons unknown, Bukkit inventories have a "contents" and a "storage contents". You can access these directly
/// with getters and setters, but these get/set item arrays: there are no APIs for either adding an item specifically
/// to an inventory's "storage contents", nor are there any options for wrapping an item array ByteBuffer-style as an
/// [Inventory].
///
/// This is used primarily by ItemExchange's `TransactionInventory`, which creates a clone-inventory based off of
/// [#getContents()], which can then take advantage of [Inventory] APIs. After you're done, you call `commit`, which
/// retrieves the clone's items via [Inventory#getContents()] and sets them back on the original-inventory in the
/// correct place via [#setContents(ItemStack\[\])].
public interface InventoryAccessor {
	@Nullable ItemStack @NotNull [] getContents();

	void setContents(
		ItemStack @NotNull [] contents
	);

	/// Convenience function
	default void editContents(
		final @NotNull Predicate<@Nullable ItemStack @NotNull []> modifier
	) {
		final ItemStack[] contents = getContents();
		if (modifier.test(contents)) {
			setContents(contents);
		}
	}

	// ============================================================
	//
	// ============================================================

	static @NotNull InventoryAccessor fullContents(
		final @NotNull Inventory inventory
	) {
		Objects.requireNonNull(inventory);
		return new InventoryAccessor() {
			@Override
			public @Nullable ItemStack @NotNull [] getContents() {
				return inventory.getContents();
			}
			@Override
			public void setContents(final ItemStack @NotNull [] contents) {
				inventory.setContents(contents);
			}
		};
	}

	static @NotNull InventoryAccessor storageContents(
		final @NotNull Inventory inventory
	) {
		Objects.requireNonNull(inventory);
		return new InventoryAccessor() {
			@Override
			public @Nullable ItemStack @NotNull [] getContents() {
				return inventory.getStorageContents();
			}
			@Override
			public void setContents(final ItemStack @NotNull [] contents) {
				inventory.setStorageContents(contents);
			}
		};
	}

	/// This *should* just be the player's 4x9 storage
	static @NotNull InventoryAccessor playerStorage(
		final @NotNull PlayerInventory playerInventory
	) {
		return storageContents(playerInventory);
	}

	/// This *should* just be the player's 4x9 storage
	static @NotNull InventoryAccessor playerStorage(
		final @NotNull Player player
	) {
		return playerStorage(player.getInventory());
	}
}
