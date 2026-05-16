package net.civmc.namelayer.velocity.write;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.sql.DataSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.civmc.namelayer.sync.NameLayerInvalidationMessage;
import net.civmc.namelayer.sync.NameLayerWriteFailureCode;
import net.civmc.namelayer.sync.NameLayerWriteRequest;
import net.civmc.namelayer.sync.NameLayerWriteResponse;
import net.civmc.namelayer.velocity.rabbitmq.NameLayerInvalidationPublisher;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

public final class NameLayerWriteCoordinator {

    private static final String LOCK_GROUP = "SELECT 1 FROM faction_id WHERE group_id = ? LIMIT 1 FOR UPDATE";
    private static final String GET_ACTOR_ROLE = "SELECT role FROM faction_member WHERE group_id = ? AND member_name = ?";
    private static final String GET_MEMBER_ROLE = "SELECT role FROM faction_member WHERE group_id = ? AND member_name = ?";
    private static final String GET_GROUP_OWNER = "SELECT founder FROM faction f JOIN faction_id fi ON fi.group_name = f.group_name WHERE fi.group_id = ? LIMIT 1";
    private static final String HAS_ROLE_PERMISSION = "SELECT 1 FROM permission_by_group_name WHERE group_id = ? AND role = ? AND permission_name = ? LIMIT 1";
    private static final String ADD_PERMISSION = "INSERT IGNORE INTO permission_by_group_name(group_id, role, permission_name) VALUES (?, ?, ?)";
    private static final String REMOVE_PERMISSION = "DELETE FROM permission_by_group_name WHERE group_id = ? AND role = ? AND permission_name = ?";
    private static final String SET_MEMBER_ROLE = "UPDATE faction_member SET role = ? WHERE group_id = ? AND member_name = ?";
    private static final String REMOVE_MEMBER = "DELETE FROM faction_member WHERE group_id = ? AND member_name = ?";
    private static final String SET_GROUP_COLOR = "UPDATE faction f JOIN faction_id fi ON fi.group_name = f.group_name SET f.group_color = ? WHERE fi.group_id = ?";
    private static final String SET_GROUP_PASSWORD = "UPDATE faction f JOIN faction_id fi ON fi.group_name = f.group_name SET f.password = ? WHERE fi.group_id = ?";
    private static final String SET_GROUP_DISCIPLINE = "UPDATE faction f JOIN faction_id fi ON fi.group_name = f.group_name SET f.discipline_flags = ? WHERE fi.group_id = ?";
    private static final String SET_GROUP_OWNER = "UPDATE faction f JOIN faction_id fi ON fi.group_name = f.group_name SET f.founder = ? WHERE fi.group_id = ?";
    private static final String SET_OWNER_MEMBER_ROLE = "UPDATE faction_member SET role = 'OWNER' WHERE group_id = ? AND member_name = ?";
    private static final String CREATE_GROUP = "CALL createGroup(?, ?, ?, ?)";
    private static final String COUNT_OWNED_GROUPS = "SELECT COUNT(*) FROM faction WHERE founder = ?";
    private static final String ADD_PERMISSION_BY_NAME = "INSERT IGNORE INTO permission_by_group_name(group_id, role, permission_name) VALUES (?, ?, ?)";
    private static final String GET_DEFAULT_GROUP_ID = "SELECT dg.defaultgroup, fi.group_id FROM default_group dg "
        + "INNER JOIN faction_id fi ON fi.group_name = dg.defaultgroup WHERE dg.uuid = ? LIMIT 1 FOR UPDATE";
    private static final String GET_PLAYER_LOCK = "SELECT GET_LOCK(?, 10)";
    private static final String RELEASE_PLAYER_LOCK = "SELECT RELEASE_LOCK(?)";
    private static final String IS_GROUP_MEMBER = "SELECT 1 FROM faction_member WHERE group_id = ? AND member_name = ? LIMIT 1";
    private static final String GET_GROUP_PASSWORD = "SELECT f.password FROM faction f JOIN faction_id fi ON fi.group_name = f.group_name WHERE fi.group_id = ? LIMIT 1";
    private static final String ADD_BLACKLIST = "INSERT IGNORE INTO blacklist(group_id, member_name) VALUES (?, ?)";
    private static final String REMOVE_BLACKLIST = "DELETE FROM blacklist WHERE group_id = ? AND member_name = ?";
    private static final String IS_BLACKLISTED = "SELECT 1 FROM blacklist WHERE group_id = ? AND member_name = ? LIMIT 1";
    private static final String IS_AUTO_ACCEPT = "SELECT 1 FROM toggleAutoAccept WHERE uuid = ? LIMIT 1";
    private static final String ADD_AUTO_ACCEPT = "INSERT IGNORE INTO toggleAutoAccept(uuid) VALUES (?)";
    private static final String REMOVE_AUTO_ACCEPT = "DELETE FROM toggleAutoAccept WHERE uuid = ?";
    private static final String ADD_MEMBER = "INSERT INTO faction_member(group_id, member_name, role) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE role = VALUES(role)";
    private static final String ADD_INVITATION = "INSERT INTO group_invitation(uuid, groupName, role) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE role = VALUES(role), date = NOW()";
    private static final String GET_INVITATION_ROLE = "SELECT role FROM group_invitation WHERE uuid = ? AND groupName = ? LIMIT 1";
    private static final String REMOVE_INVITATION = "DELETE FROM group_invitation WHERE uuid = ? AND groupName = ?";
    private static final String GET_PLAYER_NAME = "SELECT player FROM Name_player WHERE uuid = ? LIMIT 1";
    private static final String SET_DEFAULT_GROUP = "INSERT INTO default_group(uuid, defaultgroup) VALUES (?, ?) ON DUPLICATE KEY UPDATE defaultgroup = VALUES(defaultgroup)";
    private static final String GET_GROUP_NAME = "SELECT group_name FROM faction_id WHERE group_id = ? LIMIT 1";
    private static final String DELETE_GROUP = "CALL deletegroupfromtable(?, ?)";
    private static final String INCREMENT_CACHE_VERSION = "UPDATE namelayer_cache_version SET cache_version = cache_version + 1 WHERE id = 1";

