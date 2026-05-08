package vg.civcraft.mc.namelayer.group;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import net.civmc.namelayer.sync.NameLayerWriteOperation;
import net.civmc.namelayer.sync.NameLayerWriteRequest;
import net.civmc.namelayer.sync.NameLayerWriteResponse;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameLayerAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.permission.PermissionType;
import vg.civcraft.mc.namelayer.rabbitmq.NameLayerWriteClient;

public final class Group {

    private final String name;
    private final String password;
    private final UUID owner;
    private final boolean isDisciplined; // if true, prevents any interactions with this group
    private final int id;
    private final Set<Integer> ids;
    private final Map<UUID, PlayerType> players;
    private final Map<UUID, PlayerType> invites;
    private final Set<UUID> blacklist;
    private final Map<PlayerType, List<PermissionType>> permissions;
    private final long activityTimestamp;
    private final TextColor groupColor;

    public Group(final String name, final UUID owner, final boolean disciplined, final String password, final int id,
                 final long activityTimestamp, final String groupColor) {
        this(name, owner, disciplined, password, id, activityTimestamp, groupColor, List.of(id), Map.of(), Set.of(),
            Map.of(), Map.of());
    }

    public Group(final String name, final UUID owner, final boolean disciplined, final String password, final int id,
                 final long activityTimestamp, final String groupColor, final List<Integer> groupIds,
                 final Map<UUID, PlayerType> members, final Set<UUID> blacklist,
                 final Map<UUID, PlayerType> invites,
                 final Map<PlayerType, List<PermissionType>> permissions) {
        this.name = name;
        this.password = password;
        this.owner = owner;
        this.isDisciplined = disciplined;
        this.activityTimestamp = activityTimestamp;
        this.id = id;
        final Set<Integer> copiedIds = new HashSet<>(groupIds);
        copiedIds.add(id);
        this.ids = Collections.unmodifiableSet(copiedIds);
        this.players = Map.copyOf(members);
        this.invites = Map.copyOf(invites);
        this.blacklist = Set.copyOf(blacklist);
        this.permissions = copyPermissions(permissions);
        this.groupColor = parseGroupColor(groupColor);
    }

