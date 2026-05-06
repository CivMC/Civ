package vg.civcraft.mc.namelayer.group;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import net.civmc.namelayer.sync.NameLayerWriteOperation;
import net.civmc.namelayer.sync.NameLayerWriteRequest;
import net.civmc.namelayer.sync.NameLayerWriteResponse;
import org.bukkit.Bukkit;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.rabbitmq.NameLayerWriteClient;

public class AutoAcceptHandler {

    private Set<UUID> autoAccepts;

    public AutoAcceptHandler(Set<UUID> autoAccepts) {
        this.autoAccepts = autoAccepts;
    }

    private void cacheAutoAccept(UUID player, boolean accept) {
        if (accept && !autoAccepts.contains(player)) {
            autoAccepts.add(player);
        } else {
            if (autoAccepts.contains(player)) {
                autoAccepts.remove(player);
            }
        }

    }

    public void setAutoAcceptAsync(final UUID player, final boolean accept, final Consumer<AutoAcceptWriteResult> callback) {
        final NameLayerWriteClient writeClient = NameLayerPlugin.getWriteClient();
        if (writeClient == null) {
            completeOnMain(callback, AutoAcceptWriteResult.failure("NameLayer proxy write client is unavailable"));
            return;
        }
        final NameLayerPlugin plugin = NameLayerPlugin.getInstance();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            final NameLayerWriteRequest request = NameLayerWriteRequest.create(
                plugin.getConfig().getString("rabbitmq.serverId", "paper"),
                player,
                NameLayerWriteOperation.SET_AUTO_ACCEPT,
                Map.of("value", Boolean.toString(accept))
            );
            writeClient.send(request).whenComplete((response, error) -> handleWriteResponse(player, accept, response, error, callback));
        });
    }

    public void toggleAutoAcceptAsync(final UUID player, final Consumer<AutoAcceptWriteResult> callback) {
        setAutoAcceptAsync(player, !getAutoAccept(player), callback);
    }

    public boolean getAutoAccept(UUID uuid) {
        return autoAccepts.contains(uuid);
    }

    public void reloadAll(final Set<UUID> autoAccepts) {
        this.autoAccepts = autoAccepts;
    }

    private void handleWriteResponse(
        final UUID player,
        final boolean accept,
        final NameLayerWriteResponse response,
        final Throwable error,
        final Consumer<AutoAcceptWriteResult> callback
    ) {
        if (error != null) {
            NameLayerPlugin.getInstance().getLogger().log(Level.WARNING, "NameLayer auto-accept proxy write failed", error);
            completeOnMain(callback, AutoAcceptWriteResult.failure("NameLayer proxy write failed"));
            return;
        }
        if (!response.success()) {
            completeOnMain(callback, AutoAcceptWriteResult.failure(response.message()));
            return;
        }
        cacheAutoAccept(player, accept);
        completeOnMain(callback, AutoAcceptWriteResult.successResult());
    }

    private void completeOnMain(final Consumer<AutoAcceptWriteResult> callback, final AutoAcceptWriteResult result) {
        Bukkit.getScheduler().runTask(NameLayerPlugin.getInstance(), () -> callback.accept(result));
    }

    public record AutoAcceptWriteResult(boolean success, String message) {

        public static AutoAcceptWriteResult successResult() {
            return new AutoAcceptWriteResult(true, "");
        }

        public static AutoAcceptWriteResult failure(final String message) {
            return new AutoAcceptWriteResult(false, message == null || message.isBlank() ? "Auto-accept write failed" : message);
        }
    }
}
