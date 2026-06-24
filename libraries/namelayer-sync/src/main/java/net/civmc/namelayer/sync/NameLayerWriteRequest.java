package net.civmc.namelayer.sync;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record NameLayerWriteRequest(
    UUID requestId,
    String originServerId,
    UUID actorUuid,
    NameLayerWriteOperation operation,
    Map<String, String> arguments,
    long createdAtEpochMillis
) {

    public NameLayerWriteRequest {
        Objects.requireNonNull(requestId, "requestId");
        originServerId = requireNonBlank(originServerId, "originServerId");
        Objects.requireNonNull(actorUuid, "actorUuid");
        Objects.requireNonNull(operation, "operation");
        arguments = Map.copyOf(Objects.requireNonNull(arguments, "arguments"));
        if (createdAtEpochMillis <= 0) {
            throw new IllegalArgumentException("createdAtEpochMillis must be positive");
        }
    }

    public static NameLayerWriteRequest create(
        final String originServerId,
        final UUID actorUuid,
        final NameLayerWriteOperation operation,
        final Map<String, String> arguments
    ) {
        return new NameLayerWriteRequest(
            UUID.randomUUID(),
            originServerId,
            actorUuid,
            operation,
            new LinkedHashMap<>(arguments),
            System.currentTimeMillis()
        );
    }

    private static String requireNonBlank(final String value, final String fieldName) {
        Objects.requireNonNull(value, fieldName);
        final String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return trimmed;
    }
}
