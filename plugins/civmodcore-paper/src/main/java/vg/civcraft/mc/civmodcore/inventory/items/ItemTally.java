package vg.civcraft.mc.civmodcore.inventory.items;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/// This is basically a simpler and more direct version of [vg.civcraft.mc.civmodcore.inventory.items.ItemMap] intended
/// for quickly tallying up a collection of items.
public final class ItemTally {
	private final Object2IntMap<ItemKey> items;

	public ItemTally() {
		this.items = new Object2IntOpenHashMap<>(0);
		this.items.defaultReturnValue(0);
	}

	public void add(
		final @NotNull ItemKey item,
		final int amount
	) {
		this.items.merge(Objects.requireNonNull(item), amount, (lhs, rhs) -> {
			lhs = Objects.requireNonNullElse(lhs, 0);
			rhs = Objects.requireNonNullElse(rhs, 0);
			final var val = lhs + rhs;
			return val < 1 ? null : val;
		});
	}

	/// Convenience function
	public void subtract(
		final @NotNull ItemKey item,
		final int amount
	) {
		add(item, -amount);
	}

	public void addAll(
		final @NotNull ItemTally other
	) {
		other.items.forEach(this::add);
	}

	public void subtractAll(
		final @NotNull ItemTally other
	) {
		other.items.forEach(this::subtract);
	}

	/// Creates a list of items based on this tally. For example, if you have a tally of 130 diamonds, this will create
	/// a list of: `[64x DIAMOND, 64x DIAMOND, 2x DIAMOND]`
	public @NotNull List<@NotNull ItemStack> toItemList() {
		final var list = new ArrayList<ItemStack>();
		this.items.forEach((key, remainingAmount) -> {
			final int maxStackAmount = key.item.getMaxStackSize();
			while (remainingAmount > 0) {
				final int amount = Math.min(maxStackAmount, remainingAmount);
				remainingAmount -= amount;
				list.add(key.item.asQuantity(amount));
			}
		});
		return list;
	}

	/// Convenience function
	/// @throws IllegalArgumentException if the resulting [ItemStash] would be empty.
	/// @see ItemStash
	public @NotNull ItemStash toItemStash() {
		return new ItemStash(toItemList());
	}

	public @Range(from = 0, to = Integer.MAX_VALUE) int totalItemCount() {
		final var totalItemCount = new int[1];
		this.items.values().removeIf((amount) -> {
			if (amount < 1) {
				return true;
			}
			totalItemCount[0] += amount;
			return false;
		});
		return totalItemCount[0];
	}

	@Override
	public boolean equals(
		final Object obj
	) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof final ItemTally other) {
			return this.items.equals(other.items);
		}
		return false;
	}

	@Override
	public @NotNull String toString() {
		final var builder = new StringBuilder();
		builder.append(getClass().getSimpleName());
		builder.append('{');
		for (final var iter = this.items.object2IntEntrySet().iterator(); iter.hasNext();) {
			final var entry = iter.next();
			builder.append(entry.getKey().item);
			builder.append(':');
			builder.append(entry.getIntValue());
			if (iter.hasNext()) {
				builder.append(',');
			}
		}
		builder.append('}');
		return builder.toString();
	}
}