    private static Map<PlayerType, List<PermissionType>> copyPermissions(
        final Map<PlayerType, List<PermissionType>> permissions
    ) {
        if (permissions == null || permissions.isEmpty()) {
            return Map.of();
        }
        final Map<PlayerType, List<PermissionType>> copy = new EnumMap<>(PlayerType.class);
        for (final Map.Entry<PlayerType, List<PermissionType>> entry : permissions.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            copy.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return Collections.unmodifiableMap(copy);
    }

    public Map<PlayerType, List<PermissionType>> getPermissions() {
        return permissions;
    }

    public List<PermissionType> getPermissions(final PlayerType type) {
        if (type == null) {
            return List.of();
        }
        return permissions.getOrDefault(type, List.of());
    }

    public boolean hasPermission(final PlayerType type, final PermissionType permission) {
        if (type == null || permission == null) {
            return false;
        }
        final List<PermissionType> rolePermissions = permissions.get(type);
        return rolePermissions != null && rolePermissions.contains(permission);
    }

    private TextColor parseGroupColor(final String groupColor) {
        if (groupColor == null) {
            return null;
        }
        TextColor color = NamedTextColor.NAMES.value(groupColor);
        if (color == null) {
            color = TextColor.fromHexString(groupColor);
        }
        return color;
    }

    public long getActivityTimeStamp() {
        return activityTimestamp;
    }

    /**
     * Returns all the uuids of the members in this group.
     *
     * @return Returns all the uuids.
     */
    public List<UUID> getAllMembers() {
        return Lists.newArrayList(players.keySet());
    }

    /**
     * Returns all the UUIDS of a group's PlayerType.
     *
     * @param type- The PlayerType of a group that you want the UUIDs of.
     * @return Returns all the UUIDS of the specific PlayerType.
     */
    public List<UUID> getAllMembers(PlayerType type) {
        List<UUID> uuids = Lists.newArrayList();
        for (Map.Entry<UUID, PlayerType> entry : players.entrySet()) {
            if (entry.getValue() == type) {
                uuids.add(entry.getKey());
            }
        }
        return uuids;
    }

    public Set<UUID> getBlacklist() {
        return new HashSet<>(blacklist);
    }

    public boolean isBlacklisted(UUID uuid) {
        return blacklist.contains(uuid);
    }

    /**
     * Returns all the uuids of the invitees in this group.
     *
     * @return Returns all the uuids.
     */
    public List<UUID> getAllInvites() {
        return new ArrayList<>(invites.keySet());
    }

    /**
     * Returns the cached map of invitees to their pending player type.
     */
    public Map<UUID, PlayerType> getInvitesByUuid() {
        return invites;
    }

    /**
     * Returns all the invited UUIDS of a group's PlayerType.
     *
     * @param type The PlayerType of a group that you want the UUIDs of.
     * @return Returns all the invited UUIDS of the specific PlayerType.
     */
    public List<UUID> getAllInvites(PlayerType type) {
        List<UUID> uuids = new ArrayList<>(invites.size());
        for (Map.Entry<UUID, PlayerType> entry : invites.entrySet()) {
            if (entry.getValue() == type) {
                uuids.add(entry.getKey());
            }
        }
        return uuids;
    }

    /**
     * Gives the uuids of the members whose name starts with the given
     * String, this is not case-sensitive
     *
     * @param prefix start of the players name
     * @return list of all players whose name starts with the given string
     */
    public List<UUID> getMembersByName(String prefix) {
        List<UUID> uuids = Lists.newArrayList();
        List<UUID> members = getAllMembers();

        prefix = prefix.toLowerCase();
        for (UUID member : members) {
            String name = NameLayerAPI.getCurrentName(member);
            if (name.toLowerCase().startsWith(prefix)) {
                uuids.add(member);
            }
        }
        return uuids;
    }

    /**
     * Gives a list of the members of this group, excluding the inherited members.
     *
     * @return List of UUIDs of the current players in this group
     */
    public List<UUID> getCurrentMembers() {
        return Lists.newArrayList(players.keySet());
    }

    public void addInviteAsync(
        final UUID actorUuid,
        final UUID invitedUuid,
        final PlayerType type,
        final boolean adminOverride,
        final boolean showInviter,
        final Consumer<MemberWriteResult> callback
    ) {
        if (type == PlayerType.NOT_BLACKLISTED) {
            completeMemberWriteOnMain(callback, MemberWriteResult.failure("Invalid invite role"));
            return;
        }
        sendMemberWrite(
            actorUuid,
            NameLayerWriteOperation.ADD_INVITATION,
            Map.of(
                "groupId", Integer.toString(getGroupId()),
                "memberUuid", invitedUuid.toString(),
                "role", type.name(),
                "adminOverride", Boolean.toString(adminOverride),
                "showInviter", Boolean.toString(showInviter)
            ),
            callback
        );
    }

    /**
     * Get's the PlayerType of an invited Player.
     *
     * @param uuid- The UUID of the player.
     * @return Returns the PlayerType or null.
     */
    public PlayerType getInvite(UUID uuid) {
        return invites.get(uuid);
    }

    public void removeInviteAsync(
        final UUID actorUuid,
        final UUID invitedUuid,
        final boolean adminOverride,
        final Consumer<MemberWriteResult> callback
    ) {
        sendMemberWrite(
            actorUuid,
            NameLayerWriteOperation.REMOVE_INVITATION,
            Map.of(
                "groupId", Integer.toString(getGroupId()),
                "memberUuid", invitedUuid.toString(),
                "adminOverride", Boolean.toString(adminOverride)
            ),
            callback
        );
    }

    public void acceptInviteAsync(final UUID actorUuid, final Consumer<MemberWriteResult> callback) {
        sendMemberWrite(
            actorUuid,
            NameLayerWriteOperation.ACCEPT_INVITATION,
            Map.of("groupId", Integer.toString(getGroupId())),
            callback
        );
    }

    /**
     * Checks if the player is in the Group or not.
     *
     * @param uuid- The UUID of the player.
     * @return Returns true if the player is a member, false otherwise.
     */
    public boolean isMember(UUID uuid) {
        return players.containsKey(uuid);
    }

    /**
     * Checks if the player is in the Group's PlayerType or not.
     *
     * @param uuid- The UUID of the player.
     * @param type- The PlayerType wanted.
     * @return Returns true if the player is a member of the specific playertype, otherwise false.
     */
    public boolean isMember(UUID uuid, PlayerType type) {
        if (players.containsKey(uuid))
            return players.get(uuid).equals(type);
        return false;
    }

    public boolean isCurrentMember(UUID uuid) {
        return players.containsKey(uuid);
    }

    public boolean isCurrentMember(UUID uuid, PlayerType rank) {
        if (players.containsKey(uuid)) {
            return players.get(uuid).equals(rank);
        }
        return false;
    }

    /**
     * @param uuid- The UUID of the player.
     * @return Returns the PlayerType of a member UUID.
     */
    @Nullable
    public PlayerType getPlayerType(UUID uuid) {
        PlayerType member = players.get(uuid);
        if (member != null) {
            return member;
        }
        if (isBlacklisted(uuid)) {
            return null;
        }
        return PlayerType.NOT_BLACKLISTED;
    }

    /**
     * @param uuid- The UUID of the player.
     * @return Returns the PlayerType of an invited UUID.
     */
    @Nullable
    public PlayerType getPlayerInviteType(UUID uuid) {
        PlayerType invitee = invites.get(uuid);
        if (invitee != null) {
            return invitee;
        }
        if (isBlacklisted(uuid)) {
            return null;
        }
        return PlayerType.NOT_BLACKLISTED;
    }


    public PlayerType getCurrentRank(UUID uuid) {
        return players.get(uuid);
    }

    public void joinGroupAsync(
        final UUID actorUuid,
        final String password,
        final PlayerType role,
        final Consumer<MemberWriteResult> callback
    ) {
        if (role == PlayerType.NOT_BLACKLISTED || role == PlayerType.OWNER) {
            completeMemberWriteOnMain(callback, MemberWriteResult.failure("Invalid password join role"));
            return;
        }
        sendMemberWrite(
            actorUuid,
            NameLayerWriteOperation.JOIN_GROUP,
            Map.of(
                "groupId", Integer.toString(getGroupId()),
                "password", password,
                "role", role.name()
            ),
            callback
        );
    }

    public void setMemberRoleAsync(
        final UUID actorUuid,
        final UUID memberUuid,
        final PlayerType role,
        final Consumer<MemberWriteResult> callback
    ) {
        if (role == PlayerType.NOT_BLACKLISTED || role == PlayerType.OWNER) {
            completeMemberWriteOnMain(callback, MemberWriteResult.failure("Invalid member role"));
            return;
        }
        sendMemberWrite(
            actorUuid,
            NameLayerWriteOperation.SET_MEMBER_ROLE,
            Map.of(
                "groupId", Integer.toString(getGroupId()),
                "memberUuid", memberUuid.toString(),
                "role", role.name()
            ),
            callback
        );
    }

    public void addMemberAsync(
        final UUID actorUuid,
        final UUID memberUuid,
        final PlayerType role,
        final Consumer<MemberWriteResult> callback
    ) {
        if (role == PlayerType.NOT_BLACKLISTED || role == PlayerType.OWNER) {
            completeMemberWriteOnMain(callback, MemberWriteResult.failure("Invalid member role"));
            return;
        }
        sendMemberWrite(
            actorUuid,
            NameLayerWriteOperation.ADD_MEMBER,
            Map.of(
                "groupId", Integer.toString(getGroupId()),
                "memberUuid", memberUuid.toString(),
                "role", role.name()
            ),
            callback
        );
    }

    public void removeMemberAsync(
        final UUID actorUuid,
        final UUID memberUuid,
        final Consumer<MemberWriteResult> callback
    ) {
        sendMemberWrite(
            actorUuid,
            NameLayerWriteOperation.REMOVE_MEMBER,
            Map.of(
                "groupId", Integer.toString(getGroupId()),
                "memberUuid", memberUuid.toString()
            ),
            callback
        );
    }

    private void sendMemberWrite(
        final UUID actorUuid,
        final NameLayerWriteOperation operation,
        final Map<String, String> arguments,
        final Consumer<MemberWriteResult> callback
    ) {
        final NameLayerWriteClient writeClient = NameLayerPlugin.getWriteClient();
        if (writeClient == null) {
            completeMemberWriteOnMain(callback, MemberWriteResult.failure("NameLayer proxy write client is unavailable"));
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
            writeClient.send(request).whenComplete((response, error) -> handleMemberWriteResponse(response, error, callback));
        });
    }

