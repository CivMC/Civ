package net.civmc.zorweth.transfer;

import java.util.Arrays;
import java.util.Objects;
import org.bukkit.inventory.ItemStack;

public record RocketManifestChest(
    RocketBlockPosition relativePosition,
    ItemStack[] contents
) {

    public RocketManifestChest {
        Objects.requireNonNull(relativePosition, "relativePosition");
        contents = copyItems(Objects.requireNonNull(contents, "contents"));
    }

    @Override
    public ItemStack[] contents() {
        return copyItems(this.contents);
    }

    private static ItemStack[] copyItems(final ItemStack[] contents) {
        return Arrays.stream(contents)
            .map(item -> item == null ? null : item.clone())
            .toArray(ItemStack[]::new);
    }
}
