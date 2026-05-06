package vg.civcraft.mc.namelayer.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.listeners.PlayerListener;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public final class NameLayerReadDao {

    private static final String GET_GROUP = "select f.group_name, f.founder, f.password, f.discipline_flags, fi.group_id, f.last_timestamp, f.group_color "
        + "from faction f "
        + "inner join faction_id fi on fi.group_name = f.group_name "
        + "where f.group_name = ?";
    private static final String GET_GROUP_BY_ID = "select f.group_name, f.founder, f.password, f.discipline_flags, fi.group_id, f.last_timestamp, f.group_color "
        + "from faction f "
        + "inner join faction_id fi on fi.group_id = ? "
        + "where f.group_name = fi.group_name";
    private static final String GET_ALL_GROUP_NAMES = "select f.group_name from faction_id f "
        + "inner join faction_member fm on f.group_id = fm.group_id "
        + "where fm.member_name = ?";
    private static final String GET_MEMBERS = "select fm.member_name from faction_member fm "
        + "inner join faction_id id on id.group_name = ? "
        + "where fm.group_id = id.group_id and fm.role = ?";
    private static final String COUNT_GROUPS = "select count(DISTINCT group_name) as count from faction";
    private static final String COUNT_GROUPS_FROM_UUID = "select count(DISTINCT group_name) as count from faction where founder = ?";
    private static final String LOAD_ALL_AUTO_ACCEPT = "select uuid from toggleAutoAccept";
    private static final String GET_AUTO_ACCEPT = "select uuid from toggleAutoAccept where uuid = ?";
    private static final String GET_DEFAULT_GROUP = "select defaultgroup from default_group where uuid = ?";
    private static final String GET_ALL_DEFAULT_GROUPS = "select uuid,defaultgroup from default_group";
    private static final String LOAD_GROUPS_INVITATIONS = "select uuid, groupName, role from group_invitation";
    private static final String LOAD_GROUP_INVITATION = "select role from group_invitation where uuid = ? and groupName = ?";
    private static final String LOAD_GROUP_INVITATIONS_FOR_GROUP = "select uuid,role from group_invitation where groupName=?";
    private static final String GET_GROUP_NAME_FROM_ROLE = "SELECT DISTINCT faction_id.group_name FROM faction_member "
        + "inner join faction_id on faction_member.group_id = faction_id.group_id "
        + "WHERE member_name = ? "
        + "AND role = ?";
    private static final String GET_PLAYER_TYPE = "SELECT role FROM faction_member WHERE group_id = ? AND member_name = ?";
    private static final String LOG_NAME_CHANGE = "insert into nameLayerNameChanges (uuid,oldName,newName) values(?,?,?)";
    private static final String CHECK_FOR_NAME_CHANGE = "select * from nameLayerNameChanges where uuid=?";
    private static final String GET_PERMISSION = "select pg.role,pg.permission_name from permission_by_group_name pg inner join faction_id fi on fi.group_name=? "
        + "where pg.group_id = fi.group_id";
    private static final String GET_BLACKLIST_MEMBERS = "select b.member_name from blacklist b inner join faction_id fi on fi.group_name=? where b.group_id=fi.group_id";
    private static final String GET_GROUP_IDS = "SELECT f.group_id, count(DISTINCT fm.member_name) AS sz FROM faction_id f "
        + "LEFT JOIN faction_member fm ON f.group_id = fm.group_id WHERE f.group_name = ? GROUP BY f.group_id ORDER BY sz DESC";
    private static final String LOAD_ALL_GROUP_HEADERS = "select f.group_name, f.founder, f.password, f.discipline_flags, "
        + "fi.group_id, f.last_timestamp, f.group_color from faction f "
        + "inner join faction_id fi on fi.group_name = f.group_name";
    private static final String LOAD_ALL_GROUP_IDS = "SELECT f.group_name, f.group_id, count(DISTINCT fm.member_name) AS sz "
        + "FROM faction_id f LEFT JOIN faction_member fm ON f.group_id = fm.group_id "
        + "GROUP BY f.group_name, f.group_id ORDER BY f.group_name, sz DESC";
    private static final String LOAD_ALL_MEMBERS = "select fi.group_name, fm.member_name, fm.role from faction_member fm "
        + "inner join faction_id fi on fi.group_id = fm.group_id";
    private static final String LOAD_ALL_BLACKLISTS = "select fi.group_name, b.member_name from blacklist b "
        + "inner join faction_id fi on fi.group_id = b.group_id";
    private static final String LOAD_GROUPS_BY_IDS_PREFIX = "select requested.group_id, f.group_name, f.founder, f.password, "
        + "f.discipline_flags, f.last_timestamp, f.group_color, all_ids.group_id, fm.member_name, fm.role, b.member_name "
        + "from faction_id requested "
        + "inner join faction f on f.group_name = requested.group_name "
        + "left join faction_id all_ids on all_ids.group_name = f.group_name "
        + "left join (select group_id, count(distinct member_name) member_count from faction_member group by group_id) counts "
        + "on counts.group_id = all_ids.group_id "
        + "left join faction_member fm on fm.group_id = all_ids.group_id "
        + "left join blacklist b on b.group_id = all_ids.group_id "
        + "where requested.group_id in (";
    private static final String LOAD_GROUPS_BY_IDS_SUFFIX = ") order by f.group_name, counts.member_count desc, all_ids.group_id";
    private static final String LOAD_CACHE_VERSION = "select cache_version from namelayer_cache_version where id = 1";

    private final Logger logger;
    private final ManagedDatasource db;

    public NameLayerReadDao(final Logger logger, final ManagedDatasource db) {
        this.logger = logger;
        this.db = db;
    }

    private record GroupHeader(String name, UUID owner, boolean disciplined, String password, int groupId,
                               long activityTimestamp, String groupColor) {
    }

    public record GroupLoadSnapshot(List<Group> groups, long cacheVersion) {
    }

    public record GroupReloadSnapshot(Map<Integer, Group> groups, long cacheVersion) {
    }

    public Group getGroup(final String groupName) {
        try (Connection connection = db.getConnection();
             PreparedStatement getGroup = connection.prepareStatement(GET_GROUP)) {
            getGroup.setString(1, groupName);
            try (ResultSet set = getGroup.executeQuery()) {
                if (!set.next()) {
                    return null;
                }
                return groupFromResult(set);
            }
        } catch (final SQLException exception) {
            logger.log(Level.WARNING, "Problem getting group " + groupName, exception);
            return null;
        }
    }

    public Group getGroup(final int groupId) {
        try (Connection connection = db.getConnection();
             PreparedStatement getGroupById = connection.prepareStatement(GET_GROUP_BY_ID)) {
            getGroupById.setInt(1, groupId);
            try (ResultSet set = getGroupById.executeQuery()) {
                if (!set.next()) {
                    return null;
                }
                return groupFromResult(set);
            }
        } catch (final SQLException exception) {
            logger.log(Level.WARNING, "Problem getting group " + groupId, exception);
            return null;
        }
    }

    private Group groupFromResult(final ResultSet set) throws SQLException {
        final String uuid = set.getString(2);
        final UUID owner = uuid == null ? null : UUID.fromString(uuid);
        final Timestamp timeStamp = set.getTimestamp(6);
        return new Group(
            set.getString(1),
            owner,
            set.getInt(4) != 0,
            set.getString(3),
            set.getInt(5),
            timeStamp == null ? System.currentTimeMillis() : timeStamp.getTime(),
            set.getString(7));
    }

    public GroupReloadSnapshot loadGroupsByIdsSnapshot(final Set<Integer> requestedGroupIds) {
        if (requestedGroupIds == null || requestedGroupIds.isEmpty()) {
            return new GroupReloadSnapshot(new HashMap<>(), loadCacheVersion());
        }

        final Map<Integer, String> requestedNames = new HashMap<>();
        final Map<String, GroupHeader> headers = new LinkedHashMap<>();
        final Map<String, List<Integer>> groupIds = new HashMap<>();
        final Map<String, Map<UUID, PlayerType>> members = new HashMap<>();
        final Map<String, Set<UUID>> blacklists = new HashMap<>();
        final long cacheVersion;

        try (Connection connection = db.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(loadGroupsByIdsSql(requestedGroupIds.size()))) {
                int parameterIndex = 1;
                for (final int groupId : requestedGroupIds) {
                    statement.setInt(parameterIndex++, groupId);
                }
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        final int requestedGroupId = resultSet.getInt(1);
                        final String name = resultSet.getString(2);
                        requestedNames.put(requestedGroupId, name);
                        if (!headers.containsKey(name)) {
                            final String ownerString = resultSet.getString(3);
                            final UUID owner = ownerString == null ? null : UUID.fromString(ownerString);
                            final Timestamp timestamp = resultSet.getTimestamp(6);
                            headers.put(name, new GroupHeader(
                                name,
                                owner,
                                resultSet.getInt(5) != 0,
                                resultSet.getString(4),
                                requestedGroupId,
                                timestamp == null ? System.currentTimeMillis() : timestamp.getTime(),
                                resultSet.getString(7)));
                        }

                        final int groupId = resultSet.getInt(8);
                        if (!resultSet.wasNull()) {
                            final List<Integer> ids = groupIds.computeIfAbsent(name, key -> new ArrayList<>());
                            if (!ids.contains(groupId)) {
                                ids.add(groupId);
                            }
                        }

                        final String memberUuidString = resultSet.getString(9);
                        final PlayerType role = PlayerType.getPlayerType(resultSet.getString(10));
                        if (memberUuidString != null && role != null) {
                            members.computeIfAbsent(name, key -> new HashMap<>()).put(UUID.fromString(memberUuidString), role);
                        }

                        final String blacklistUuidString = resultSet.getString(11);
                        if (blacklistUuidString != null) {
                            blacklists.computeIfAbsent(name, key -> new HashSet<>()).add(UUID.fromString(blacklistUuidString));
                        }
                    }
                }
            }
            cacheVersion = loadCacheVersion(connection);
            connection.commit();
        } catch (final SQLException exception) {
            logger.log(Level.WARNING, "Unable to bulk load NameLayer groups by ID", exception);
            return null;
        }

        final Map<String, Group> groupsByName = new HashMap<>();
        for (final GroupHeader header : headers.values()) {
            final List<Integer> ids = groupIds.get(header.name());
            final int primaryId = ids == null || ids.isEmpty() ? header.groupId() : ids.get(0);
            groupsByName.put(header.name(), new Group(
                header.name(),
                header.owner(),
                header.disciplined(),
                header.password(),
                primaryId,
                header.activityTimestamp(),
                header.groupColor(),
                ids,
                members.get(header.name()),
                blacklists.get(header.name())));
        }

        final Map<Integer, Group> groupsByRequestedId = new HashMap<>();
        for (final Entry<Integer, String> entry : requestedNames.entrySet()) {
            groupsByRequestedId.put(entry.getKey(), groupsByName.get(entry.getValue()));
        }
        return new GroupReloadSnapshot(groupsByRequestedId, cacheVersion);
    }

    public long loadCacheVersion() {
        try (Connection connection = db.getConnection()) {
            return loadCacheVersion(connection);
        } catch (final SQLException exception) {
            logger.log(Level.WARNING, "Unable to load NameLayer cache version", exception);
        }
        return 0L;
    }

    private long loadCacheVersion(final Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(LOAD_CACHE_VERSION);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getLong(1) : 0L;
        }
    }

    private String loadGroupsByIdsSql(final int groupIdCount) {
        final StringBuilder placeholders = new StringBuilder();
        for (int index = 0; index < groupIdCount; index++) {
            if (index > 0) {
                placeholders.append(',');
            }
            placeholders.append('?');
        }
        return LOAD_GROUPS_BY_IDS_PREFIX + placeholders + LOAD_GROUPS_BY_IDS_SUFFIX;
    }

    public List<String> getGroupNames(final UUID uuid) {
        final List<String> groups = new ArrayList<>();
        try (Connection connection = db.getConnection();
             PreparedStatement getAllGroupsNames = connection.prepareStatement(GET_ALL_GROUP_NAMES)) {
            getAllGroupsNames.setString(1, uuid.toString());
            try (ResultSet set = getAllGroupsNames.executeQuery()) {
                while (set.next()) {
                    groups.add(set.getString(1));
                }
            }
        } catch (final SQLException exception) {
            logger.log(Level.WARNING, "Problem getting player's groups " + uuid, exception);
        }
        return groups;
    }

    public List<String> getGroupNames(final UUID uuid, final String role) {
        final List<String> groups = new ArrayList<>();
        try (Connection connection = db.getConnection();
             PreparedStatement getGroupNameFromRole = connection.prepareStatement(GET_GROUP_NAME_FROM_ROLE)) {
            getGroupNameFromRole.setString(1, uuid.toString());
            getGroupNameFromRole.setString(2, role);
            try (ResultSet set = getGroupNameFromRole.executeQuery()) {
                while (set.next()) {
                    groups.add(set.getString(1));
                }
            }
        } catch (final SQLException exception) {
            logger.log(Level.WARNING, "Problem getting player " + uuid + " groups by role " + role, exception);
        }
        return groups;
    }

    public PlayerType getPlayerType(final int groupId, final UUID uuid) {
        try (Connection connection = db.getConnection();
             PreparedStatement getPlayerType = connection.prepareStatement(GET_PLAYER_TYPE)) {
            getPlayerType.setInt(1, groupId);
            getPlayerType.setString(2, uuid.toString());
            try (ResultSet set = getPlayerType.executeQuery()) {
                return set.next() ? PlayerType.getPlayerType(set.getString(1)) : null;
            }
        } catch (final SQLException exception) {
            logger.log(Level.WARNING, "Problem getting player " + uuid + " type within group " + groupId, exception);
        }
        return null;
    }

    public List<UUID> getAllMembers(final String groupName, final PlayerType role) {
        final List<UUID> members = new ArrayList<>();
        try (Connection connection = db.getConnection();
             PreparedStatement getMembers = connection.prepareStatement(GET_MEMBERS)) {
            getMembers.setString(1, groupName);
            getMembers.setString(2, role.name());
            try (ResultSet set = getMembers.executeQuery()) {
                while (set.next()) {
                    final String uuid = set.getString(1);
                    if (uuid != null) {
                        members.add(UUID.fromString(uuid));
                    }
                }
            }
        } catch (final SQLException exception) {
            logger.log(Level.WARNING, "Problem getting all " + role + " for group " + groupName, exception);
        }
        return members;
    }

    public Map<PlayerType, List<PermissionType>> getPermissions(final String group) {
        final Map<PlayerType, List<PermissionType>> perms = new HashMap<>();
        try (Connection connection = db.getConnection();
             PreparedStatement getPermission = connection.prepareStatement(GET_PERMISSION)) {
            getPermission.setString(1, group);
            try (ResultSet set = getPermission.executeQuery()) {
                while (set.next()) {
                    final PlayerType type = PlayerType.getPlayerType(set.getString(1));
                    final String name = set.getString(2);
                    final PermissionType perm = PermissionType.getPermission(name);
                    if (type != null && perm != null) {
                        final List<PermissionType> listPerm = perms.computeIfAbsent(type, key -> new ArrayList<>());
                        if (!listPerm.contains(perm)) {
                            listPerm.add(perm);
                        }
                    }
                }
            }
        } catch (final SQLException exception) {
            logger.log(Level.WARNING, "Problem getting permissions for group " + group, exception);
        }
        return perms;
    }

    public int countGroups() {
        try (Connection connection = db.getConnection();
             Statement countGroups = connection.createStatement();
             ResultSet set = countGroups.executeQuery(COUNT_GROUPS)) {
            return set.next() ? set.getInt("count") : 0;
        } catch (final SQLException exception) {
            logger.log(Level.WARNING, "Problem counting groups", exception);
            return 0;
        }
    }

    public int countGroups(final UUID uuid) {
        try (Connection connection = db.getConnection();
             PreparedStatement countGroupsFromUUID = connection.prepareStatement(COUNT_GROUPS_FROM_UUID)) {
            countGroupsFromUUID.setString(1, uuid.toString());
            try (ResultSet set = countGroupsFromUUID.executeQuery()) {
                return set.next() ? set.getInt("count") : 0;
            }
        } catch (final SQLException exception) {
            logger.log(Level.WARNING, "Problem counting groups for " + uuid, exception);
            return 0;
        }
    }

    public Set<UUID> loadAllAutoAccept() {
        final Set<UUID> accepts = new HashSet<>();
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(LOAD_ALL_AUTO_ACCEPT);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                accepts.add(UUID.fromString(resultSet.getString(1)));
            }
        } catch (final SQLException exception) {
            logger.log(Level.WARNING, "Problem loading all autoaccepts", exception);
        }
        return accepts;
    }

    @Deprecated
    public boolean shouldAutoAcceptGroups(final UUID uuid) {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_AUTO_ACCEPT)) {
            statement.setString(1, uuid.toString());
            try (ResultSet set = statement.executeQuery()) {
                return set.next();
            }
        } catch (final SQLException exception) {
            logger.log(Level.WARNING, "Problem getting autoaccept for " + uuid, exception);
        }
        return false;
    }

    public String getDefaultGroup(final UUID uuid) {
        try (Connection connection = db.getConnection();
             PreparedStatement getDefaultGroup = connection.prepareStatement(GET_DEFAULT_GROUP)) {
            getDefaultGroup.setString(1, uuid.toString());
            try (ResultSet set = getDefaultGroup.executeQuery()) {
                return set.next() ? set.getString(1) : null;
            }
        } catch (final SQLException exception) {
            logger.log(Level.WARNING, "Problem getting default group for " + uuid, exception);
            return null;
        }
    }

    public Map<UUID, String> getAllDefaultGroups() {
        final Map<UUID, String> groups = new TreeMap<>();
        try (Connection connection = db.getConnection();
             Statement getAllDefaultGroups = connection.createStatement();
             ResultSet set = getAllDefaultGroups.executeQuery(GET_ALL_DEFAULT_GROUPS)) {
            while (set.next()) {
                groups.put(UUID.fromString(set.getString(1)), set.getString(2));
            }
        } catch (final SQLException exception) {
            logger.log(Level.WARNING, "Problem getting all default groups", exception);
        }
        return groups;
    }

    public void loadGroupInvitation(final UUID playerUUID, final Group group) {
        if (group == null) {
            return;
        }
        try (Connection connection = db.getConnection();
             PreparedStatement loadGroupInvitation = connection.prepareStatement(LOAD_GROUP_INVITATION)) {
            loadGroupInvitation.setString(1, playerUUID.toString());
            loadGroupInvitation.setString(2, group.getName());
            try (ResultSet set = loadGroupInvitation.executeQuery()) {
                while (set.next()) {
                    final String role = set.getString("role");
                    final PlayerType type = role == null ? null : PlayerType.getPlayerType(role);
                    group.addInvite(playerUUID, type, false);
                }
            }
        } catch (final SQLException exception) {
            logger.log(Level.WARNING, "Problem loading group " + group.getName() + " invites for " + playerUUID, exception);
        }
    }

    public Map<UUID, PlayerType> getInvitesForGroup(final String groupName) {
        final Map<UUID, PlayerType> invitations = new TreeMap<>();
        if (groupName == null) {
            return invitations;
        }
        try (Connection connection = db.getConnection();
             PreparedStatement loadGroupInvitationsForGroup = connection.prepareStatement(LOAD_GROUP_INVITATIONS_FOR_GROUP)) {
            loadGroupInvitationsForGroup.setString(1, groupName);
            try (ResultSet set = loadGroupInvitationsForGroup.executeQuery()) {
                while (set.next()) {
                    final String uuid = set.getString(1);
                    final String role = set.getString(2);
                    final PlayerType type = role == null ? null : PlayerType.getPlayerType(role);
                    if (uuid != null && type != null) {
                        invitations.put(UUID.fromString(uuid), type);
                    }
                }
            }
        } catch (final SQLException exception) {
            logger.log(Level.WARNING, "Problem loading group invitations for group " + groupName, exception);
        }
        return invitations;
    }

    public void loadGroupsInvitations() {
        try (Connection connection = db.getConnection();
             PreparedStatement loadGroupsInvitations = connection.prepareStatement(LOAD_GROUPS_INVITATIONS);
             ResultSet set = loadGroupsInvitations.executeQuery()) {
            while (set.next()) {
                final String uuid = set.getString("uuid");
                final String group = set.getString("groupName");
                final String role = set.getString("role");
                final Group cachedGroup = group == null ? null : GroupManager.getGroup(group);
                final PlayerType type = role == null ? null : PlayerType.getPlayerType(role);
                if (uuid != null && cachedGroup != null) {
                    final UUID playerUUID = UUID.fromString(uuid);
                    cachedGroup.addInvite(playerUUID, type, false);
                    PlayerListener.addNotification(playerUUID, cachedGroup);
                }
            }
        } catch (final SQLException exception) {
            logger.log(Level.WARNING, "Problem loading all group invitations", exception);
        }
    }

    public void logNameChange(final UUID uuid, final String oldName, final String newName) {
        try (Connection connection = db.getConnection();
             PreparedStatement logNameChange = connection.prepareStatement(LOG_NAME_CHANGE)) {
            logNameChange.setString(1, uuid.toString());
            logNameChange.setString(2, oldName);
            logNameChange.setString(3, newName);
            logNameChange.executeUpdate();
        } catch (final SQLException exception) {
            logger.log(Level.WARNING, "Failed to log a name change for " + uuid + " from " + oldName + " -> " + newName, exception);
        }
    }

    public boolean hasChangedNameBefore(final UUID uuid) {
        try (Connection connection = db.getConnection();
             PreparedStatement checkForNameChange = connection.prepareStatement(CHECK_FOR_NAME_CHANGE)) {
            checkForNameChange.setString(1, uuid.toString());
            try (ResultSet set = checkForNameChange.executeQuery()) {
                return set.next();
            }
        } catch (final SQLException exception) {
            logger.log(Level.WARNING, "Failed to check if " + uuid + " has previously changed names", exception);
        }
        return false;
    }

    public Set<UUID> getBlackListMembers(final String groupName) {
        final Set<UUID> uuids = new HashSet<>();
        try (Connection connection = db.getConnection();
             PreparedStatement getBlackListMembers = connection.prepareStatement(GET_BLACKLIST_MEMBERS)) {
            getBlackListMembers.setString(1, groupName);
            try (ResultSet set = getBlackListMembers.executeQuery()) {
                while (set.next()) {
                    uuids.add(UUID.fromString(set.getString(1)));
                }
            }
        } catch (final SQLException exception) {
            logger.log(Level.WARNING, "Unable to retrieve black list members for group " + groupName, exception);
        }
        return uuids;
    }

    public List<Integer> getAllIDs(final String groupName) {
        if (groupName == null) {
            return null;
        }
        try (Connection connection = db.getConnection();
             PreparedStatement getGroupIDs = connection.prepareStatement(GET_GROUP_IDS)) {
            getGroupIDs.setString(1, groupName);
            try (ResultSet set = getGroupIDs.executeQuery()) {
                final List<Integer> ids = new ArrayList<>();
                while (set.next()) {
                    ids.add(set.getInt(1));
                }
                return ids;
            }
        } catch (final SQLException exception) {
            logger.log(Level.WARNING, "Unable to fully load group ID set", exception);
        }
        return null;
    }

    public GroupLoadSnapshot loadAllGroupsSnapshot() {
        final Map<String, GroupHeader> headers = new LinkedHashMap<>();
        final Map<String, List<Integer>> groupIds = new HashMap<>();
        final Map<String, Map<UUID, PlayerType>> members = new HashMap<>();
        final Map<String, Set<UUID>> blacklists = new HashMap<>();
        final long cacheVersion;

        try (Connection connection = db.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(LOAD_ALL_GROUP_HEADERS);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    final String name = resultSet.getString(1);
                    if (headers.containsKey(name)) {
                        continue;
                    }
                    final String ownerString = resultSet.getString(2);
                    final UUID owner = ownerString == null ? null : UUID.fromString(ownerString);
                    final Timestamp timestamp = resultSet.getTimestamp(6);
                    headers.put(name, new GroupHeader(
                        name,
                        owner,
                        resultSet.getInt(4) != 0,
                        resultSet.getString(3),
                        resultSet.getInt(5),
                        timestamp == null ? System.currentTimeMillis() : timestamp.getTime(),
                        resultSet.getString(7)));
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(LOAD_ALL_GROUP_IDS);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    groupIds.computeIfAbsent(resultSet.getString(1), key -> new ArrayList<>()).add(resultSet.getInt(2));
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(LOAD_ALL_MEMBERS);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    final String uuidString = resultSet.getString(2);
                    final PlayerType role = PlayerType.getPlayerType(resultSet.getString(3));
                    if (uuidString != null && role != null) {
                        members.computeIfAbsent(resultSet.getString(1), key -> new HashMap<>())
                            .put(UUID.fromString(uuidString), role);
                    }
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(LOAD_ALL_BLACKLISTS);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    final String uuidString = resultSet.getString(2);
                    if (uuidString != null) {
                        blacklists.computeIfAbsent(resultSet.getString(1), key -> new HashSet<>())
                            .add(UUID.fromString(uuidString));
                    }
                }
            }
            cacheVersion = loadCacheVersion(connection);
            connection.commit();
        } catch (final SQLException exception) {
            logger.log(Level.WARNING, "Unable to bulk load NameLayer groups", exception);
            return new GroupLoadSnapshot(new ArrayList<>(), 0L);
        }

        final List<Group> groups = new ArrayList<>(headers.size());
        for (final GroupHeader header : headers.values()) {
            final List<Integer> ids = groupIds.get(header.name());
            final int primaryId = ids == null || ids.isEmpty() ? header.groupId() : ids.get(0);
            groups.add(new Group(
                header.name(),
                header.owner(),
                header.disciplined(),
                header.password(),
                primaryId,
                header.activityTimestamp(),
                header.groupColor(),
                ids,
                members.get(header.name()),
                blacklists.get(header.name())));
        }
        return new GroupLoadSnapshot(groups, cacheVersion);
    }
}
