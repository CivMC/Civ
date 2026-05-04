package net.civmc.namelayer.velocity.write;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.sql.DataSource;
import net.civmc.namelayer.sync.NameLayerInvalidationMessage;
import net.civmc.namelayer.sync.NameLayerWriteFailureCode;
import net.civmc.namelayer.sync.NameLayerWriteRequest;
import net.civmc.namelayer.sync.NameLayerWriteResponse;
import net.civmc.namelayer.velocity.rabbitmq.NameLayerInvalidationPublisher;
import org.slf4j.Logger;

public final class NameLayerWriteCoordinator {

    private static final String LOCK_GROUP = "SELECT 1 FROM faction_id WHERE group_id = ? LIMIT 1 FOR UPDATE";
    private static final String GET_ACTOR_ROLE = "SELECT role FROM faction_member WHERE group_id = ? AND member_name = ?";
    private static final String GET_MEMBER_ROLE = "SELECT role FROM faction_member WHERE group_id = ? AND member_name = ?";
    private static final String GET_GROUP_OWNER = "SELECT founder FROM faction_id WHERE group_id = ? LIMIT 1";
    private static final String GET_PERMS_PERMISSION_ID = "SELECT perm_id FROM permissionIdMapping WHERE name = 'PERMS' LIMIT 1";
    private static final String GET_PERMISSION_ID = "SELECT perm_id FROM permissionIdMapping WHERE name = ? LIMIT 1";
    private static final String HAS_ROLE_PERMISSION = "SELECT 1 FROM permissionByGroup WHERE group_id = ? AND role = ? AND perm_id = ? LIMIT 1";
    private static final String ADD_PERMISSION = "INSERT IGNORE INTO permissionByGroup(group_id, role, perm_id) VALUES (?, ?, ?)";
    private static final String REMOVE_PERMISSION = "DELETE FROM permissionByGroup WHERE group_id = ? AND role = ? AND perm_id = ?";
    private static final String SET_MEMBER_ROLE = "UPDATE faction_member SET role = ? WHERE group_id = ? AND member_name = ?";
    private static final String REMOVE_MEMBER = "DELETE FROM faction_member WHERE group_id = ? AND member_name = ?";
    private static final String SET_GROUP_COLOR = "UPDATE faction f JOIN faction_id fi ON fi.group_name = f.group_name SET f.group_color = ? WHERE fi.group_id = ?";
    private static final String SET_GROUP_PASSWORD = "UPDATE faction f JOIN faction_id fi ON fi.group_name = f.group_name SET f.password = ? WHERE fi.group_id = ?";
    private static final String SET_GROUP_DISCIPLINE = "UPDATE faction f JOIN faction_id fi ON fi.group_name = f.group_name SET f.discipline_flags = ? WHERE fi.group_id = ?";
    private static final String SET_GROUP_OWNER = "UPDATE faction f JOIN faction_id fi ON fi.group_name = f.group_name SET f.founder = ? WHERE fi.group_id = ?";
    private static final String SET_OWNER_MEMBER_ROLE = "UPDATE faction_member SET role = 'OWNER' WHERE group_id = ? AND member_name = ?";
    private static final String INCREMENT_CACHE_VERSION = "UPDATE namelayer_cache_version SET cache_version = cache_version + 1 WHERE id = 1";

    private final DataSource dataSource;
    private final NameLayerInvalidationPublisher invalidationPublisher;
    private final Logger logger;

    public NameLayerWriteCoordinator(
        final DataSource dataSource,
        final NameLayerInvalidationPublisher invalidationPublisher,
        final Logger logger
    ) {
        this.dataSource = dataSource;
        this.invalidationPublisher = invalidationPublisher;
        this.logger = logger;
    }