    private final DataSource dataSource;
    private final NameLayerInvalidationPublisher invalidationPublisher;
    private final ProxyServer proxyServer;
    private final Logger logger;
    private final Map<String, String> serverDatabases;

    public NameLayerWriteCoordinator(
        final DataSource dataSource,
        final NameLayerInvalidationPublisher invalidationPublisher,
        final ProxyServer proxyServer,
        final Logger logger,
        final Map<String, String> serverDatabases) {
        this.dataSource = dataSource;
        this.invalidationPublisher = invalidationPublisher;
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.serverDatabases = serverDatabases;
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
            case CREATE_GROUP -> handleCreateGroup(request);
            case ENSURE_NEWFRIEND_GROUP -> handleEnsureNewfriendGroup(request);
            case DELETE_GROUP -> handleDeleteGroup(request);
            case ADD_MEMBER -> handleAddMember(request);
            case JOIN_GROUP -> handleJoinGroup(request);
            case ADD_BLACKLIST -> handleBlacklistWrite(request, ADD_BLACKLIST, true);
            case REMOVE_BLACKLIST -> handleBlacklistWrite(request, REMOVE_BLACKLIST, false);
            case ADD_INVITATION -> handleAddInvitation(request);
            case REMOVE_INVITATION -> handleRemoveInvitation(request);
            case ACCEPT_INVITATION -> handleAcceptInvitation(request);
            case SET_DEFAULT_GROUP -> handleSetDefaultGroup(request);
            case SET_AUTO_ACCEPT -> handleSetAutoAccept(request);
            default -> NameLayerWriteResponse.failure(
                request.requestId(),
                NameLayerWriteFailureCode.UNKNOWN_OPERATION,
                "NameLayer proxy write operation is not implemented yet: " + request.operation()
            );
        };
    }

    private Connection getConnection(NameLayerWriteRequest request) throws SQLException {
        Connection connection = dataSource.getConnection();
        String database = serverDatabases.get(request.originServerId());
        Objects.requireNonNull(database, "database required for server " + request.originServerId());
        connection.createStatement().execute("USE " + database);
        return connection;
    }

    private NameLayerWriteResponse handleCreateGroup(final NameLayerWriteRequest request) {
        final CreateGroupWrite createWrite;
        try {
            createWrite = CreateGroupWrite.from(request.arguments());
        } catch (final IllegalArgumentException exception) {
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.INVALID_REQUEST, exception.getMessage());
        }
        try (Connection connection = getConnection(request)) {
            connection.setAutoCommit(false);
            try {
                if (!createWrite.adminOverride() && createWrite.maxGroups() > 0) {
                    final int ownedGroups = countOwnedGroups(connection, request.actorUuid());
                    if (ownedGroups >= createWrite.maxGroups()) {
                        connection.rollback();
                        return NameLayerWriteResponse.failure(
                            request.requestId(),
                            NameLayerWriteFailureCode.MAX_GROUPS_REACHED,
                            "You have reached the group limit of " + createWrite.maxGroups()
                        );
                    }
                }
                CreatedGroup created = createGroup(connection, createWrite.groupName(), request.actorUuid(), createWrite.password);
                if (created == null) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.NAME_CONFLICT, "Group name is already taken");
                }
                final int groupId = created.groupId();
                insertDefaultPermissions(connection, groupId, createWrite.defaultPermissions());
                incrementCacheVersion(connection);
                connection.commit();
                publishInvalidation(request, groupId);
                return NameLayerWriteResponse.success(request.requestId(), Set.of(groupId));
            } catch (final SQLException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (final SQLException exception) {
            logger.error("NameLayer group create failed", exception);
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.DATABASE_UNAVAILABLE, "Database write failed");
        }
    }

    private NameLayerWriteResponse handleEnsureNewfriendGroup(final NameLayerWriteRequest request) {
        final NewfriendGroupWrite newfriendWrite;
        try {
            newfriendWrite = NewfriendGroupWrite.from(request.arguments());
        } catch (final IllegalArgumentException exception) {
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.INVALID_REQUEST, exception.getMessage());
        }
        final String lockName = "namelayer:newfriend:" + request.actorUuid();
        try (Connection connection = getConnection(request)) {
            if (!acquireLock(connection, lockName)) {
                return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.DATABASE_UNAVAILABLE, "Timed out waiting for player group lock");
            }
            try {
                connection.setAutoCommit(false);
                try {
                    final DefaultGroup defaultGroup = getDefaultGroupId(connection, request.actorUuid());
                    if (defaultGroup != null) {
                        connection.commit();
                        return NameLayerWriteResponse.success(request.requestId(), Set.of(defaultGroup.groupId()));
                    }
                    final CreatedGroup createdGroup = createFirstAvailableGroup(connection, request.actorUuid(), newfriendWrite);
                    if (createdGroup == null) {
                        connection.rollback();
                        return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.NAME_CONFLICT, "Unable to find an available newfriend group name");
                    }
                    insertDefaultPermissions(connection, createdGroup.groupId(), newfriendWrite.defaultPermissions());
                    setDefaultGroup(connection, request.actorUuid(), createdGroup.groupName());
                    incrementCacheVersion(connection);
                    connection.commit();
                    publishInvalidation(request, NameLayerInvalidationMessage.withAffected(
                        Set.of(createdGroup.groupId()),
                        java.util.Map.of(request.actorUuid(), createdGroup.groupName()),
                        Set.of(),
                        java.util.Map.of()
                    ));
                    return NameLayerWriteResponse.success(request.requestId(), Set.of(createdGroup.groupId()));
                } catch (final SQLException exception) {
                    connection.rollback();
                    throw exception;
                }
            } finally {
                releaseLock(connection, lockName);
            }
        } catch (final SQLException exception) {
            logger.error("NameLayer newfriend group ensure failed", exception);
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.DATABASE_UNAVAILABLE, "Database write failed");
        }
    }

    private NameLayerWriteResponse handleDeleteGroup(final NameLayerWriteRequest request) {
        final GroupIdWrite deleteWrite;
        try {
            deleteWrite = GroupIdWrite.from(request.arguments());
        } catch (final IllegalArgumentException exception) {
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.INVALID_REQUEST, exception.getMessage());
        }
        try (Connection connection = getConnection(request)) {
            connection.setAutoCommit(false);
            try {
                if (!lockGroup(connection, deleteWrite.groupId())) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.GROUP_NOT_FOUND, "Group does not exist");
                }
                if (!deleteWrite.adminOverride() && !hasRoleEditAccess(connection, deleteWrite.groupId(), request.actorUuid(), "DELETE")) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.AUTHORIZATION_FAILED, "You do not have delete access");
                }
                final String groupName = getGroupName(connection, deleteWrite.groupId());
                if (groupName == null) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.GROUP_NOT_FOUND, "Group does not exist");
                }
                try (PreparedStatement statement = connection.prepareStatement(DELETE_GROUP)) {
                    statement.setString(1, groupName);
                    statement.setString(2, "Name_Layer_Special");
                    statement.executeUpdate();
                }
                incrementCacheVersion(connection);
                connection.commit();
                publishInvalidation(request, deleteWrite.groupId());
                return NameLayerWriteResponse.success(request.requestId(), Set.of(deleteWrite.groupId()));
            } catch (final SQLException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (final SQLException exception) {
            logger.error("NameLayer group delete failed", exception);
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.DATABASE_UNAVAILABLE, "Database write failed");
        }
    }

    private NameLayerWriteResponse handleBlacklistWrite(
        final NameLayerWriteRequest request,
        final String sql,
        final boolean add
    ) {
        final BlacklistWrite blacklistWrite;
        try {
            blacklistWrite = BlacklistWrite.from(request.arguments());
        } catch (final IllegalArgumentException exception) {
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.INVALID_REQUEST, exception.getMessage());
        }
        try (Connection connection = getConnection(request)) {
            connection.setAutoCommit(false);
            try {
                if (!lockGroup(connection, blacklistWrite.groupId())) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.GROUP_NOT_FOUND, "Group does not exist");
                }
                if (!blacklistWrite.adminOverride() && !hasRoleEditAccess(connection, blacklistWrite.groupId(), request.actorUuid(), "BLACKLIST")) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.AUTHORIZATION_FAILED, "You do not have blacklist access");
                }
                if (add && isGroupMember(connection, blacklistWrite.groupId(), blacklistWrite.memberUuid())) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.INVALID_REQUEST, "Cannot blacklist a group member");
                }
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, blacklistWrite.groupId());
                    statement.setString(2, blacklistWrite.memberUuid().toString());
                    statement.executeUpdate();
                }
                incrementCacheVersion(connection);
                connection.commit();
                publishInvalidation(request, blacklistWrite.groupId());
                return NameLayerWriteResponse.success(request.requestId(), Set.of(blacklistWrite.groupId()));
            } catch (final SQLException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (final SQLException exception) {
            logger.error("NameLayer blacklist write failed", exception);
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.DATABASE_UNAVAILABLE, "Database write failed");
        }
    }

    private NameLayerWriteResponse handleJoinGroup(final NameLayerWriteRequest request) {
        final JoinGroupWrite joinWrite;
        try {
            joinWrite = JoinGroupWrite.from(request.arguments());
        } catch (final IllegalArgumentException exception) {
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.INVALID_REQUEST, exception.getMessage());
        }
        try (Connection connection = getConnection(request)) {
            connection.setAutoCommit(false);
            try {
                if (!lockGroup(connection, joinWrite.groupId())) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.GROUP_NOT_FOUND, "Group does not exist");
                }
                final String password = getGroupPassword(connection, joinWrite.groupId());
                if (password == null || !password.equals(joinWrite.password())) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.AUTHORIZATION_FAILED, "Group password is incorrect");
                }
                if (!hasRolePermission(connection, joinWrite.groupId(), joinWrite.role(), "JOIN_PASSWORD")) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.AUTHORIZATION_FAILED, "Role cannot join by password");
                }
                if (isGroupMember(connection, joinWrite.groupId(), request.actorUuid())) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.INVALID_REQUEST, "You are already a group member");
                }
                if (isBlacklisted(connection, joinWrite.groupId(), request.actorUuid())) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.AUTHORIZATION_FAILED, "You are blacklisted");
                }
                addMember(connection, joinWrite.groupId(), request.actorUuid(), joinWrite.role());
                incrementCacheVersion(connection);
                connection.commit();
                publishInvalidation(request, joinWrite.groupId());
                return NameLayerWriteResponse.success(request.requestId(), Set.of(joinWrite.groupId()));
            } catch (final SQLException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (final SQLException exception) {
            logger.error("NameLayer password join failed", exception);
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.DATABASE_UNAVAILABLE, "Database write failed");
        }
    }

    private NameLayerWriteResponse handleAddMember(final NameLayerWriteRequest request) {
        final MemberRoleWrite memberWrite;
        try {
            memberWrite = MemberRoleWrite.from(request.arguments());
        } catch (final IllegalArgumentException exception) {
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.INVALID_REQUEST, exception.getMessage());
        }
        try (Connection connection = getConnection(request)) {
            connection.setAutoCommit(false);
            try {
                if (!lockGroup(connection, memberWrite.groupId())) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.GROUP_NOT_FOUND, "Group does not exist");
                }
                if (isBlacklisted(connection, memberWrite.groupId(), memberWrite.memberUuid())) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.AUTHORIZATION_FAILED, "Player is blacklisted");
                }
                addMember(connection, memberWrite.groupId(), memberWrite.memberUuid(), memberWrite.role());
                incrementCacheVersion(connection);
                connection.commit();
                publishInvalidation(request, memberWrite.groupId());
                return NameLayerWriteResponse.success(request.requestId(), Set.of(memberWrite.groupId()));
            } catch (final SQLException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (final SQLException exception) {
            logger.error("NameLayer member add failed", exception);
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.DATABASE_UNAVAILABLE, "Database write failed");
        }
    }

    private NameLayerWriteResponse handleAddInvitation(final NameLayerWriteRequest request) {
        final InvitationWrite invitationWrite;
        try {
            invitationWrite = InvitationWrite.from(request.arguments(), true);
        } catch (final IllegalArgumentException exception) {
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.INVALID_REQUEST, exception.getMessage());
        }
        try (Connection connection = getConnection(request)) {
            connection.setAutoCommit(false);
            try {
                final NameLayerWriteResponse validationFailure = validateInvitationAccess(connection, request, invitationWrite, invitationWrite.role());
                if (validationFailure != null) {
                    connection.rollback();
                    return validationFailure;
                }
                if (isGroupMember(connection, invitationWrite.groupId(), invitationWrite.memberUuid())) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.INVALID_REQUEST, "Player is already a group member");
                }
                if (isBlacklisted(connection, invitationWrite.groupId(), invitationWrite.memberUuid())) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.INVALID_REQUEST, "Player is blacklisted");
                }
                final String groupName = getGroupName(connection, invitationWrite.groupId());
                if (groupName == null) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.GROUP_NOT_FOUND, "Group does not exist");
                }
                final boolean autoAccept = isAutoAccept(connection, invitationWrite.memberUuid());
                if (autoAccept) {
                    addMember(connection, invitationWrite.groupId(), invitationWrite.memberUuid(), invitationWrite.role());
                    removeInvitation(connection, invitationWrite.memberUuid(), groupName);
                } else {
                    addInvitation(connection, invitationWrite.memberUuid(), groupName, invitationWrite.role());
                }
                incrementCacheVersion(connection);
                connection.commit();
                publishInvalidation(request, invitationWrite.groupId());
                sendInvitationMessage(connection, request, invitationWrite, groupName, autoAccept);
                return NameLayerWriteResponse.success(request.requestId(), Set.of(invitationWrite.groupId()));
            } catch (final SQLException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (final SQLException exception) {
            logger.error("NameLayer invitation add failed", exception);
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.DATABASE_UNAVAILABLE, "Database write failed");
        }
    }

    private NameLayerWriteResponse handleRemoveInvitation(final NameLayerWriteRequest request) {
        final InvitationWrite invitationWrite;
        try {
            invitationWrite = InvitationWrite.from(request.arguments(), false);
        } catch (final IllegalArgumentException exception) {
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.INVALID_REQUEST, exception.getMessage());
        }
        try (Connection connection = getConnection(request)) {
            connection.setAutoCommit(false);
            try {
                if (!lockGroup(connection, invitationWrite.groupId())) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.GROUP_NOT_FOUND, "Group does not exist");
                }
                final String groupName = getGroupName(connection, invitationWrite.groupId());
                if (groupName == null) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.GROUP_NOT_FOUND, "Group does not exist");
                }
                final String role = getInvitationRole(connection, invitationWrite.memberUuid(), groupName);
                if (role == null) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.INVALID_REQUEST, "Player is not invited");
                }
                if (!invitationWrite.adminOverride() && !request.actorUuid().equals(invitationWrite.memberUuid()) && !hasRoleEditAccess(connection, invitationWrite.groupId(), request.actorUuid(), role)) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.AUTHORIZATION_FAILED, "You cannot remove this invitation");
                }
                removeInvitation(connection, invitationWrite.memberUuid(), groupName);
                incrementCacheVersion(connection);
                connection.commit();
                publishInvalidation(request, invitationWrite.groupId());
                return NameLayerWriteResponse.success(request.requestId(), Set.of(invitationWrite.groupId()));
            } catch (final SQLException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (final SQLException exception) {
            logger.error("NameLayer invitation removal failed", exception);
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.DATABASE_UNAVAILABLE, "Database write failed");
        }
    }

    private NameLayerWriteResponse handleAcceptInvitation(final NameLayerWriteRequest request) {
        final GroupIdWrite groupWrite;
        try {
            groupWrite = GroupIdWrite.from(request.arguments());
        } catch (final IllegalArgumentException exception) {
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.INVALID_REQUEST, exception.getMessage());
        }
        try (Connection connection = getConnection(request)) {
            connection.setAutoCommit(false);
            try {
                if (!lockGroup(connection, groupWrite.groupId())) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.GROUP_NOT_FOUND, "Group does not exist");
                }
                final String groupName = getGroupName(connection, groupWrite.groupId());
                if (groupName == null) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.GROUP_NOT_FOUND, "Group does not exist");
                }
                final String role = getInvitationRole(connection, request.actorUuid(), groupName);
                if (role == null) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.INVALID_REQUEST, "You are not invited");
                }
                addMember(connection, groupWrite.groupId(), request.actorUuid(), role);
                removeInvitation(connection, request.actorUuid(), groupName);
                incrementCacheVersion(connection);
                connection.commit();
                publishInvalidation(request, groupWrite.groupId());
                return NameLayerWriteResponse.success(request.requestId(), Set.of(groupWrite.groupId()));
            } catch (final SQLException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (final SQLException exception) {
            logger.error("NameLayer invitation accept failed", exception);
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.DATABASE_UNAVAILABLE, "Database write failed");
        }
    }

    private NameLayerWriteResponse handleSetDefaultGroup(final NameLayerWriteRequest request) {
        final GroupIdWrite groupWrite;
        try {
            groupWrite = GroupIdWrite.from(request.arguments());
        } catch (final IllegalArgumentException exception) {
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.INVALID_REQUEST, exception.getMessage());
        }
        try (Connection connection = getConnection(request)) {
            connection.setAutoCommit(false);
            try {
                if (!lockGroup(connection, groupWrite.groupId())) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.GROUP_NOT_FOUND, "Group does not exist");
                }
                if (!isGroupMember(connection, groupWrite.groupId(), request.actorUuid())) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.AUTHORIZATION_FAILED, "You are not a group member");
                }
                final String groupName = getGroupName(connection, groupWrite.groupId());
                if (groupName == null) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.GROUP_NOT_FOUND, "Group does not exist");
                }
                setDefaultGroup(connection, request.actorUuid(), groupName);
                incrementCacheVersion(connection);
                connection.commit();
                publishInvalidation(request, NameLayerInvalidationMessage.defaultGroupAssignment(request.actorUuid(), groupName));
                return NameLayerWriteResponse.success(request.requestId(), Set.of());
            } catch (final SQLException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (final SQLException exception) {
            logger.error("NameLayer default group write failed", exception);
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.DATABASE_UNAVAILABLE, "Database write failed");
        }
    }

    private NameLayerWriteResponse handleSetAutoAccept(final NameLayerWriteRequest request) {
        final boolean autoAccept;
        try {
            final String value = PermissionWrite.requireNonBlank(request.arguments(), "value");
            if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
                throw new IllegalArgumentException("value must be true or false");
            }
            autoAccept = Boolean.parseBoolean(value);
        } catch (final IllegalArgumentException exception) {
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.INVALID_REQUEST, exception.getMessage());
        }
        try (Connection connection = getConnection(request)) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(autoAccept ? ADD_AUTO_ACCEPT : REMOVE_AUTO_ACCEPT)) {
                statement.setString(1, request.actorUuid().toString());
                statement.executeUpdate();
                incrementCacheVersion(connection);
                connection.commit();
                publishInvalidation(request, NameLayerInvalidationMessage.autoAccept(request.actorUuid(), autoAccept));
                return NameLayerWriteResponse.success(request.requestId(), Set.of());
            } catch (final SQLException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (final SQLException exception) {
            logger.error("NameLayer auto-accept write failed", exception);
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.DATABASE_UNAVAILABLE, "Database write failed");
        }
    }

    private NameLayerWriteResponse validateInvitationAccess(
        final Connection connection,
        final NameLayerWriteRequest request,
        final InvitationWrite invitationWrite,
        final String role
    ) throws SQLException {
        if (!lockGroup(connection, invitationWrite.groupId())) {
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.GROUP_NOT_FOUND, "Group does not exist");
        }
        if (!invitationWrite.adminOverride() && !hasRoleEditAccess(connection, invitationWrite.groupId(), request.actorUuid(), role)) {
            return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.AUTHORIZATION_FAILED, "You cannot invite this role");
        }
        return null;
    }

    private void insertDefaultPermissions(
        final Connection connection,
        final int groupId,
        final Set<DefaultPermission> defaultPermissions
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(ADD_PERMISSION_BY_NAME)) {
            for (final DefaultPermission defaultPermission : defaultPermissions) {
                statement.setInt(1, groupId);
                statement.setString(2, defaultPermission.role());
                statement.setString(3, defaultPermission.permissionName());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private CreatedGroup createFirstAvailableGroup(
        final Connection connection,
        final UUID ownerUuid,
        final NewfriendGroupWrite newfriendWrite
    ) throws SQLException {
        for (int attempt = 0; attempt < 20; attempt++) {
            final String groupName = attempt == 0 ? newfriendWrite.baseName() : newfriendWrite.baseName() + (attempt - 1);
            final CreatedGroup createdGroup = createGroup(connection, groupName, ownerUuid, null);
            if (createdGroup != null) {
                return createdGroup;
            }
        }
        return null;
    }

    private CreatedGroup createGroup(
        final Connection connection,
        final String groupName,
        final UUID ownerUuid,
        final String password
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(CREATE_GROUP)) {
            statement.setString(1, groupName);
            statement.setString(2, ownerUuid.toString());
            statement.setString(3, password);
            statement.setInt(4, 0);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new CreatedGroup(groupName, resultSet.getInt(1));
            }
        }
    }

    private DefaultGroup getDefaultGroupId(final Connection connection, final UUID playerUuid) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(GET_DEFAULT_GROUP_ID)) {
            statement.setString(1, playerUuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new DefaultGroup(resultSet.getString(1), resultSet.getInt(2));
            }
        }
    }

    private void setDefaultGroup(final Connection connection, final UUID playerUuid, final String groupName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SET_DEFAULT_GROUP)) {
            statement.setString(1, playerUuid.toString());
            statement.setString(2, groupName);
            statement.executeUpdate();
        }
    }

    private int countOwnedGroups(final Connection connection, final UUID ownerUuid) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(COUNT_OWNED_GROUPS)) {
            statement.setString(1, ownerUuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return 0;
                }
                return resultSet.getInt(1);
            }
        }
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
        try (Connection connection = getConnection(request)) {
            connection.setAutoCommit(false);
            try {
                if (!lockGroup(connection, metadataWrite.groupId())) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.GROUP_NOT_FOUND, "Group does not exist");
                }
                if (!metadataWrite.adminOverride() && permissionName != null && !hasRoleEditAccess(connection, metadataWrite.groupId(), request.actorUuid(), permissionName)) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.AUTHORIZATION_FAILED, "You do not have metadata edit access");
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
                publishInvalidation(request, metadataWrite.groupId());
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
        try (Connection connection = getConnection(request)) {
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
                if (!ownerWrite.adminOverride() && ownerWrite.maxGroups() > 0) {
                    final int ownedGroups = countOwnedGroups(connection, ownerWrite.ownerUuid());
                    if (ownedGroups >= ownerWrite.maxGroups()) {
                        connection.rollback();
                        return NameLayerWriteResponse.failure(
                            request.requestId(),
                            NameLayerWriteFailureCode.MAX_GROUPS_REACHED,
                            "New owner has reached the group limit of " + ownerWrite.maxGroups()
                        );
                    }
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
                publishInvalidation(request, ownerWrite.groupId());
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

        try (Connection connection = getConnection(request)) {
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
                    return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.AUTHORIZATION_FAILED, "You do not have member edit access");
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
                publishInvalidation(request, memberWrite.groupId());
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

        try (Connection connection = getConnection(request)) {
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
                publishInvalidation(request, memberWrite.groupId());
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
                return NameLayerWriteResponse.failure(request.requestId(), NameLayerWriteFailureCode.AUTHORIZATION_FAILED, "You do not have member edit access");
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

        try (Connection connection = getConnection(request)) {
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
                if (!permissionWrite.adminOverride() && !hasPermissionEditAccess(connection, permissionWrite.groupId(), request.actorUuid())) {
                    connection.rollback();
                    return NameLayerWriteResponse.failure(
                        request.requestId(),
                        NameLayerWriteFailureCode.AUTHORIZATION_FAILED,
                        "You do not have permission edit access"
                    );
                }
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, permissionWrite.groupId());
                    statement.setString(2, permissionWrite.role());
                    statement.setString(3, permissionWrite.permissionName());
                    statement.executeUpdate();
                }
                incrementCacheVersion(connection);
                connection.commit();
                publishInvalidation(request, permissionWrite.groupId());
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

    private void publishInvalidation(NameLayerWriteRequest request, final int groupId) {
        publishInvalidation(request, NameLayerInvalidationMessage.targeted(Set.of(groupId)));
    }

    private void publishInvalidation(NameLayerWriteRequest request, final NameLayerInvalidationMessage invalidation) {
        final boolean published = invalidationPublisher.publish(serverDatabases.get(request.originServerId()), invalidation);
        if (!published) {
            logger.error("NameLayer write committed, but failed to publish invalidation {}", invalidation);
        }
    }

    private boolean acquireLock(final Connection connection, final String lockName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(GET_PLAYER_LOCK)) {
            statement.setString(1, lockName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) == 1;
            }
        }
    }

    private void releaseLock(final Connection connection, final String lockName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(RELEASE_PLAYER_LOCK)) {
            statement.setString(1, lockName);
            statement.executeQuery();
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
        if ("OWNER".equals(actorRole)) {
            return true;
        }
        try (PreparedStatement statement = connection.prepareStatement(HAS_ROLE_PERMISSION)) {
            statement.setInt(1, groupId);
            statement.setString(2, actorRole);
            statement.setString(3, "PERMS");
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
        if ("OWNER".equals(actorRole)) {
            return true;
        }
        return hasRolePermission(connection, groupId, actorRole, permissionName);
    }

    private boolean hasRolePermission(final Connection connection, final int groupId, final String role, final String permissionName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(HAS_ROLE_PERMISSION)) {
            statement.setInt(1, groupId);
            statement.setString(2, role);
            statement.setString(3, permissionName);
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

    private String getGroupName(final Connection connection, final int groupId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(GET_GROUP_NAME)) {
            statement.setInt(1, groupId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return resultSet.getString(1);
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

    private boolean isGroupMember(final Connection connection, final int groupId, final UUID memberUuid) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(IS_GROUP_MEMBER)) {
            statement.setInt(1, groupId);
            statement.setString(2, memberUuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private boolean isBlacklisted(final Connection connection, final int groupId, final UUID memberUuid) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(IS_BLACKLISTED)) {
            statement.setInt(1, groupId);
            statement.setString(2, memberUuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private String getGroupPassword(final Connection connection, final int groupId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(GET_GROUP_PASSWORD)) {
            statement.setInt(1, groupId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return resultSet.getString(1);
            }
        }
    }

    private boolean isAutoAccept(final Connection connection, final UUID memberUuid) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(IS_AUTO_ACCEPT)) {
            statement.setString(1, memberUuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void addMember(final Connection connection, final int groupId, final UUID memberUuid, final String role) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(ADD_MEMBER)) {
            statement.setInt(1, groupId);
            statement.setString(2, memberUuid.toString());
            statement.setString(3, role);
            statement.executeUpdate();
        }
    }

    private void addInvitation(final Connection connection, final UUID memberUuid, final String groupName, final String role) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(ADD_INVITATION)) {
            statement.setString(1, memberUuid.toString());
            statement.setString(2, groupName);
            statement.setString(3, role);
            statement.executeUpdate();
        }
    }

    private String getInvitationRole(final Connection connection, final UUID memberUuid, final String groupName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(GET_INVITATION_ROLE)) {
            statement.setString(1, memberUuid.toString());
            statement.setString(2, groupName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return resultSet.getString(1);
            }
        }
    }

    private void removeInvitation(final Connection connection, final UUID memberUuid, final String groupName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(REMOVE_INVITATION)) {
            statement.setString(1, memberUuid.toString());
            statement.setString(2, groupName);
            statement.executeUpdate();
        }
    }

    private void sendInvitationMessage(
        final Connection connection,
        final NameLayerWriteRequest request,
        final InvitationWrite invitationWrite,
        final String groupName,
        final boolean autoAccept
    ) {
        final Player player = proxyServer.getPlayer(invitationWrite.memberUuid()).orElse(null);
        if (player == null) {
            return;
        }
        if (!isOnSameDatabase(player, request.originServerId())) {
            return;
        }
        if (autoAccept) {
            player.sendMessage(Component.text(" You have auto-accepted invite to the group: " + groupName, NamedTextColor.GREEN));
            return;
        }
        final String inviteText;
        if (invitationWrite.showInviter()) {
            final String inviterName;
            try {
                inviterName = getCurrentName(connection, request.actorUuid());
            } catch (final SQLException exception) {
                logger.warn("Failed to look up inviter name for invitation message", exception);
                return;
            }
            inviteText = "You have been invited to the group " + groupName + " by " + inviterName + ".\n";
        } else {
            inviteText = "You have been invited to the group " + groupName + ".\n";
        }
        player.sendMessage(Component.text(inviteText + "Click this message to accept. If you wish to toggle invites "
                + "so they always are accepted please run /autoaccept", NamedTextColor.GREEN)
            .clickEvent(ClickEvent.runCommand("/nlag " + groupName))
            .hoverEvent(HoverEvent.showText(Component.text("  ---  Click to accept"))));
    }

    private boolean isOnSameDatabase(final Player player, final String originServerId) {
        final String originDatabase = serverDatabases.get(originServerId);
        if (originDatabase == null) {
            return false;
        }
        return player.getCurrentServer()
            .map(serverConnection -> originDatabase.equals(serverDatabases.get(serverConnection.getServerInfo().getName())))
            .orElse(false);
    }

    private String getCurrentName(final Connection connection, final UUID uuid) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(GET_PLAYER_NAME)) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return resultSet.getString(1);
            }
        }
    }

    private record PermissionWrite(int groupId, String role, String permissionName, boolean adminOverride) {

        private static PermissionWrite from(final Map<String, String> arguments) {
            final int groupId = parsePositiveInt(arguments, "groupId");
            final String role = requireNonBlank(arguments, "role");
            validateRole(role);
            final String permissionName = requireNonBlank(arguments, "permissionName");
            final boolean adminOverride = Boolean.parseBoolean(arguments.getOrDefault("adminOverride", "false"));
            return new PermissionWrite(groupId, role, permissionName, adminOverride);
        }

        private static void validateRole(final String role) {
            if (!Set.of("MEMBERS", "MODS", "ADMINS", "OWNER", "NOT_BLACKLISTED").contains(role)) {
                throw new IllegalArgumentException("role must be MEMBERS, MODS, ADMINS, NOT_BLACKLISTED, or OWNER");
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
            return new MemberRoleWrite(memberWrite.groupId(), memberWrite.memberUuid(), role);
        }
    }

    private record JoinGroupWrite(int groupId, String password, String role) {

        private static JoinGroupWrite from(final Map<String, String> arguments) {
            final int groupId = PermissionWrite.parsePositiveInt(arguments, "groupId");
            final String password = PermissionWrite.requireNonBlank(arguments, "password");
            final String role = PermissionWrite.requireNonBlank(arguments, "role");
            PermissionWrite.validateRole(role);
            if ("OWNER".equals(role)) {
                throw new IllegalArgumentException("role cannot be OWNER for password joins");
            }
            return new JoinGroupWrite(groupId, password, role);
        }
    }

    private record BlacklistWrite(int groupId, UUID memberUuid, boolean adminOverride) {

        private static BlacklistWrite from(final Map<String, String> arguments) {
            final MemberWrite memberWrite = MemberWrite.from(arguments);
            final boolean adminOverride = Boolean.parseBoolean(arguments.getOrDefault("adminOverride", "false"));
            return new BlacklistWrite(memberWrite.groupId(), memberWrite.memberUuid(), adminOverride);
        }
    }

    private record InvitationWrite(int groupId, UUID memberUuid, String role, boolean adminOverride, boolean showInviter) {

        private static InvitationWrite from(final Map<String, String> arguments, final boolean requireRole) {
            final MemberWrite memberWrite = MemberWrite.from(arguments);
            final String role;
            if (requireRole) {
                role = PermissionWrite.requireNonBlank(arguments, "role");
                PermissionWrite.validateRole(role);
            } else {
                role = null;
            }
            final boolean adminOverride = Boolean.parseBoolean(arguments.getOrDefault("adminOverride", "false"));
            final boolean showInviter = Boolean.parseBoolean(arguments.getOrDefault("showInviter", "true"));
            return new InvitationWrite(memberWrite.groupId(), memberWrite.memberUuid(), role, adminOverride, showInviter);
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

    private record OwnerWrite(int groupId, UUID ownerUuid, int maxGroups, boolean adminOverride) {

        private static OwnerWrite from(final Map<String, String> arguments) {
            final int groupId = PermissionWrite.parsePositiveInt(arguments, "groupId");
            final UUID ownerUuid = MemberWrite.parseUuid(arguments, "ownerUuid");
            final int maxGroups;
            try {
                maxGroups = Integer.parseInt(arguments.getOrDefault("maxGroups", "0"));
            } catch (final NumberFormatException exception) {
                throw new IllegalArgumentException("maxGroups must be an integer", exception);
            }
            final boolean adminOverride = Boolean.parseBoolean(arguments.getOrDefault("adminOverride", "false"));
            return new OwnerWrite(groupId, ownerUuid, maxGroups, adminOverride);
        }
    }

    private record GroupIdWrite(int groupId, boolean adminOverride) {

        private static GroupIdWrite from(final Map<String, String> arguments) {
            final int groupId = PermissionWrite.parsePositiveInt(arguments, "groupId");
            final boolean adminOverride = Boolean.parseBoolean(arguments.getOrDefault("adminOverride", "false"));
            return new GroupIdWrite(groupId, adminOverride);
        }
    }

    private record CreateGroupWrite(
        String groupName,
        String password,
        Set<DefaultPermission> defaultPermissions,
        int maxGroups,
        boolean adminOverride
    ) {

        private static CreateGroupWrite from(final Map<String, String> arguments) {
            final String groupName = PermissionWrite.requireNonBlank(arguments, "groupName");
            final String password = Boolean.parseBoolean(arguments.getOrDefault("hasPassword", "false"))
                ? arguments.getOrDefault("password", "")
                : null;
            final int maxGroups;
            try {
                maxGroups = Integer.parseInt(arguments.getOrDefault("maxGroups", "0"));
            } catch (final NumberFormatException exception) {
                throw new IllegalArgumentException("maxGroups must be an integer");
            }
            final boolean adminOverride = Boolean.parseBoolean(arguments.getOrDefault("adminOverride", "false"));
            return new CreateGroupWrite(
                groupName,
                password,
                DefaultPermission.parse(arguments.getOrDefault("defaultPermissions", "")),
                maxGroups,
                adminOverride
            );
        }
    }

    private record NewfriendGroupWrite(String baseName, Set<DefaultPermission> defaultPermissions) {

        private static NewfriendGroupWrite from(final Map<String, String> arguments) {
            final String baseName = PermissionWrite.requireNonBlank(arguments, "baseName");
            return new NewfriendGroupWrite(baseName, DefaultPermission.parse(arguments.getOrDefault("defaultPermissions", "")));
        }
    }

    private record CreatedGroup(String groupName, int groupId) {
    }

    private record DefaultGroup(String groupName, int groupId) {
    }

    private record DefaultPermission(String role, String permissionName) {

        private static Set<DefaultPermission> parse(final String value) {
            final Set<DefaultPermission> defaultPermissions = new java.util.LinkedHashSet<>();
            if (value == null || value.isBlank()) {
                return defaultPermissions;
            }
            for (final String entry : value.split(";")) {
                if (entry.isBlank()) {
                    continue;
                }
                final String[] parts = entry.split(":", 2);
                if (parts.length != 2) {
                    throw new IllegalArgumentException("defaultPermissions entries must be role:permissionName");
                }
                PermissionWrite.validateRole(parts[0]);
                defaultPermissions.add(new DefaultPermission(parts[0], PermissionWrite.requireNonBlank(Map.of("permissionName", parts[1]), "permissionName")));
            }
            return defaultPermissions;
        }
    }
}
