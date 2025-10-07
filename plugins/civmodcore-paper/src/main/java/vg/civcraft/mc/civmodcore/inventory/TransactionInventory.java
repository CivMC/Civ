package vg.civcraft.mc.civmodcore.inventory;

import java.util.Objects;
import org.bukkit.craftbukkit.inventory.CraftInventoryCustom;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/// Creates a disposable inventory based on a given accessor that you can modify without worry, and only committing if
/// satisfied with the result.
public final class TransactionInventory extends CraftInventoryCustom {
    private final InventoryAccessor accessor;

    public TransactionInventory(
        final @NotNull InventoryAccessor accessor
    ) {
        this(accessor, InventoryUtils.clone(accessor.getContents()));
    }

    /// TODO: This should not be necessary with https://openjdk.org/jeps/513
    private TransactionInventory(
        final @NotNull InventoryAccessor accessor,
        final @Nullable ItemStack @NotNull [] contents
    ) {
        super(null, contents.length);
        this.accessor = Objects.requireNonNull(accessor);
        setContents(contents);
    }

    public boolean addedAllItems(
        final @NotNull Iterable<@NotNull ItemStack> items
    ) {
        for (final ItemStack itemToAdd : items) {
            /// The clone is to avoid the item-amount contamination mentioned in `addItem`'s javadoc
            if (!addItem(itemToAdd.clone()).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean removedAllItems(
        final @NotNull Iterable<@NotNull ItemStack> items
    ) {
        /// The clone is to avoid the item-amount contamination mentioned in `removeItem`'s javadoc
        for (final ItemStack itemToRemove : items) {
            if (!removeItem(itemToRemove.clone()).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void commit() {
        this.accessor.setContents(getContents());
    }
}
