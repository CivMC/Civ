package vg.civcraft.mc.civmodcore.inventory.items;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/// This is to ensure, as much as it is possible to ensure, that there's a not-null, not-empty list of items which are
/// themselves not-null, not-empty, and have valid amounts.
public record ItemStash(
	@NotNull List<@NotNull ItemStack> items
) implements Iterable<ItemStack> {
	public ItemStash {
		try {
			items = List.copyOf(items); // This will do a list and per-element null check
		}
		catch (final NullPointerException e) {
			throw new IllegalArgumentException("item stash cannot be null or contain null elements!");
		}
		if (items.isEmpty()) {
			throw new IllegalArgumentException("item stash cannot be empty!");
		}
		for (final ItemStack item : items) {
			if (ItemUtils.isEmptyItem(item)) {
				throw new IllegalArgumentException("item stash cannot contain empty items!");
			}
			if (!ItemUtils.isValidItemAmount(item)) {
				throw new IllegalArgumentException("item stash cannot contain invalid item amounts!");
			}
		}
	}

	public ItemStash(
		final @NotNull ItemStack @NotNull ... items
	) {
		this(Arrays.asList(items));
	}

	@Override
	public Iterator<ItemStack> iterator() {
		return items().iterator();
	}
}
