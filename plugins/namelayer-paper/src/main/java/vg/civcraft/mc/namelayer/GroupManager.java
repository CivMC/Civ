package vg.civcraft.mc.namelayer;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import net.civmc.namelayer.sync.NameLayerWriteOperation;
import net.civmc.namelayer.sync.NameLayerWriteRequest;
import net.civmc.namelayer.sync.NameLayerWriteResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.cache.NameLayerGroupCache;
import vg.civcraft.mc.namelayer.database.NameLayerReadDao;
import vg.civcraft.mc.namelayer.events.GroupCreateEvent;
import vg.civcraft.mc.namelayer.events.GroupDeleteEvent;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.GroupPermission;
import vg.civcraft.mc.namelayer.permission.PermissionHandler;
import vg.civcraft.mc.namelayer.permission.PermissionType;
import vg.civcraft.mc.namelayer.rabbitmq.NameLayerWriteClient;

public class GroupManager {

    private static NameLayerReadDao nameLayerReadDao;
    private PermissionHandler permhandle;

    private static Map<String, Group> groupsByName = new ConcurrentHashMap<>();
    private static Map<Integer, Group> groupsById = new ConcurrentHashMap<>();

    private static boolean mergingInProgress = false;

    public GroupManager() {
        nameLayerReadDao = NameLayerPlugin.getNameLayerReadDao();
        permhandle = new PermissionHandler();
    }

    private static NameLayerGroupCache getCache() {
        return NameLayerPlugin.getGroupCache();
    }

    public static void fullResyncCache() {
        NameLayerPlugin.fullResyncGroupCache();
    }

    public static boolean reloadGroupById(final int groupId) {
        return reloadGroupsById(List.of(groupId));
    }

    public static boolean reloadGroupsById(final List<Integer> groupIds) {
        if (groupIds == null) {
            NameLayerPlugin.recordTargetedReloadFailure(0);
            return false;
        }
        final long startedAtMillis = System.currentTimeMillis();
        final Set<Integer> uniqueGroupIds = new LinkedHashSet<>(groupIds);
        final NameLayerReadDao.GroupReloadSnapshot snapshot = nameLayerReadDao.loadGroupsByIdsSnapshot(uniqueGroupIds);
        if (snapshot == null) {
            NameLayerPlugin.recordTargetedReloadFailure(uniqueGroupIds.size());
            return false;
        }
        final Map<Integer, Group> groups = snapshot.groups();
        final NameLayerGroupCache cache = getCache();
        if (cache == null) {
            NameLayerPlugin.recordTargetedReloadFailure(uniqueGroupIds.size());
            return false;
        }
        for (final int groupId : uniqueGroupIds) {
            cache.replaceGroupById(groupId, groups.get(groupId));
        }
        cache.setAppliedVersion(snapshot.cacheVersion());
        NameLayerPlugin.recordTargetedReload(uniqueGroupIds.size(), System.currentTimeMillis() - startedAtMillis);
        return true;
    }

