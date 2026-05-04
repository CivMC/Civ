package net.civmc.namelayer.velocity.write;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.sql.DataSource;
import net.civmc.namelayer.sync.NameLayerWriteFailureCode;
import net.civmc.namelayer.sync.NameLayerWriteRequest;
import net.civmc.namelayer.sync.NameLayerWriteResponse;
import org.slf4j.Logger;

public final class NameLayerWriteCoordinator {

    private static final String LOCK_GROUP = "SELECT 1 FROM faction_id WHERE group_id = ? LIMIT 1 FOR UPDATE";
    private static final String GET_ACTOR_ROLE = "SELECT role FROM faction_member WHERE group_id = ? AND member_name = ?";
    private static final String GET_PERMS_PERMISSION_ID = "SELECT perm_id FROM permissionIdMapping WHERE name = 'PERMS' LIMIT 1";
    private static final String HAS_ROLE_PERMISSION = "SELECT 1 FROM permissionByGroup WHERE group_id = ? AND role = ? AND perm_id = ? LIMIT 1";
    private static final String ADD_PERMISSION = "INSERT IGNORE INTO permissionByGroup(group_id, role, perm_id) VALUES (?, ?, ?)";
    private static final String REMOVE_PERMISSION = "DELETE FROM permissionByGroup WHERE group_id = ? AND role = ? AND perm_id = ?";

    private final DataSource dataSource;
    private final Logger logger;

    public NameLayerWriteCoordinator(final DataSource dataSource, final Logger logger) {
        this.dataSource = dataSource;
        this.logger = logger;
    }

    public NameLayerWriteResponse handle(final NameLayerWriteRequest request) {
        return switch (request.operation()) {
            case ADD_PERMISSION -> handlePermissionWrite(request, ADD_PERMISSION);
            case REMOVE_PERMISSION -> handlePermissionWrite(request, REMOVE_PERMISSION);
            default -> NameLayerWriteResponse.failure(
                request.requestId(),
                NameLayerWriteFailureCode.UNKNOWN_OPERATION,
                "NameLayer proxy write operation is not implemented yet: " + request.operation()
            );
        };
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
                connection.commit();
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
}
