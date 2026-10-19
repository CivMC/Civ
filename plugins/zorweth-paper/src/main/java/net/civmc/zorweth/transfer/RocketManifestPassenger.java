package net.civmc.zorweth.transfer;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.inventory.ItemStack;

public record RocketManifestPassenger(
    UUID playerUuid,
    RocketEntityPosition relativePosition,
    ItemStack[] inventoryContents,
    double health,
    int xpLevel,
    float xpProgress,
    int foodLevel,
    float saturation,
    float exhaustion,
    int heldSlot,
    GameMode gameMode
) {

    public RocketManifestPassenger {
        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(relativePosition, "relativePosition");
        inventoryContents = copyItems(Objects.requireNonNull(inventoryContents, "inventoryContents"));
        Objects.requireNonNull(gameMode, "gameMode");
    }

    @Override
    public ItemStack[] inventoryContents() {
        return copyItems(this.inventoryContents);
    }

    private static ItemStack[] copyItems(final ItemStack[] contents) {
        return Arrays.stream(contents)
            .map(item -> item == null ? null : item.clone())
            .toArray(ItemStack[]::new);
    }
}
