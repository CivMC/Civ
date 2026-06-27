package net.civmc.zorweth.transfer;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record RocketManifest(
    UUID transferId,
    String sourceServer,
    String destinationServer,
    String sourceWorld,
    String destinationWorld,
    RocketBlockPosition sourceOrigin,
    int destinationRequestedX,
    int destinationRequestedZ,
    UUID pilotUuid,
    Integer flightComputerGroupId,
    List<RocketManifestPassenger> passengers,
    List<RocketManifestChest> chests,
    double fuelKg,
    int usesRemaining
) {

    public RocketManifest {
        Objects.requireNonNull(transferId, "transferId");
        sourceServer = requireNonBlank(sourceServer, "sourceServer");
        destinationServer = requireNonBlank(destinationServer, "destinationServer");
        sourceWorld = requireNonBlank(sourceWorld, "sourceWorld");
        destinationWorld = requireNonBlank(destinationWorld, "destinationWorld");
        Objects.requireNonNull(sourceOrigin, "sourceOrigin");
        Objects.requireNonNull(pilotUuid, "pilotUuid");
        passengers = List.copyOf(Objects.requireNonNull(passengers, "passengers"));
        chests = List.copyOf(Objects.requireNonNull(chests, "chests"));
    }

    private static String requireNonBlank(final String value, final String name) {
        Objects.requireNonNull(value, name);
        final String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return trimmed;
    }
}
