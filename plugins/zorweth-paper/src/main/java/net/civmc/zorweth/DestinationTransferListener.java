package net.civmc.zorweth;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockState;
import java.util.logging.Level;
import net.civmc.zorweth.transfer.DestinationRocketTransfer;
import net.civmc.zorweth.transfer.RocketBlockPosition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;

public final class DestinationTransferListener implements Listener {

    private final ZorwethPlugin plugin;

    public DestinationTransferListener(final ZorwethPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> handleJoin(player));
    }

    private void handleJoin(final Player player) {
        final DestinationRocketTransfer transfer;
        try {
            transfer = this.plugin.getRocketTransferDao().getPendingDestinationTransfer(
                player.getUniqueId(), this.plugin.getServerName());
        } catch (final Exception exception) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to look up destination rocket transfer", exception);
            kick(player);
            return;
        }
        if (transfer == null) {
            return;
        }

        final boolean claimed;
        try {
            claimed = this.plugin.getRocketTransferDao().claimDestinationTransfer(transfer.transferId(), player.getUniqueId());
        } catch (final Exception exception) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to claim destination rocket transfer", exception);
            kick(player);
            return;
        }
        if (!claimed) {
            kick(player);
            return;
        }

        Bukkit.getScheduler().runTask(this.plugin, () -> {
            try {
                if (!ensureDestinationRocketPasted(transfer)) {
                    kick(player);
                }
            } catch (final RuntimeException exception) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to paste destination rocket", exception);
                kick(player);
            }
        });
    }

    private boolean ensureDestinationRocketPasted(final DestinationRocketTransfer transfer) {
        final World world = Bukkit.getWorld(transfer.destinationWorld());
        if (world == null) {
            this.plugin.getLogger().warning("Destination world is not loaded: " + transfer.destinationWorld());
            return false;
        }

        final Block computer = getDestinationComputerBlock(world, transfer.destinationOrigin());
        final String existingTransferId = getSchematicPasteMarker(computer);
        if (transfer.transferId().toString().equals(existingTransferId)) {
            return true;
        }
        if (existingTransferId != null) {
            this.plugin.getLogger().warning("Destination rocket already has schematic marker for " + existingTransferId
                + " at " + computer.getLocation());
            return false;
        }

        pasteRocket(world, transfer.destinationOrigin());
        markDestinationComputer(getDestinationComputerBlock(world, transfer.destinationOrigin()), transfer);
        return true;
    }

    private String getSchematicPasteMarker(final Block computer) {
        if (computer.getType() != Material.DISPENSER) {
            return null;
        }
        final Dispenser dispenser = (Dispenser) computer.getState(false);
        return dispenser.getPersistentDataContainer().get(RocketTransferKeys.SCHEMATIC_PASTED_LOCAL,
            PersistentDataType.STRING);
    }

    private void pasteRocket(final World world, final RocketBlockPosition origin) {
        final Clipboard clipboard = this.plugin.getRocketClipboard();
        final Region region = clipboard.getRegion();
        final BlockVector3 schematicNorthWestCorner = region.getMinimumPoint();
        for (final BlockVector3 position : region) {
            final BlockVector3 relative = position.subtract(schematicNorthWestCorner);
            final BlockState block = clipboard.getBlock(position);
            final Block target = world.getBlockAt(
                origin.x() + relative.getX(),
                origin.y() + relative.getY(),
                origin.z() + relative.getZ()
            );
            target.setBlockData(Bukkit.createBlockData(block.getAsString()), false);
        }
    }

    private void markDestinationComputer(final Block computer, final DestinationRocketTransfer transfer) {
        if (computer.getType() != Material.DISPENSER) {
            throw new IllegalStateException("Destination schematic did not produce a flight computer at "
                + computer.getLocation());
        }
        final Dispenser dispenser = (Dispenser) computer.getState(false);
        dispenser.getPersistentDataContainer().set(FlightComputer.ROCKET_COMPUTER_KEY, PersistentDataType.BOOLEAN, true);
        dispenser.getPersistentDataContainer().set(RocketTransferKeys.DESTINATION_TRANSFER_ID,
            PersistentDataType.STRING, transfer.transferId().toString());
        dispenser.getPersistentDataContainer().set(RocketTransferKeys.SCHEMATIC_PASTED_LOCAL,
            PersistentDataType.STRING, transfer.transferId().toString());
        dispenser.update(true, false);
    }

    private Block getDestinationComputerBlock(final World world, final RocketBlockPosition origin) {
        return world.getBlockAt(
            origin.x() + FlightComputer.RELATIVE_POSITION.getX(),
            origin.y() + FlightComputer.RELATIVE_POSITION.getY(),
            origin.z() + FlightComputer.RELATIVE_POSITION.getZ()
        );
    }

    private void kick(final Player player) {
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            if (player.isOnline()) {
                player.kick(Component.text(this.plugin.getTransferFailureMessage(), NamedTextColor.RED));
            }
        });
    }
}