    private void handleMemberWriteResponse(
        final NameLayerWriteResponse response,
        final Throwable error,
        final Consumer<MemberWriteResult> callback
    ) {
        if (error != null) {
            NameLayerPlugin.getInstance().getLogger().log(Level.WARNING, "NameLayer member proxy write failed", error);
            completeMemberWriteOnMain(callback, MemberWriteResult.failure("NameLayer proxy write failed"));
            return;
        }
        if (!response.success()) {
            completeMemberWriteOnMain(callback, MemberWriteResult.failure(response.message()));
            return;
        }
        final boolean reloadSucceeded;
        if (response.requiresFullResync()) {
            NameLayerPlugin.fullResyncGroupCache();
            reloadSucceeded = true;
        } else {
            final Set<Integer> affectedGroupIds = response.affectedGroupIds().isEmpty()
                ? Set.of(getGroupId())
                : response.affectedGroupIds();
            reloadSucceeded = GroupManager.reloadGroupsById(List.copyOf(affectedGroupIds));
        }
        if (!reloadSucceeded) {
            completeMemberWriteOnMain(callback, MemberWriteResult.failure("Member write succeeded, but local cache refresh failed"));
            return;
        }
        completeMemberWriteOnMain(callback, MemberWriteResult.successResult());
    }

