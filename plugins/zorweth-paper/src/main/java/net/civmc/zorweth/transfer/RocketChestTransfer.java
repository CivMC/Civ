package net.civmc.zorweth.transfer;

import java.util.Objects;
import java.util.UUID;

public record RocketChestTransfer(
    UUID transferId,
    RocketBlockPosition relativePosition,
    byte[] serializedInventory
) {

    public RocketChestTransfer {
        Objects.requireNonNull(transferId, "transferId");
        Objects.requireNonNull(relativePosition, "relativePosition");
        Objects.requireNonNull(serializedInventory, "serializedInventory");
    }
}
