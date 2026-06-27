package net.civmc.zorweth;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.sql.SQLException;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import net.civmc.zorweth.database.RocketTransferDao.CrossServerOttArrival;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.persistence.PersistentDataType;

public final class CrossServerOttManager {

    private static final long TRANSFER_TIMEOUT_TICKS = 150L;

    private final ZorwethPlugin plugin;

    public CrossServerOttManager(final ZorwethPlugin plugin) {
        this.plugin = plugin;
    }

    public String getServerName() {
        return this.plugin.getServerName();
    }

    public String getDestinationServer() {
        return this.plugin.getDestinationServer();
    }

    public void prepareArrivalAsync(final UUID requesterId, final Player target, final long expiresAtMillis,
                                    final Runnable onSuccess, final Consumer<SQLException> onFailure) {
        final UUID targetId = target.getUniqueId();
        final Location targetLocation = target.getLocation();
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try {
                this.plugin.getRocketTransferDao().setCrossServerOttArrival(
                    requesterId, targetId, this.plugin.getServerName(), targetLocation, expiresAtMillis);
            } catch (final SQLException exception) {
                Bukkit.getScheduler().runTask(this.plugin, () -> onFailure.accept(exception));
                return;
            }
            Bukkit.getScheduler().runTask(this.plugin, onSuccess);
        });
    }

    public CrossServerOttArrival getArrival(final UUID requesterId) throws SQLException {
        return this.plugin.getRocketTransferDao().getCrossServerOttArrival(requesterId, this.plugin.getServerName());
    }

    private void clearArrival(final UUID requesterId) throws SQLException {
        this.plugin.getRocketTransferDao().clearCrossServerOttArrival(requesterId);
    }

    public void clearArrivalAsync(final UUID requesterId) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try {
                clearArrival(requesterId);
            } catch (final SQLException exception) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to clear cross-server OTT arrival", exception);
            }
        });
    }

    public void transfer(final Player player, final String destinationServer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try {
                this.plugin.getRocketTransferDao().setPlayerRoute(player.getUniqueId(), destinationServer);
                this.plugin.getLogger().log(Level.SEVERE, player.getUniqueId() + " has been routed for OTT");
            } catch (final SQLException exception) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to set OTT route override", exception);
                return;
            }

            Bukkit.getScheduler().runTask(this.plugin, () -> completeTransfer(player, destinationServer));
        });
    }

    private void completeTransfer(final Player player, final String destinationServer) {
        player.getInventory().clear();
        if (player.getOpenInventory().getTopInventory() instanceof CraftingInventory inventory) {
            inventory.clear();
        }
        player.setLevel(0);
        player.setExp(0.0f);
        player.setFoodLevel(20);
        player.setSaturation(5.0f);
        player.setExhaustion(0.0f);
        player.getInventory().setHeldItemSlot(0);
        player.getPersistentDataContainer().set(RocketTransferKeys.PIONEER, PersistentDataType.BOOLEAN, true);
        this.plugin.getStasisHandler().putInStasis(player);
        connect(player, destinationServer);
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            final Player toKick = Bukkit.getPlayer(player.getUniqueId());
            if (toKick != null) {
                toKick.kick(Component.text(this.plugin.getTransferFailureMessage(), NamedTextColor.RED));
            }
        }, TRANSFER_TIMEOUT_TICKS);
    }

    private void connect(final Player player, final String server) {
        final ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("Connect");
        output.writeUTF(server);
        player.sendPluginMessage(this.plugin, "BungeeCord", output.toByteArray());
    }
}