    private void completeMemberWriteOnMain(final Consumer<MemberWriteResult> callback, final MemberWriteResult result) {
        Bukkit.getScheduler().runTask(NameLayerPlugin.getInstance(), () -> callback.accept(result));
    }

    public record MemberWriteResult(boolean success, String message) {

        public static MemberWriteResult successResult() {
            return new MemberWriteResult(true, "");
        }

        public static MemberWriteResult failure(final String message) {
            return new MemberWriteResult(false, message == null || message.isBlank() ? "Member write failed" : message);
        }
    }

    public void setPasswordAsync(
        final UUID actorUuid,
        final String password,
        final Consumer<MetadataWriteResult> callback
    ) {
        final Map<String, String> arguments = new java.util.HashMap<>();
        arguments.put("groupId", Integer.toString(getGroupId()));
        arguments.put("hasValue", Boolean.toString(password != null));
        if (password != null) {
            arguments.put("value", password);
        }
        sendMetadataWrite(actorUuid, NameLayerWriteOperation.SET_GROUP_PASSWORD, arguments, callback);
    }

    public void setOwnerAsync(
        final UUID actorUuid,
        final UUID ownerUuid,
        final int maxGroups,
        final boolean adminOverride,
        final Consumer<MetadataWriteResult> callback
    ) {
        sendMetadataWrite(
            actorUuid,
            NameLayerWriteOperation.SET_GROUP_OWNER,
            Map.of(
                "groupId", Integer.toString(getGroupId()),
                "ownerUuid", ownerUuid.toString(),
                "maxGroups", Integer.toString(maxGroups),
                "adminOverride", Boolean.toString(adminOverride)
            ),
            callback
        );
    }

    public void setDisciplinedAsync(
        final UUID actorUuid,
        final boolean disciplined,
        final boolean adminOverride,
        final Consumer<MetadataWriteResult> callback
    ) {
        sendMetadataWrite(
            actorUuid,
            NameLayerWriteOperation.SET_GROUP_DISCIPLINE,
            Map.of(
                "groupId", Integer.toString(getGroupId()),
                "value", Boolean.toString(disciplined),
                "adminOverride", Boolean.toString(adminOverride)
            ),
            callback
        );
    }

    public void setGroupColorAsync(
        final UUID actorUuid,
        final TextColor groupColor,
        final boolean adminOverride,
        final Consumer<MetadataWriteResult> callback
    ) {
        sendMetadataWrite(
            actorUuid,
            NameLayerWriteOperation.SET_GROUP_COLOR,
            Map.of(
                "groupId", Integer.toString(getGroupId()),
                "value", groupColor.toString(),
                "adminOverride", Boolean.toString(adminOverride)
            ),
            callback
        );
    }

