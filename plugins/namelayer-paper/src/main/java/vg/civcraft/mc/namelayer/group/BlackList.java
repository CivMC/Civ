package vg.civcraft.mc.namelayer.group;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import net.civmc.namelayer.sync.NameLayerWriteOperation;
import net.civmc.namelayer.sync.NameLayerWriteRequest;
import net.civmc.namelayer.sync.NameLayerWriteResponse;
import org.bukkit.Bukkit;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.rabbitmq.NameLayerWriteClient;

public class BlackList {

    public BlackList() {
    }

    public Set<UUID> getBlacklist(Group g) {
        return g.getBlacklist();
    }

    public Set<UUID> getBlacklist(String groupName) {
        Group group = GroupManager.getGroup(groupName);
        return group == null ? new HashSet<UUID>() : group.getBlacklist();
    }

    public boolean isBlacklisted(Group group, UUID uuid) {
        return group.isBlacklisted(uuid);
    }

    public boolean isBlacklisted(String groupName, UUID uuid) {
        Group group = GroupManager.getGroup(groupName);
        return group != null && group.isBlacklisted(uuid);
    }

    public void initEmptyBlackList(String groupName) {
    }

    public void addBlacklistMemberAsync(
        final UUID actorUuid,
        final Group group,
        final UUID uuid,
        final boolean adminOverride,
        final Consumer<BlacklistWriteResult> callback
    ) {
        sendBlacklistWrite(actorUuid, NameLayerWriteOperation.ADD_BLACKLIST, group, uuid, adminOverride, callback);
    }

    public void removeBlacklistMemberAsync(
        final UUID actorUuid,
        final Group group,
        final UUID uuid,
        final boolean adminOverride,
        final Consumer<BlacklistWriteResult> callback
    ) {
        sendBlacklistWrite(actorUuid, NameLayerWriteOperation.REMOVE_BLACKLIST, group, uuid, adminOverride, callback);
    }

    private void sendBlacklistWrite(
        final UUID actorUuid,
        final NameLayerWriteOperation operation,
        final Group group,
        final UUID uuid,
        final boolean adminOverride,
        final Consumer<BlacklistWriteResult> callback
    ) {
        if (group == null || uuid == null) {
            completeOnMain(callback, BlacklistWriteResult.failure("Group and player are required"));
            return;
        }
        final NameLayerWriteClient writeClient = NameLayerPlugin.getWriteClient();
        if (writeClient == null) {
            completeOnMain(callback, BlacklistWriteResult.failure("NameLayer proxy write client is unavailable"));
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
                    "memberUuid", uuid.toString(),
                    "adminOverride", Boolean.toString(adminOverride)
                )
            );
            writeClient.send(request).whenComplete((response, error) -> handleWriteResponse(group, response, error, callback));
        });
    }

    private void handleWriteResponse(
        final Group group,
        final NameLayerWriteResponse response,
        final Throwable error,
        final Consumer<BlacklistWriteResult> callback
    ) {
        if (error != null) {
            NameLayerPlugin.getInstance().getLogger().log(Level.WARNING, "NameLayer blacklist proxy write failed", error);
            completeOnMain(callback, BlacklistWriteResult.failure("NameLayer proxy write failed"));
            return;
        }
        if (!response.success()) {
            completeOnMain(callback, BlacklistWriteResult.failure(response.message()));
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
            reloadSucceeded = GroupManager.reloadGroupsById(java.util.List.copyOf(affectedGroupIds));
        }
        if (!reloadSucceeded) {
            completeOnMain(callback, BlacklistWriteResult.failure("Blacklist write succeeded, but local cache refresh failed"));
            return;
        }
        completeOnMain(callback, BlacklistWriteResult.successResult());
    }

    private void completeOnMain(final Consumer<BlacklistWriteResult> callback, final BlacklistWriteResult result) {
        Bukkit.getScheduler().runTask(NameLayerPlugin.getInstance(), () -> callback.accept(result));
    }

    public record BlacklistWriteResult(boolean success, String message) {

        public static BlacklistWriteResult successResult() {
            return new BlacklistWriteResult(true, "");
        }

        public static BlacklistWriteResult failure(final String message) {
            return new BlacklistWriteResult(false, message == null || message.isBlank() ? "Blacklist write failed" : message);
        }
    }
}