    public void createGroupAsync(
        final UUID actorUuid,
        final String groupName,
        final String password,
        final Consumer<GroupWriteResult> callback
    ) {
        final GroupCreateEvent event = new GroupCreateEvent(groupName, actorUuid, password);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            completeGroupWriteOnMain(callback, GroupWriteResult.failure("Group create was cancelled"));
            return;
        }
        final Map<String, String> arguments = new HashMap<>();
        arguments.put("groupName", event.getGroupName());
        arguments.put("hasPassword", Boolean.toString(event.getPassword() != null));
        if (event.getPassword() != null) {
            arguments.put("password", event.getPassword());
        }
        arguments.put("defaultPermissions", encodeDefaultPermissions(PermissionType.getAllPermissions()));
        sendGroupWrite(actorUuid, NameLayerWriteOperation.CREATE_GROUP, arguments, callback);
    }

    public void ensureNewfriendGroupAsync(
        final UUID playerUuid,
        final String baseName,
        final Consumer<GroupWriteResult> callback
    ) {
        final Map<String, String> arguments = new HashMap<>();
        arguments.put("baseName", baseName);
        arguments.put("defaultPermissions", encodeDefaultPermissions(PermissionType.getAllPermissions()));
        sendGroupWrite(playerUuid, NameLayerWriteOperation.ENSURE_NEWFRIEND_GROUP, arguments, callback);
    }

    public void deleteGroupAsync(
        final UUID actorUuid,
        final Group group,
        final boolean adminOverride,
        final Consumer<GroupWriteResult> callback
    ) {
        if (group == null) {
            completeGroupWriteOnMain(callback, GroupWriteResult.failure("Group does not exist"));
            return;
        }
        GroupDeleteEvent event = new GroupDeleteEvent(group, false);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            completeGroupWriteOnMain(callback, GroupWriteResult.failure("Group delete was cancelled"));
            return;
        }
        sendGroupWrite(
            actorUuid,
            NameLayerWriteOperation.DELETE_GROUP,
            Map.of(
                "groupId", Integer.toString(group.getGroupId()),
                "adminOverride", Boolean.toString(adminOverride)
            ),
            result -> {
                if (result.success()) {
                    final GroupDeleteEvent finishedEvent = new GroupDeleteEvent(group, true);
                    Bukkit.getPluginManager().callEvent(finishedEvent);
                }
                callback.accept(result);
            }
        );
    }

    private void sendGroupWrite(
        final UUID actorUuid,
        final NameLayerWriteOperation operation,
        final Map<String, String> arguments,
        final Consumer<GroupWriteResult> callback
    ) {
        final NameLayerWriteClient writeClient = NameLayerPlugin.getWriteClient();
        if (writeClient == null) {
            completeGroupWriteOnMain(callback, GroupWriteResult.failure("NameLayer proxy write client is unavailable"));
            return;
        }
        final NameLayerPlugin plugin = NameLayerPlugin.getInstance();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            final NameLayerWriteRequest request = NameLayerWriteRequest.create(
                plugin.getConfig().getString("rabbitmq.serverId", "paper"),
                actorUuid,
                operation,
                arguments
            );
            writeClient.send(request).whenComplete((response, error) -> handleGroupWriteResponse(response, error, callback));
        });
    }

    private void handleGroupWriteResponse(
        final NameLayerWriteResponse response,
        final Throwable error,
        final Consumer<GroupWriteResult> callback
    ) {
        if (error != null) {
            NameLayerPlugin.getInstance().getLogger().log(Level.WARNING, "NameLayer group proxy write failed", error);
            completeGroupWriteOnMain(callback, GroupWriteResult.failure("NameLayer proxy write failed"));
            return;
        }
        if (!response.success()) {
            completeGroupWriteOnMain(callback, GroupWriteResult.failure(response.message()));
            return;
        }
        final boolean reloadSucceeded;
        if (response.requiresFullResync()) {
            NameLayerPlugin.fullResyncGroupCache();
            reloadSucceeded = true;
        } else {
            reloadSucceeded = reloadGroupsById(List.copyOf(response.affectedGroupIds()));
        }
        if (!reloadSucceeded) {
            completeGroupWriteOnMain(callback, GroupWriteResult.failure("Group write succeeded, but local cache refresh failed"));
            return;
        }
        final Group group = response.affectedGroupIds().isEmpty() ? null : getGroup(response.affectedGroupIds().iterator().next());
        completeGroupWriteOnMain(callback, GroupWriteResult.successResult(group));
    }

    private void completeGroupWriteOnMain(final Consumer<GroupWriteResult> callback, final GroupWriteResult result) {
        Bukkit.getScheduler().runTask(NameLayerPlugin.getInstance(), () -> callback.accept(result));
    }

    private String encodeDefaultPermissions(final Collection<PermissionType> permissions) {
        final StringBuilder builder = new StringBuilder();
        for (final PermissionType permission : permissions) {
            for (final PlayerType playerType : permission.getDefaultPermLevels()) {
                if (!builder.isEmpty()) {
                    builder.append(';');
                }
                builder.append(playerType.name()).append(':').append(permission.getName());
            }
        }
        return builder.toString();
    }

    public record GroupWriteResult(boolean success, String message, Group group) {

        public static GroupWriteResult successResult(final Group group) {
            return new GroupWriteResult(true, "", group);
        }

        public static GroupWriteResult failure(final String message) {
            return new GroupWriteResult(false, message == null || message.isBlank() ? "Group write failed" : message, null);
        }
    }

    /**
     * This will create a group asynchronously. Always saves to database. Pass in a callback that specifies what to run
     * <i>synchronously</i> after the insertion of the group. Your callback should handle the case where
     * id = -1 (failure).
     * <p>
     * Note that your run() method should use the passed group's getGroupId() to retrieve the created group ID and react to it.
     *
     * @param group             the Group placeholder to use in creating a group. Calls GroupCreateEvent synchronously, then insert the
     *                          group asynchronously, then calls the callback synchronously.
     * @param postCreate        The callback to run after insertion (whether successful or not!)
     * @param checkBeforeCreate Checks if the group already exists (asynchronously) prior to creating it. Runs the CreateEvent
     *                          synchronously, then behaves as normal after that (running async create).
     */
    public void createGroupAsync(final Group group, final Consumer<Group> postCreate, boolean checkBeforeCreate) {
        if (group == null) {
            NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "Group create failed, caller passed in null", new Exception());
            Bukkit.getScheduler().runTask(NameLayerPlugin.getInstance(), () -> postCreate.accept(null));
            return;
        }
        if (checkBeforeCreate && getGroup(group.getName()) != null) {
            NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "Group create failed, group {0} already exists", group.getName());
            Bukkit.getScheduler().runTask(NameLayerPlugin.getInstance(), () -> postCreate.accept(null));
            return;
        }
        createGroupAsync(group.getOwner(), group.getName(), group.getPassword(), result -> {
            Group createdGroup = result.group();
            if (!result.success() || createdGroup == null) {
                createdGroup = null;
            }
            postCreate.accept(createdGroup);
        });
    }

    /*
     * Making this static so I can use it in other places without needing the GroupManager Object.
     * Saves me code so I can always grab a group if it is already loaded while not needing to check db.
     */
    public static Group getGroup(String name) {
        if (name == null) {
            NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "getGroup failed, caller passed in null", new Exception());
            return null;
        }

        String lower = name.toLowerCase();
        if (getCache() != null) {
            Group group = getCache().getByName(lower);
            if (group != null) {
                return group;
            }
        } else if (groupsByName.containsKey(lower)) {
            return groupsByName.get(lower);
        }

        Group group = nameLayerReadDao.getGroup(name);
        if (group != null) {
            if (getCache() != null) {
                getCache().putGroup(group);
            } else {
                groupsByName.put(lower, group);
                for (int j : group.getGroupIds()) {
                    groupsById.put(j, group);
                }
            }
        } else {
            NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "getGroup by Name failed, unable to find the group " + name);
        }
        return group;
    }

    public static Group getGroup(int groupId) {
        if (getCache() != null) {
            Group group = getCache().getById(groupId);
            if (group != null) {
                return group;
            }
        } else if (groupsById.containsKey(groupId)) {
            return groupsById.get(groupId);
        }

        Group group = nameLayerReadDao.getGroup(groupId);
        if (group != null) {
            if (getCache() != null) {
                getCache().putGroup(group);
            } else {
                groupsByName.put(group.getName().toLowerCase(), group);
                for (int j : group.getGroupIds()) {
                    groupsById.put(j, group);
                }
            }
        } else {
            NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "getGroup by ID failed, unable to find the group " + groupId);
        }
        return group;
    }

    public static boolean hasGroup(String groupName) {
        if (groupName == null) {
            NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "HasGroup Name failed, name was null ", new Exception());
            return false;
        }

        if (getCache() != null) {
            return getCache().containsName(groupName) || getGroup(groupName.toLowerCase()) != null;
        } else if (!groupsByName.containsKey(groupName.toLowerCase())) {
            return (getGroup(groupName.toLowerCase()) != null);
        } else {
            return true;
        }
    }

    /**
     * Returns the admin group for groups if the group was found to be null.
     * Good for when you have to have a group that can't be null.
     *
     * @param name - The group name for the group
     * @return Either the group or the special admin group.
     */
    public static Group getSpecialCircumstanceGroup(String name) {
        if (name == null) {
            NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "getSpecialCircumstance failed, caller passed in null", new Exception());
            return null;
        }
        String lower = name.toLowerCase();
        if (getCache() != null) {
            Group group = getCache().getByName(lower);
            if (group != null) {
                return group;
            }
        } else if (groupsByName.containsKey(lower)) {
            return groupsByName.get(lower);
        }

        Group group = nameLayerReadDao.getGroup(name);
        if (group != null) {
            if (getCache() != null) {
                getCache().putGroup(group);
            } else {
                groupsByName.put(lower, group);
                for (int j : group.getGroupIds()) {
                    groupsById.put(j, group);
                }
            }
        } else {
            group = nameLayerReadDao.getGroup(NameLayerPlugin.getSpecialAdminGroup());
            if (group != null && getCache() != null) {
                getCache().putGroup(group);
            }
        }
        return group;
    }

    /**
     * DO NOT WORK WITH THE PERMISSION OBJECT ITSELF TO DETERMINE ACCESS. Use the methods provided in this class instead, as they
     * respect all the permission inheritation stuff
     *
     * @param group the group to retrieve permissions from
     * @return the actual permissions for this object or null
     */
    public GroupPermission getPermissionforGroup(Group group) {
        if (group == null) {
            NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "getPermissionForGroup failed, caller passed in null", new Exception());
            return null;
        }
        return permhandle.getGroupPermission(group);
    }

    public boolean hasAccess(String groupname, UUID player, PermissionType perm) {
        if (groupname == null) {
            NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "hasAccess failed (access denied), could not find group " + groupname);
            return false;
        }
        return hasAccess(getGroup(groupname), player, perm);
    }

    public boolean hasAccess(Group group, UUID player, PermissionType perm) {
        Player p = Bukkit.getPlayer(player);
        if (p != null && (p.isOp() || p.hasPermission("namelayer.admin"))) {
            return true;
        }
        if (group == null || perm == null) {
            NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "hasAccess failed, caller passed in null", new Exception());
            return false;
        }
        if (!group.isValid()) {
            group = getGroup(group.getName());
            if (group == null) {
                //what happened? who knows?
                return false;
            }
        }
        if (group.isOwner(player) && perm.isOwnerPermission()) {
            return true;
        }
        PlayerType type = group.getPlayerType(player);
        return type != null && getPermissionforGroup(group).hasPermission(type, perm);
    }

    // == PERMISSION HANDLING ============================================================= //

    public List<String> getAllGroupNames(UUID uuid) {
        if (uuid == null) {
            NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "getAllGroupNames failed, caller passed in null", new Exception());
            return new ArrayList<>();
        }
        if (getCache() != null) {
            List<String> groups = getCache().getGroupNames(uuid);
            if (!groups.isEmpty()) {
                return groups;
            }
        }
        return nameLayerReadDao.getGroupNames(uuid);
    }

    public String getDefaultGroup(UUID uuid) {
        if (uuid == null) {
            NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "getDefaultGroup was cancelled, caller passed in null", new Exception());
            return null;
        }
        return NameLayerPlugin.getDefaultGroupHandler().getDefaultGroup(uuid);
    }

    /**
     * Invalidates a group from cache.
     *
     * @param group the group to invalidate cache for
     */
    public static void invalidateCache(String group) {
        if (group == null) {
            NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "invalidateCache failed, caller passed in null", new Exception());
            return;
        }

        Group g = groupsByName.get(group.toLowerCase());
        if (getCache() != null) {
            g = getCache().getByName(group);
        }
        if (g != null) {
            if (getCache() != null) {
                getCache().removeGroup(g);
            } else {
                List<Integer> k = g.getGroupIds();
                groupsByName.remove(group.toLowerCase());
                boolean fail = true;
                for (int j : k) {
                    if (groupsById.remove(j) != null) {
                        fail = false;
                    }
                }

                // FALLBACK is hardloop
                if (fail) { // can't find ID or cache is wrong.
                    for (Group x : groupsById.values()) {
                        if (x.getName().equals(g.getName())) {
                            groupsById.remove(x.getGroupId());
                        }
                    }
                }
            }
        } else {
            NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "Invalidate cache by name failed, unable to find the group " + group);
        }
    }

    public static void invalidateCache(int groupId) {
        if (getCache() != null) {
            getCache().removeGroupById(groupId);
            return;
        }
        Group group = groupsById.remove(groupId);
        if (group != null) {
            groupsByName.remove(group.getName().toLowerCase());
        }
    }

    public int countGroups(UUID uuid) {
        if (uuid == null) {
            NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "countGroups failed, caller passed in null", new Exception());
            return 0;
        }
        return nameLayerReadDao.countGroups(uuid);
    }

    /**
     * In ascending order
     * Add an enum here if you wish to add more than the four default tiers of
     * roles.
     */
    public enum PlayerType {
        MEMBERS,
        MODS,
        ADMINS,
        OWNER,
        NOT_BLACKLISTED;//anyone, who is not blacklisted

        private final static Map<String, PlayerType> BY_NAME = Maps.newHashMap();

        static {
            for (PlayerType rank : values()) {
                BY_NAME.put(rank.name(), rank);
            }
        }

        public static PlayerType getPlayerType(String type) {
            return BY_NAME.get(type.toUpperCase());
        }

        public static String getStringOfTypes() {
            StringBuilder ranks = new StringBuilder();
            for (String rank : BY_NAME.keySet()) {
                ranks.append(rank);
                ranks.append(" ");
            }
            return ranks.toString();
        }

        public static void displayPlayerTypes(CommandSender p) {
            p.sendMessage(ChatColor.RED
                + "That PlayerType does not exist.\n"
                + "The current types are: " + getStringOfTypes());
        }

        public static void displayPlayerTypesnllpt(Player p) {
            p.sendMessage(ChatColor.GREEN
                + "The current types are: " + getStringOfTypes());
            //dont yell at player for nllpt
        }

        public static PlayerType getByID(int id) {
            switch (id) {
                case 0:
                    return PlayerType.NOT_BLACKLISTED;
                case 1:
                    return PlayerType.MEMBERS;
                case 2:
                    return PlayerType.MODS;
                case 3:
                    return PlayerType.ADMINS;
                case 4:
                    return PlayerType.OWNER;
                default:
                    return null;
            }
        }

        public static int getID(PlayerType type) {
            if (type == null) {
                return -1;
            }
            switch (type) {
                case NOT_BLACKLISTED:
                    return 0;
                case MEMBERS:
                    return 1;
                case MODS:
                    return 2;
                case ADMINS:
                    return 3;
                case OWNER:
                    return 4;
                default:
                    return -1;
            }
        }

        public static String getNiceRankName(PlayerType pType) {
            if (pType == null) {
                return "RANK_ERROR";
            }
            switch (pType) {
                case MEMBERS:
                    return "Member";
                case MODS:
                    return "Mod";
                case ADMINS:
                    return "Admin";
                case OWNER:
                    return "Owner";
                case NOT_BLACKLISTED:
                    return "Anyone who is not blacklisted";
            }
            return "RANK_ERROR";
        }
    }
}
