package net.civmc.zorweth.transfer;

import java.util.Arrays;
import java.util.Objects;
import org.bukkit.inventory.ItemStack;

public final class RocketInventorySerialization {

    private RocketInventorySerialization() {
    }

    public static byte[] serializeInventory(final ItemStack[] contents) {
        Objects.requireNonNull(contents, "contents");
        return ItemStack.serializeItemsAsBytes(Arrays.copyOf(contents, contents.length));
    }

    public static ItemStack[] deserializeInventory(final byte[] serializedInventory, final int expectedLength) {
        Objects.requireNonNull(serializedInventory, "serializedInventory");
        if (expectedLength < 0) {
            throw new IllegalArgumentException("expectedLength must not be negative");
        }

        final ItemStack[] contents = ItemStack.deserializeItemsFromBytes(serializedInventory);
        if (contents.length != expectedLength) {
            throw new IllegalArgumentException("Serialized inventory has " + contents.length
                + " slots, expected " + expectedLength);
        }
        return contents;
    }
}
