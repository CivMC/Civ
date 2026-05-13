package net.civmc.zorweth.transfer;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public record RocketChestTransfer(
    UUID transferId,
    RocketBlockPosition relativePosition,
    byte[] serializedInventory,
    RocketTransferCargoState state
) {

    public RocketChestTransfer {
        Objects.requireNonNull(transferId, "transferId");
        Objects.requireNonNull(relativePosition, "relativePosition");
        Objects.requireNonNull(serializedInventory, "serializedInventory");
        Objects.requireNonNull(state, "state");
        serializedInventory = Arrays.copyOf(serializedInventory, serializedInventory.length);
    }

    @Override
    public byte[] serializedInventory() {
        return Arrays.copyOf(this.serializedInventory, this.serializedInventory.length);
    }
}