    private void sendMetadataWrite(
        final UUID actorUuid,
        final NameLayerWriteOperation operation,
        final Map<String, String> arguments,
        final Consumer<MetadataWriteResult> callback
    ) {
        final NameLayerWriteClient writeClient = NameLayerPlugin.getWriteClient();
        if (writeClient == null) {
            completeMetadataWriteOnMain(callback, MetadataWriteResult.failure("NameLayer proxy write client is unavailable"));
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
            writeClient.send(request).whenComplete((response, error) -> handleMetadataWriteResponse(response, error, callback));
        });
    }

    private void handleMetadataWriteResponse(
        final NameLayerWriteResponse response,
        final Throwable error,
        final Consumer<MetadataWriteResult> callback
    ) {
        if (error != null) {
            NameLayerPlugin.getInstance().getLogger().log(Level.WARNING, "NameLayer metadata proxy write failed", error);
            completeMetadataWriteOnMain(callback, MetadataWriteResult.failure("NameLayer proxy write failed"));
            return;
        }
        if (!response.success()) {
            completeMetadataWriteOnMain(callback, MetadataWriteResult.failure(response.message()));
            return;
        }
        final boolean reloadSucceeded;
        if (response.requiresFullResync()) {
            NameLayerPlugin.fullResyncGroupCache();
            reloadSucceeded = true;
        } else {
            final Set<Integer> affectedGroupIds = response.affectedGroupIds().isEmpty()
                ? Set.of(getGroupId())
                : response.affectedGroupIds();
            reloadSucceeded = GroupManager.reloadGroupsById(List.copyOf(affectedGroupIds));
        }
        if (!reloadSucceeded) {
            completeMetadataWriteOnMain(callback, MetadataWriteResult.failure("Metadata write succeeded, but local cache refresh failed"));
            return;
        }
        completeMetadataWriteOnMain(callback, MetadataWriteResult.successResult());
    }

    private void completeMetadataWriteOnMain(final Consumer<MetadataWriteResult> callback, final MetadataWriteResult result) {
        Bukkit.getScheduler().runTask(NameLayerPlugin.getInstance(), () -> callback.accept(result));
    }

    public record MetadataWriteResult(boolean success, String message) {

        public static MetadataWriteResult successResult() {
            return new MetadataWriteResult(true, "");
        }

        public static MetadataWriteResult failure(final String message) {
            return new MetadataWriteResult(false, message == null || message.isBlank() ? "Metadata write failed" : message);
        }
    }

    public void setDefaultGroupAsync(UUID uuid, Consumer<DefaultGroupHandler.DefaultGroupWriteResult> callback) {
        NameLayerPlugin.getDefaultGroupHandler().setDefaultGroupAsync(uuid, this, callback);
    }

    public void changeDefaultGroupAsync(UUID uuid, Consumer<DefaultGroupHandler.DefaultGroupWriteResult> callback) {
        setDefaultGroupAsync(uuid, callback);
    }

    // == GETTERS ========================================================================= //

    /**
     * @return Returns the group name.
     */
    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    /**
     * @return The UUID of the owner of the group.
     */
    public UUID getOwner() {
        return owner;
    }

    /**
     * @param uuid the uuid of owner
     * @return true if the UUID belongs to the owner of the group, false otherwise.
     */
    public boolean isOwner(UUID uuid) {
        if (owner == null) {
            return false;
        }
        return owner.equals(uuid);
    }

    public boolean isDisciplined() {
        return isDisciplined;
    }

    public boolean isValid() {
        if (name == null) {
            return false;
        }
        if (NameLayerPlugin.getGroupCache() == null) {
            return true;
        }
        return NameLayerPlugin.getGroupCache().getById(id) == this;
    }

    /**
     * Gets the id for a group.
     * <p>
     * <b>Note:</b>
     * Keep in mind though if you are trying to get a group_id from a GroupCreateEvent event
     * it will not be accurate. You must have a delay for 1 tick for it to work correctly.
     * <p>
     * Also calling the GroupManager.getGroup(int) will return a group that either has that
     * group id or the object associated with that id. As such if a group is previously called
     * and which didn't have the same id as the one called now you could get a different group id.
     * Example would be System.out.println(GroupManager.getGroup(1).getGroupId()) and that
     * could equal something like 2.
     *
     * @return the group id for a group.
     */
    public int getGroupId() {
        return id;
    }

    /**
     * Addresses issue above somewhat. Allows implementations that need the whole list of Ids
     * associated with this groupname to get them.
     *
     * @return list of ids paired with this group name.
     */
    public List<Integer> getGroupIds() {
        return new ArrayList<>(this.ids);
    }

    public TextColor getGroupColor() {
        return groupColor;
    }

    public Component getGroupNameColored() {
        return Component.text(this.name, this.groupColor);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Group g))
            return false;
        return g.getName().equals(this.getName()); // If they have the same name they are equal.
    }
}