    public NameLayerWriteResponse handle(final NameLayerWriteRequest request) {
        return switch (request.operation()) {
            case ADD_PERMISSION -> handlePermissionWrite(request, ADD_PERMISSION);
            case REMOVE_PERMISSION -> handlePermissionWrite(request, REMOVE_PERMISSION);
            case SET_MEMBER_ROLE -> handleSetMemberRole(request);
            case REMOVE_MEMBER -> handleRemoveMember(request);
            case SET_GROUP_COLOR -> handleMetadataWrite(request, SET_GROUP_COLOR, "EDIT_COLOR", MetadataWrite.ValueType.STRING);
            case SET_GROUP_PASSWORD -> handleMetadataWrite(request, SET_GROUP_PASSWORD, "PASSWORD", MetadataWrite.ValueType.NULLABLE_STRING);
            case SET_GROUP_DISCIPLINE -> handleMetadataWrite(request, SET_GROUP_DISCIPLINE, null, MetadataWrite.ValueType.BOOLEAN_INT);
            case SET_GROUP_OWNER -> handleSetGroupOwner(request);
            default -> NameLayerWriteResponse.failure(
                request.requestId(),
                NameLayerWriteFailureCode.UNKNOWN_OPERATION,
                "NameLayer proxy write operation is not implemented yet: " + request.operation()
            );
        };
    }

