package net.civmc.namelayer.sync;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record NameLayerInvalidationMessage(
    Set<Integer> affectedGroupIds,
    Set<UUID> affectedDefaultGroupPlayers,
    Set<UUID> affectedAutoAcceptPlayers,
    boolean requiresFullResync
) {

    public NameLayerInvalidationMessage {
        affectedGroupIds = affectedGroupIds == null ? Set.of() : affectedGroupIds;
        affectedDefaultGroupPlayers = affectedDefaultGroupPlayers == null ? Set.of() : affectedDefaultGroupPlayers;
        affectedAutoAcceptPlayers = affectedAutoAcceptPlayers == null ? Set.of() : affectedAutoAcceptPlayers;
        affectedGroupIds = Set.copyOf(validateGroupIds(affectedGroupIds));
        affectedDefaultGroupPlayers = Set.copyOf(validateUuids(affectedDefaultGroupPlayers, "affectedDefaultGroupPlayers"));
        affectedAutoAcceptPlayers = Set.copyOf(validateUuids(affectedAutoAcceptPlayers, "affectedAutoAcceptPlayers"));
        if (!requiresFullResync
            && affectedGroupIds.isEmpty()
            && affectedDefaultGroupPlayers.isEmpty()
            && affectedAutoAcceptPlayers.isEmpty()) {
            throw new IllegalArgumentException("targeted invalidations must affect at least one cache entry");
        }
    }

    public static NameLayerInvalidationMessage targeted(final Set<Integer> affectedGroupIds) {
        return new NameLayerInvalidationMessage(affectedGroupIds, Set.of(), Set.of(), false);
    }

    public static NameLayerInvalidationMessage defaultGroups(final Set<UUID> affectedPlayers) {
        return new NameLayerInvalidationMessage(Set.of(), affectedPlayers, Set.of(), false);
    }

    public static NameLayerInvalidationMessage autoAccepts(final Set<UUID> affectedPlayers) {
        return new NameLayerInvalidationMessage(Set.of(), Set.of(), affectedPlayers, false);
    }

    public static NameLayerInvalidationMessage targeted(
        final Set<Integer> affectedGroupIds,
        final Set<UUID> affectedDefaultGroupPlayers,
        final Set<UUID> affectedAutoAcceptPlayers
    ) {
        return new NameLayerInvalidationMessage(
            affectedGroupIds,
            affectedDefaultGroupPlayers,
            affectedAutoAcceptPlayers,
            false
        );
    }

    public static NameLayerInvalidationMessage fullResync() {
        return new NameLayerInvalidationMessage(Set.of(), Set.of(), Set.of(), true);
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

    private static Set<UUID> validateUuids(final Set<UUID> uuids, final String fieldName) {
        Objects.requireNonNull(uuids, fieldName);
        for (final UUID uuid : uuids) {
            if (uuid == null) {
                throw new IllegalArgumentException(fieldName + " must not contain null values");
            }
        }
        return uuids;
    }
}
