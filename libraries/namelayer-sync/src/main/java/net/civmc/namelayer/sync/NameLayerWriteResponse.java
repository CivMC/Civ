package net.civmc.namelayer.sync;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record NameLayerWriteResponse(
    UUID requestId,
    boolean success,
    NameLayerWriteFailureCode failureCode,
    String message,
    Set<Integer> affectedGroupIds,
    boolean requiresFullResync,
    long completedAtEpochMillis
) {

    public NameLayerWriteResponse {
        Objects.requireNonNull(requestId, "requestId");
        if (success && failureCode != null) {
            throw new IllegalArgumentException("successful responses must not include a failureCode");
        }
        if (!success) {
            Objects.requireNonNull(failureCode, "failureCode");
        }
        message = message == null ? "" : message;
        affectedGroupIds = Set.copyOf(validateGroupIds(affectedGroupIds));
        if (completedAtEpochMillis <= 0) {
            throw new IllegalArgumentException("completedAtEpochMillis must be positive");
        }
    }

    public static NameLayerWriteResponse success(final UUID requestId, final Set<Integer> affectedGroupIds) {
        return new NameLayerWriteResponse(
            requestId,
            true,
            null,
            "",
            affectedGroupIds,
            false,
            System.currentTimeMillis()
        );
    }

    public static NameLayerWriteResponse failure(
        final UUID requestId,
        final NameLayerWriteFailureCode failureCode,
        final String message
    ) {
        return new NameLayerWriteResponse(
            requestId,
            false,
            failureCode,
            message,
            Set.of(),
            false,
            System.currentTimeMillis()
        );
    }

    private static Set<Integer> validateGroupIds(final Set<Integer> groupIds) {
        Objects.requireNonNull(groupIds, "affectedGroupIds");
        for (final Integer groupId : groupIds) {
            if (groupId == null || groupId <= 0) {
                throw new IllegalArgumentException("affectedGroupIds must contain only positive group IDs");
            }
        }
        return groupIds;
    }
}
