package net.civmc.zorweth.transfer;

import java.util.Objects;
import java.util.UUID;

public record DestinationRocketTransfer(
    UUID transferId,
    RocketTransferState state,
    String destinationWorld,
    RocketBlockPosition destinationOrigin
) {

    public DestinationRocketTransfer {
        Objects.requireNonNull(transferId, "transferId");
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(destinationWorld, "destinationWorld");
        Objects.requireNonNull(destinationOrigin, "destinationOrigin");
    }
}
