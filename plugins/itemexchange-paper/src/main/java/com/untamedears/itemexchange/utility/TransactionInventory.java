package com.untamedears.itemexchange.utility;

import java.util.Objects;
import org.bukkit.craftbukkit.inventory.CraftInventoryCustom;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.inventory.InventoryAccessor;
import vg.civcraft.mc.civmodcore.inventory.InventoryUtils;

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

    public void commit() {
        this.accessor.setContents(getContents());
    }
}
