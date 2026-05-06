package net.civmc.namelayer.sync;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record NameLayerInvalidationMessage(
    Set<Integer> affectedGroupIds,
    Map<UUID, String> defaultGroupAssignments,
    Set<UUID> defaultGroupClears,
    Map<UUID, Boolean> autoAcceptAssignments,
    boolean requiresFullResync
) {

    public NameLayerInvalidationMessage {
        affectedGroupIds = affectedGroupIds == null ? Set.of() : Set.copyOf(validateGroupIds(affectedGroupIds));
        defaultGroupAssignments = defaultGroupAssignments == null
            ? Map.of()
            : Map.copyOf(validateAssignments(defaultGroupAssignments, "defaultGroupAssignments"));
        defaultGroupClears = defaultGroupClears == null
            ? Set.of()
            : Set.copyOf(validateUuids(defaultGroupClears, "defaultGroupClears"));
        autoAcceptAssignments = autoAcceptAssignments == null
            ? Map.of()
            : Map.copyOf(validateBooleanAssignments(autoAcceptAssignments));
        if (!requiresFullResync
            && affectedGroupIds.isEmpty()
            && defaultGroupAssignments.isEmpty()
            && defaultGroupClears.isEmpty()
            && autoAcceptAssignments.isEmpty()) {
            throw new IllegalArgumentException("targeted invalidations must affect at least one cache entry");
        }
    }

    public static NameLayerInvalidationMessage targeted(final Set<Integer> affectedGroupIds) {
        return new NameLayerInvalidationMessage(affectedGroupIds, Map.of(), Set.of(), Map.of(), false);
    }

    public static NameLayerInvalidationMessage defaultGroupAssignment(final UUID player, final String groupName) {
        return new NameLayerInvalidationMessage(Set.of(), Map.of(player, groupName), Set.of(), Map.of(), false);
    }

    public static NameLayerInvalidationMessage defaultGroupClear(final UUID player) {
        return new NameLayerInvalidationMessage(Set.of(), Map.of(), Set.of(player), Map.of(), false);
    }

    public static NameLayerInvalidationMessage autoAccept(final UUID player, final boolean autoAccept) {
        return new NameLayerInvalidationMessage(Set.of(), Map.of(), Set.of(), Map.of(player, autoAccept), false);
    }

    public static NameLayerInvalidationMessage withAffected(
        final Set<Integer> affectedGroupIds,
        final Map<UUID, String> defaultGroupAssignments,
        final Set<UUID> defaultGroupClears,
        final Map<UUID, Boolean> autoAcceptAssignments
    ) {
        return new NameLayerInvalidationMessage(
            affectedGroupIds,
            defaultGroupAssignments,
            defaultGroupClears,
            autoAcceptAssignments,
            false
        );
    }

    public static NameLayerInvalidationMessage fullResync() {
        return new NameLayerInvalidationMessage(Set.of(), Map.of(), Set.of(), Map.of(), true);
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

    private static Map<UUID, String> validateAssignments(final Map<UUID, String> assignments, final String fieldName) {
        Objects.requireNonNull(assignments, fieldName);
        final Map<UUID, String> validated = new HashMap<>();
        for (final Map.Entry<UUID, String> entry : assignments.entrySet()) {
            if (entry.getKey() == null) {
                throw new IllegalArgumentException(fieldName + " must not contain null keys");
            }
            if (entry.getValue() == null || entry.getValue().isBlank()) {
                throw new IllegalArgumentException(fieldName + " must not contain null or blank values");
            }
            validated.put(entry.getKey(), entry.getValue());
        }
        return validated;
    }

    private static Map<UUID, Boolean> validateBooleanAssignments(final Map<UUID, Boolean> assignments) {
        Objects.requireNonNull(assignments, "autoAcceptAssignments");
        final Map<UUID, Boolean> validated = new HashMap<>();
        for (final Map.Entry<UUID, Boolean> entry : assignments.entrySet()) {
            if (entry.getKey() == null) {
                throw new IllegalArgumentException("autoAcceptAssignments must not contain null keys");
            }
            if (entry.getValue() == null) {
                throw new IllegalArgumentException("autoAcceptAssignments must not contain null values");
            }
            validated.put(entry.getKey(), entry.getValue());
        }
        return validated;
    }
}
