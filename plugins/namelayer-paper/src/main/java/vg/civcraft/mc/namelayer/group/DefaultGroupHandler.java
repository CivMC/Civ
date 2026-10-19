package vg.civcraft.mc.namelayer.group;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import net.civmc.namelayer.sync.NameLayerWriteOperation;
import net.civmc.namelayer.sync.NameLayerWriteRequest;
import net.civmc.namelayer.sync.NameLayerWriteResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.database.NameLayerReadDao;
import vg.civcraft.mc.namelayer.rabbitmq.NameLayerWriteClient;

public class DefaultGroupHandler {

    private final NameLayerReadDao dao;

    private Map<UUID, String> defaultGroups;

    public DefaultGroupHandler() {
        dao = NameLayerPlugin.getNameLayerReadDao();
        defaultGroups = dao.getAllDefaultGroups();
    }

    public String getDefaultGroup(Player p) {
        return getDefaultGroup(p.getUniqueId());
    }

    public String getDefaultGroup(UUID uuid) {
        return defaultGroups.get(uuid);
    }

    public void reloadAll() {
        defaultGroups = dao.getAllDefaultGroups();
    }

    /**
     * Applies an authoritative assignment received via RabbitMQ invalidation. Does not hit the database.
     */
    public void applyAssignment(final UUID uuid, final String groupName) {
        if (uuid == null || groupName == null) {
            return;
        }
        defaultGroups.put(uuid, groupName);
    }

    /**
     * Applies an authoritative clear received via RabbitMQ invalidation. Does not hit the database.
     */
    public void applyClear(final UUID uuid) {
        if (uuid == null) {
            return;
        }
        defaultGroups.remove(uuid);
    }

    public void cacheDefaultGroup(UUID uuid, Group g) {
        defaultGroups.put(uuid, g.getName());
    }

    public void setDefaultGroupAsync(
        final UUID uuid,
        final Group group,
        final Consumer<DefaultGroupWriteResult> callback
    ) {
        if (uuid == null || group == null) {
            completeOnMain(callback, DefaultGroupWriteResult.failure("Player and group are required"));
            return;
        }
        final NameLayerWriteClient writeClient = NameLayerPlugin.getWriteClient();
        if (writeClient == null) {
            completeOnMain(callback, DefaultGroupWriteResult.failure("NameLayer proxy write client is unavailable"));
            return;
        }
        final NameLayerPlugin plugin = NameLayerPlugin.getInstance();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            final NameLayerWriteRequest request = NameLayerWriteRequest.create(
                plugin.getConfig().getString("rabbitmq.serverId", "paper"),
                uuid,
                NameLayerWriteOperation.SET_DEFAULT_GROUP,
                Map.of("groupId", Integer.toString(group.getGroupId()))
            );
            writeClient.send(request).whenComplete((response, error) -> handleWriteResponse(uuid, group, response, error, callback));
        });
    }

    private void handleWriteResponse(
        final UUID uuid,
        final Group group,
        final NameLayerWriteResponse response,
        final Throwable error,
        final Consumer<DefaultGroupWriteResult> callback
    ) {
        if (error != null) {
            NameLayerPlugin.getInstance().getLogger().log(Level.WARNING, "NameLayer default group proxy write failed", error);
            completeOnMain(callback, DefaultGroupWriteResult.failure("NameLayer proxy write failed"));
            return;
        }
        if (!response.success()) {
            completeOnMain(callback, DefaultGroupWriteResult.failure(response.message()));
            return;
        }
        cacheDefaultGroup(uuid, group);
        completeOnMain(callback, DefaultGroupWriteResult.successResult());
    }

    private void completeOnMain(final Consumer<DefaultGroupWriteResult> callback, final DefaultGroupWriteResult result) {
        Bukkit.getScheduler().runTask(NameLayerPlugin.getInstance(), () -> callback.accept(result));
    }

    public record DefaultGroupWriteResult(boolean success, String message) {

        public static DefaultGroupWriteResult successResult() {
            return new DefaultGroupWriteResult(true, "");
        }

        public static DefaultGroupWriteResult failure(final String message) {
            return new DefaultGroupWriteResult(false, message == null || message.isBlank() ? "Default group write failed" : message);
        }
    }

}