    private NameLayerWriteResponse handleMetadataWrite(
        final NameLayerWriteRequest request,
        final String sql,
        final String permissionName,
        final MetadataWrite.ValueType valueType
    ) {
        final MetadataWrite metadataWrite;
        try {
            metadataWrite = MetadataWrite.from(request.arguments(), valueType);
        } catch (final IllegalArgumentException exception) {
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.INVALID_REQUEST, exception.getMessage());
        }
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (!lockGroup(connection, metadataWrite.groupId())) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.GROUP_NOT_FOUND, "Group does not exist");
                }
                if (!metadataWrite.adminOverride() && permissionName != null && !hasRoleEditAccess(connection, metadataWrite.groupId(), request.actorUuid(), permissionName)) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.AUTHORIZATION_FAILED, "Actor does not have metadata edit access");
                }
                if (!metadataWrite.adminOverride() && permissionName == null) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.AUTHORIZATION_FAILED, "Metadata write requires admin authorization");
                }
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    metadataWrite.bindValue(statement, 1);
                    statement.setInt(2, metadataWrite.groupId());
                    statement.executeUpdate();
                }
                incrementCacheVersion(connection);
                connection.commit();
                publishInvalidation(metadataWrite.groupId());
                return NameLayerWriteResponse.success(request.requestId(), Set.of(metadataWrite.groupId()));
            } catch (final SQLException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (final SQLException exception) {
            logger.error("NameLayer metadata write failed", exception);
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.DATABASE_UNAVAILABLE, "Database write failed");
        }
    }

    private NameLayerWriteResponse handleSetGroupOwner(final NameLayerWriteRequest request) {
        final OwnerWrite ownerWrite;
        try {
            ownerWrite = OwnerWrite.from(request.arguments());
        } catch (final IllegalArgumentException exception) {
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.INVALID_REQUEST, exception.getMessage());
        }
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (!lockGroup(connection, ownerWrite.groupId())) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.GROUP_NOT_FOUND, "Group does not exist");
                }
                if (!ownerWrite.adminOverride() && !isGroupOwner(connection, ownerWrite.groupId(), request.actorUuid())) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.AUTHORIZATION_FAILED, "Only the owner can transfer group ownership");
                }
                if (getMemberRole(connection, ownerWrite.groupId(), ownerWrite.ownerUuid()) == null) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.MEMBER_NOT_FOUND, "New owner must be a group member");
                }
                try (PreparedStatement statement = connection.prepareStatement(SET_GROUP_OWNER)) {
                    statement.setString(1, ownerWrite.ownerUuid().toString());
                    statement.setInt(2, ownerWrite.groupId());
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(SET_OWNER_MEMBER_ROLE)) {
                    statement.setInt(1, ownerWrite.groupId());
                    statement.setString(2, ownerWrite.ownerUuid().toString());
                    statement.executeUpdate();
                }
                incrementCacheVersion(connection);
                connection.commit();
                publishInvalidation(ownerWrite.groupId());
                return NameLayerWriteResponse.success(request.requestId(), Set.of(ownerWrite.groupId()));
            } catch (final SQLException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (final SQLException exception) {
            logger.error("NameLayer owner transfer failed", exception);
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.DATABASE_UNAVAILABLE, "Database write failed");
        }
    }

    private NameLayerWriteResponse handleSetMemberRole(final NameLayerWriteRequest request) {
        final MemberRoleWrite memberWrite;
        try {
            memberWrite = MemberRoleWrite.from(request.arguments());
        } catch (final IllegalArgumentException exception) {
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.INVALID_REQUEST, exception.getMessage());
        }

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                final String currentRole = getMemberRole(connection, memberWrite.groupId(), memberWrite.memberUuid());
                final NameLayerWriteResponse validationFailure = validateMemberWriteAccess(connection, request, memberWrite.groupId(), memberWrite.memberUuid(), currentRole, false);
                if (validationFailure != null) {
                    connection.rollback();
                    return validationFailure;
                }
                if (!currentRole.equals(memberWrite.role()) && !hasRoleEditAccess(connection, memberWrite.groupId(), request.actorUuid(), memberWrite.role())) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.AUTHORIZATION_FAILED, "Actor does not have member edit access");
                }
                try (PreparedStatement statement = connection.prepareStatement(SET_MEMBER_ROLE)) {
                    statement.setString(1, memberWrite.role());
                    statement.setInt(2, memberWrite.groupId());
                    statement.setString(3, memberWrite.memberUuid().toString());
                    if (statement.executeUpdate() == 0) {
                        connection.rollback();
                        return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.MEMBER_NOT_FOUND, "Member does not exist");
                    }
                }
                incrementCacheVersion(connection);
                connection.commit();
                publishInvalidation(memberWrite.groupId());
                return NameLayerWriteResponse.success(request.requestId(), Set.of(memberWrite.groupId()));
            } catch (final SQLException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (final SQLException exception) {
            logger.error("NameLayer member role write failed", exception);
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.DATABASE_UNAVAILABLE, "Database write failed");
        }
    }

    private NameLayerWriteResponse handleRemoveMember(final NameLayerWriteRequest request) {
        final MemberWrite memberWrite;
        try {
            memberWrite = MemberWrite.from(request.arguments());
        } catch (final IllegalArgumentException exception) {
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.INVALID_REQUEST, exception.getMessage());
        }

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                final String memberRole = getMemberRole(connection, memberWrite.groupId(), memberWrite.memberUuid());
                final NameLayerWriteResponse validationFailure = validateMemberWriteAccess(connection, request, memberWrite.groupId(), memberWrite.memberUuid(), memberRole, true);
                if (validationFailure != null) {
                    connection.rollback();
                    return validationFailure;
                }
                try (PreparedStatement statement = connection.prepareStatement(REMOVE_MEMBER)) {
                    statement.setInt(1, memberWrite.groupId());
                    statement.setString(2, memberWrite.memberUuid().toString());
                    if (statement.executeUpdate() == 0) {
                        connection.rollback();
                        return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.MEMBER_NOT_FOUND, "Member does not exist");
                    }
                }
                incrementCacheVersion(connection);
                connection.commit();
                publishInvalidation(memberWrite.groupId());
                return NameLayerWriteResponse.success(request.requestId(), Set.of(memberWrite.groupId()));
            } catch (final SQLException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (final SQLException exception) {
            logger.error("NameLayer member removal failed", exception);
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.DATABASE_UNAVAILABLE, "Database write failed");
        }
    }

    private NameLayerWriteResponse validateMemberWriteAccess(
        final Connection connection,
        final NameLayerWriteRequest request,
        final int groupId,
        final UUID memberUuid,
        final String targetRole,
        final boolean allowSelfRemoval
    ) throws SQLException {
        if (!lockGroup(connection, groupId)) {
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.GROUP_NOT_FOUND, "Group does not exist");
        }
        if (targetRole == null) {
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.MEMBER_NOT_FOUND, "Member does not exist");
        }
        if (isGroupOwner(connection, groupId, memberUuid)) {
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.AUTHORIZATION_FAILED, "Group owner cannot be modified through member writes");
        }
        if (!allowSelfRemoval || !request.actorUuid().equals(memberUuid)) {
            if (!hasRoleEditAccess(connection, groupId, request.actorUuid(), targetRole)) {
                return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.AUTHORIZATION_FAILED, "Actor does not have member edit access");
            }
        }
        return null;
    }

    private NameLayerWriteResponse handlePermissionWrite(final NameLayerWriteRequest request, final String sql) {
        final PermissionWrite permissionWrite;
        try {
            permissionWrite = PermissionWrite.from(request.arguments());
        } catch (final IllegalArgumentException exception) {
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.INVALID_REQUEST, exception.getMessage());
        }

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (!lockGroup(connection, permissionWrite.groupId())) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(
                        request.requestId(),
                        NameLayerWriteFailureCode.GROUP_NOT_FOUND,
                        "Group does not exist"
                    );
                }
                if (!hasPermissionEditAccess(connection, permissionWrite.groupId(), request.actorUuid())) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(
                        request.requestId(),
                        NameLayerWriteFailureCode.AUTHORIZATION_FAILED,
                        "Actor does not have permission edit access"
                    );
                }
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, permissionWrite.groupId());
                    statement.setString(2, permissionWrite.role());
                    statement.setInt(3, permissionWrite.permissionId());
                    statement.executeUpdate();
                }
                incrementCacheVersion(connection);
                connection.commit();
                publishInvalidation(permissionWrite.groupId());
                return NameLayerWriteResponse.success(request.requestId(), Set.of(permissionWrite.groupId()));
            } catch (final SQLException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (final SQLException exception) {
            logger.error("NameLayer permission write failed", exception);
            return NameLayerWriteResponse.failure(
                request.requestId(),
                NameLayerWriteFailureCode.DATABASE_UNAVAILABLE,
                "Database write failed"
            );
        }
    }

    private void publishInvalidation(final int groupId) {
        final boolean published = invalidationPublisher.publish(NameLayerInvalidationMessage.targeted(Set.of(groupId)));
        if (!published) {
            logger.error("NameLayer write committed, but failed to publish invalidation for group {}", groupId);
        }
    }

    private void incrementCacheVersion(final Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INCREMENT_CACHE_VERSION)) {
            statement.executeUpdate();
        }
    }

    private boolean hasPermissionEditAccess(final Connection connection, final int groupId, final UUID actorUuid) throws SQLException {
        final String actorRole = getActorRole(connection, groupId, actorUuid);
        if (actorRole == null) {
            return false;
        }
        final Integer permsPermissionId = getPermsPermissionId(connection);
        if (permsPermissionId == null) {
            logger.warn("NameLayer PERMS permission is not registered; denying proxy permission write");
            return false;
        }
        try (PreparedStatement statement = connection.prepareStatement(HAS_ROLE_PERMISSION)) {
            statement.setInt(1, groupId);
            statement.setString(2, actorRole);
            statement.setInt(3, permsPermissionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private boolean hasRoleEditAccess(final Connection connection, final int groupId, final UUID actorUuid, final String permissionName) throws SQLException {
        final String actorRole = getActorRole(connection, groupId, actorUuid);
        if (actorRole == null) {
            return false;
        }
        final Integer permissionId = getPermissionId(connection, permissionName);
        if (permissionId == null) {
            logger.warn("NameLayer {} permission is not registered; denying proxy member write", permissionName);
            return false;
        }
        try (PreparedStatement statement = connection.prepareStatement(HAS_ROLE_PERMISSION)) {
            statement.setInt(1, groupId);
            statement.setString(2, actorRole);
            statement.setInt(3, permissionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private String getActorRole(final Connection connection, final int groupId, final UUID actorUuid) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(GET_ACTOR_ROLE)) {
            statement.setInt(1, groupId);
            statement.setString(2, actorUuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return resultSet.getString(1);
            }
        }
    }

    private Integer getPermsPermissionId(final Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(GET_PERMS_PERMISSION_ID);
             ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                return null;
            }
            return resultSet.getInt(1);
        }
    }

    private Integer getPermissionId(final Connection connection, final String permissionName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(GET_PERMISSION_ID)) {
            statement.setString(1, permissionName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return resultSet.getInt(1);
            }
        }
    }

    private String getMemberRole(final Connection connection, final int groupId, final UUID memberUuid) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(GET_MEMBER_ROLE)) {
            statement.setInt(1, groupId);
            statement.setString(2, memberUuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return resultSet.getString(1);
            }
        }
    }

    private boolean isGroupOwner(final Connection connection, final int groupId, final UUID memberUuid) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(GET_GROUP_OWNER)) {
            statement.setInt(1, groupId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && memberUuid.toString().equals(resultSet.getString(1));
            }
        }
    }

    private boolean lockGroup(final Connection connection, final int groupId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(LOCK_GROUP)) {
            statement.setInt(1, groupId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private record PermissionWrite(int groupId, String role, int permissionId) {

        private static PermissionWrite from(final Map<String, String> arguments) {
            final int groupId = parsePositiveInt(arguments, "groupId");
            final String role = requireNonBlank(arguments, "role");
            validateRole(role);
            final int permissionId = parsePositiveInt(arguments, "permissionId");
            return new PermissionWrite(groupId, role, permissionId);
        }

        private static void validateRole(final String role) {
            if (!Set.of("MEMBERS", "MODS", "ADMINS", "OWNER").contains(role)) {
                throw new IllegalArgumentException("role must be MEMBERS, MODS, ADMINS, or OWNER");
            }
        }

        private static int parsePositiveInt(final Map<String, String> arguments, final String key) {
            final String value = requireNonBlank(arguments, key);
            try {
                final int parsed = Integer.parseInt(value);
                if (parsed <= 0) {
                    throw new IllegalArgumentException(key + " must be positive");
                }
                return parsed;
            } catch (final NumberFormatException exception) {
                throw new IllegalArgumentException(key + " must be an integer", exception);
            }
        }

        private static String requireNonBlank(final Map<String, String> arguments, final String key) {
            final String value = arguments.get(key);
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Missing required argument: " + key);
            }
            return value.trim();
        }
    }

    private record MemberWrite(int groupId, UUID memberUuid) {

        private static MemberWrite from(final Map<String, String> arguments) {
            final int groupId = PermissionWrite.parsePositiveInt(arguments, "groupId");
            final UUID memberUuid = parseUuid(arguments, "memberUuid");
            return new MemberWrite(groupId, memberUuid);
        }

        private static UUID parseUuid(final Map<String, String> arguments, final String key) {
            final String value = PermissionWrite.requireNonBlank(arguments, key);
            try {
                return UUID.fromString(value);
            } catch (final IllegalArgumentException exception) {
                throw new IllegalArgumentException(key + " must be a UUID", exception);
            }
        }
    }

    private record MemberRoleWrite(int groupId, UUID memberUuid, String role) {

        private static MemberRoleWrite from(final Map<String, String> arguments) {
            final MemberWrite memberWrite = MemberWrite.from(arguments);
            final String role = PermissionWrite.requireNonBlank(arguments, "role");
            PermissionWrite.validateRole(role);
            if ("OWNER".equals(role)) {
                throw new IllegalArgumentException("role cannot be OWNER for member role writes");
            }
            return new MemberRoleWrite(memberWrite.groupId(), memberWrite.memberUuid(), role);
        }
    }

    private record MetadataWrite(int groupId, String value, boolean adminOverride, ValueType valueType) {

        private static MetadataWrite from(final Map<String, String> arguments, final ValueType valueType) {
            final int groupId = PermissionWrite.parsePositiveInt(arguments, "groupId");
            final boolean adminOverride = Boolean.parseBoolean(arguments.getOrDefault("adminOverride", "false"));
            final String value = switch (valueType) {
                case STRING -> PermissionWrite.requireNonBlank(arguments, "value");
                case NULLABLE_STRING -> Boolean.parseBoolean(arguments.getOrDefault("hasValue", "true"))
                    ? arguments.getOrDefault("value", "")
                    : null;
                case BOOLEAN_INT -> {
                    final String rawValue = PermissionWrite.requireNonBlank(arguments, "value");
                    if (!"true".equalsIgnoreCase(rawValue) && !"false".equalsIgnoreCase(rawValue)) {
                        throw new IllegalArgumentException("value must be true or false");
                    }
                    yield rawValue;
                }
            };
            return new MetadataWrite(groupId, value, adminOverride, valueType);
        }

        private void bindValue(final PreparedStatement statement, final int index) throws SQLException {
            switch (valueType) {
                case STRING, NULLABLE_STRING -> statement.setString(index, value);
                case BOOLEAN_INT -> statement.setInt(index, Boolean.parseBoolean(value) ? 1 : 0);
            }
        }

        private enum ValueType {
            STRING,
            NULLABLE_STRING,
            BOOLEAN_INT
        }
    }

    private record OwnerWrite(int groupId, UUID ownerUuid, boolean adminOverride) {

        private static OwnerWrite from(final Map<String, String> arguments) {
            final int groupId = PermissionWrite.parsePositiveInt(arguments, "groupId");
            final UUID ownerUuid = MemberWrite.parseUuid(arguments, "ownerUuid");
            final boolean adminOverride = Boolean.parseBoolean(arguments.getOrDefault("adminOverride", "false"));
            return new OwnerWrite(groupId, ownerUuid, adminOverride);
        }
    }
}
