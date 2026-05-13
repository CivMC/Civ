package net.civmc.zorweth.transfer;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record RocketTransfer(
    UUID transferId,
    RocketTransferState state,
    String sourceServer,
    String destinationServer,
    String sourceWorld,
    String destinationWorld,
    RocketBlockPosition sourceOrigin,
    RocketBlockPosition destinationOrigin,
    int destinationRequestedX,
    int destinationRequestedZ,
    Instant createdAt,
    Instant updatedAt
) {

    public RocketTransfer {
        Objects.requireNonNull(transferId, "transferId");
        Objects.requireNonNull(state, "state");
        sourceServer = requireNonBlank(sourceServer, "sourceServer");
        destinationServer = requireNonBlank(destinationServer, "destinationServer");
        sourceWorld = requireNonBlank(sourceWorld, "sourceWorld");
        destinationWorld = requireNonBlank(destinationWorld, "destinationWorld");
        Objects.requireNonNull(sourceOrigin, "sourceOrigin");
        Objects.requireNonNull(destinationOrigin, "destinationOrigin");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(updatedAt, "updatedAt");
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
