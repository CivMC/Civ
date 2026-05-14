package net.civmc.zorweth.transfer;

import java.util.Objects;
import java.util.UUID;

public record DestinationRocketTransfer(
    UUID transferId,
    String destinationWorld,
    RocketBlockPosition destinationOrigin,
    int requestedX,
    int requestedZ,
    double fuelKg
) {

    public DestinationRocketTransfer {
        Objects.requireNonNull(transferId, "transferId");
        Objects.requireNonNull(destinationWorld, "destinationWorld");
    }

    public DestinationRocketTransfer withPosition(RocketBlockPosition position) {
        return new DestinationRocketTransfer(transferId, destinationWorld, position, requestedX, requestedZ, fuelKg);
    }
}
