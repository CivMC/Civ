package vg.civcraft.mc.namelayer.permission;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import net.civmc.namelayer.sync.NameLayerWriteOperation;
import net.civmc.namelayer.sync.NameLayerWriteRequest;
import net.civmc.namelayer.sync.NameLayerWriteResponse;
import org.bukkit.Bukkit;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.database.GroupManagerDao;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.rabbitmq.NameLayerWriteClient;

public class GroupPermission {

    private Map<PlayerType, List<PermissionType>> perms;
    private GroupManagerDao db = NameLayerPlugin.getGroupManagerDao();

    private Group group;

    public GroupPermission(Group group) {
        this.group = group;
        loadPermsforGroup();
    }

    private void loadPermsforGroup() {
        perms = db.getPermissions(group.getName());
        //to save ourselves from trouble later, we ensure that every perm type has at least an empty list
        for (PlayerType pType : PlayerType.values()) {
            perms.computeIfAbsent(pType, k -> new ArrayList<>());
        }
    }

    /**
     * Checks if a certain PlayerType has the given permission. DONT USE THIS DIRECTLY. Use GroupManager.hasAccess() instead!
     *
     * @param playerType The PlayerType in question.
     * @param perm       The PermissionType to check for.
     * @return return true if this type of player has this type of perm, false otherwise
     */
    public boolean hasPermission(PlayerType playerType, PermissionType perm) {
        if (playerType == null || perm == null) {
            return false;
        }
        List<PermissionType> per = perms.get(playerType);
        if (per == null || per.isEmpty()) {
            return false;
        }
        if (per.contains(perm)) {
            return true;
        }
        return false;
    }

    /**
     * Lists the permissions types for a given PlayerType for the specific GroupPermission.
     *
     * @param type The PlayerType to check for.
     * @return Returns a String representation of the permissions. Should be sent to the player in this form.
     */
    public String listPermsforPlayerType(PlayerType type) {
        String x = "The permission types are: ";
        for (PermissionType pType : perms.get(type)) {
            if (pType != null) {
                x += pType.getName() + " ";
            }
        }
        return x;
    }

    /**
     * Adds a PermissionType to a PlayerType.
     *
     * @param pType    The PlayerType.
     * @param permType The PermissionType.
     * @return Returns false if the PlayerType already has the permission.
     */
    public void addPermission(
        final UUID actorUuid,
        final PlayerType pType,
        final PermissionType permType,
        final Consumer<PermissionWriteResult> callback
    ) {
        List<PermissionType> playerPerms = perms.get(pType);
        if (playerPerms == null || playerPerms.contains(permType)) {
            completeOnMain(callback, PermissionWriteResult.failure("This PlayerType already has the PermissionType: " + permType.getName()));
            return;
        }
        sendPermissionWrite(actorUuid, NameLayerWriteOperation.ADD_PERMISSION, pType, permType, callback);
    }

    /**
     * Removes the PermissionType from a PlayerType.
     *
     * @param pType    The PlayerType to get the PermissionType removed from.
     * @param permType The PermissionType.
     * @return Returns false if the PlayerType doesn't have that permission.
     */
    public void removePermission(
        final UUID actorUuid,
        final PlayerType pType,
        final PermissionType permType,
        final Consumer<PermissionWriteResult> callback
    ) {
        List<PermissionType> playerPerms = perms.get(pType);
        if (playerPerms == null || !playerPerms.contains(permType)) {
            completeOnMain(callback, PermissionWriteResult.failure("This PlayerType does not have the PermissionType: " + permType.getName()));
            return;
        }
        sendPermissionWrite(actorUuid, NameLayerWriteOperation.REMOVE_PERMISSION, pType, permType, callback);
    }

    private void sendPermissionWrite(
        final UUID actorUuid,
        final NameLayerWriteOperation operation,
        final PlayerType pType,
        final PermissionType permType,
        final Consumer<PermissionWriteResult> callback
    ) {
        final NameLayerWriteClient writeClient = NameLayerPlugin.getWriteClient();
        if (writeClient == null) {
            completeOnMain(callback, PermissionWriteResult.failure("NameLayer proxy write client is unavailable"));
            return;
        }
        final NameLayerPlugin plugin = NameLayerPlugin.getInstance();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            final NameLayerWriteRequest request = NameLayerWriteRequest.create(
                plugin.getConfig().getString("rabbitmq.serverId", "paper"),
                actorUuid,
                operation,
                Map.of(
                    "groupId", Integer.toString(group.getGroupId()),
                    "role", pType.name(),
                    "permissionId", Integer.toString(permType.getId())
                )
            );
            writeClient.send(request).whenComplete((response, error) -> handleWriteResponse(response, error, callback));
        });
    }

    private void handleWriteResponse(
        final NameLayerWriteResponse response,
        final Throwable error,
        final Consumer<PermissionWriteResult> callback
    ) {
        if (error != null) {
            NameLayerPlugin.getInstance().getLogger().log(Level.WARNING, "NameLayer permission proxy write failed", error);
            completeOnMain(callback, PermissionWriteResult.failure("NameLayer proxy write failed"));
            return;
        }
        if (!response.success()) {
            completeOnMain(callback, PermissionWriteResult.failure(response.message()));
            return;
        }
        final boolean reloadSucceeded;
        if (response.requiresFullResync()) {
            NameLayerPlugin.fullResyncGroupCache();
            reloadSucceeded = true;
        } else {
            final Set<Integer> affectedGroupIds = response.affectedGroupIds().isEmpty()
                ? Set.of(group.getGroupId())
                : response.affectedGroupIds();
            reloadSucceeded = vg.civcraft.mc.namelayer.GroupManager.reloadGroupsById(List.copyOf(affectedGroupIds));
        }
        if (!reloadSucceeded) {
            completeOnMain(callback, PermissionWriteResult.failure("Permission write succeeded, but local cache refresh failed"));
            return;
        }
        completeOnMain(callback, PermissionWriteResult.successResult());
    }

    private void completeOnMain(final Consumer<PermissionWriteResult> callback, final PermissionWriteResult result) {
        Bukkit.getScheduler().runTask(NameLayerPlugin.getInstance(), () -> callback.accept(result));
    }

    public record PermissionWriteResult(boolean success, String message) {

        public static PermissionWriteResult successResult() {
            return new PermissionWriteResult(true, "");
        }

        public static PermissionWriteResult failure(final String message) {
            return new PermissionWriteResult(false, message == null || message.isBlank() ? "Permission write failed" : message);
        }
    }

    /**
     * Returns the first PlayerType with a specific permission.
     *
     * @param type The PermissionType you are looking for.
     * @return Returns the first PlayerType with the permission or false if none was found.
     */
    public PlayerType getFirstWithPerm(PermissionType type) {
        for (PlayerType pType : perms.keySet()) {
            if (perms.get(pType).contains(type))
                return pType;
        }
        return null;
    }
}
