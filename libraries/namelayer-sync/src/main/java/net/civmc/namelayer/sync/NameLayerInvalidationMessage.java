package net.civmc.namelayer.sync;

import java.util.Objects;
import java.util.Set;

public record NameLayerInvalidationMessage(Set<Integer> affectedGroupIds, boolean requiresFullResync) {

    public NameLayerInvalidationMessage {
        affectedGroupIds = Set.copyOf(validateGroupIds(affectedGroupIds));
        if (!requiresFullResync && affectedGroupIds.isEmpty()) {
            throw new IllegalArgumentException("affectedGroupIds must not be empty unless requiresFullResync is true");
        }
    }

    public static NameLayerInvalidationMessage targeted(final Set<Integer> affectedGroupIds) {
        return new NameLayerInvalidationMessage(affectedGroupIds, false);
    }

    public static NameLayerInvalidationMessage fullResync() {
        return new NameLayerInvalidationMessage(Set.of(), true);
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
