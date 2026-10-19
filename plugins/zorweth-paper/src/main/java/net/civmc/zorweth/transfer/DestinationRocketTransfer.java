package net.civmc.zorweth.transfer;

import java.util.Objects;
import java.util.UUID;

public record DestinationRocketTransfer(
    UUID transferId,
    String destinationWorld,
    RocketBlockPosition destinationOrigin,
    int requestedX,
    int requestedZ,
    UUID pilotUuid,
    Integer flightComputerGroupId,
    double fuelKg,
    int usesRemaining
) {

    public DestinationRocketTransfer {
        Objects.requireNonNull(transferId, "transferId");
        Objects.requireNonNull(destinationWorld, "destinationWorld");
        Objects.requireNonNull(pilotUuid, "pilotUuid");
    }

    public DestinationRocketTransfer withPosition(final RocketBlockPosition position) {
        return new DestinationRocketTransfer(transferId, destinationWorld, position, requestedX, requestedZ,
            pilotUuid, flightComputerGroupId, fuelKg, usesRemaining);
    }
}
