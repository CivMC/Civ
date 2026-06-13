package net.civmc.zorweth;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockState;
import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import isaac.bastion.Bastion;
import net.civmc.zorweth.flight.FlightComputer;
import net.civmc.zorweth.transfer.DestinationRocketTransfer;
import net.civmc.zorweth.transfer.RocketBlockPosition;
import net.civmc.zorweth.transfer.RocketChestTransfer;
import net.civmc.zorweth.transfer.RocketEntityPosition;
import net.civmc.zorweth.transfer.RocketPassengerTransfer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import vg.civcraft.mc.citadel.CitadelPermissionHandler;

public final class DestinationTransferListener implements Listener {

    public static final int SPIRAL_ITERATION_DISTANCE = 8;
    private static final int DESTINATION_CENTER_X = 0;
    private static final int DESTINATION_CENTER_Z = -20_000;
    private final ZorwethPlugin plugin;
    private final Map<UUID, CompletableFuture<DestinationRocketTransfer>> futures = new ConcurrentHashMap<>();

    private final Map<UUID, DestinationRocketTransfer> cachedRockets = new ConcurrentHashMap<>();
    private final Map<UUID, RocketPassengerTransfer> cachedPlayers = new ConcurrentHashMap<>();

    public DestinationTransferListener(final ZorwethPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerPreJoin(final AsyncPlayerPreLoginEvent event) {
        final DestinationRocketTransfer transfer;
        UUID playerId = event.getUniqueId();

        try {
            transfer = this.plugin.getRocketTransferDao().getPendingDestinationTransfer(
                playerId, this.plugin.getServerName());
        } catch (final Exception exception) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to look up destination rocket transfer", exception);
            event.kickMessage(Component.text("Unable process logins at this time, please try again later"));
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            return;
        }

        if (transfer == null) {
            return;
        }

        CompletableFuture<DestinationRocketTransfer> success = new CompletableFuture<>();

        RocketBlockPosition rocketBlockPosition = transfer.destinationOrigin();
        if (rocketBlockPosition == null) {
            CompletableFuture<DestinationRocketTransfer> existing = futures.putIfAbsent(transfer.transferId(), success);
            if (existing != null) {
                success.complete(existing.join());
            } else {
                Bukkit.getScheduler().runTask(plugin, () -> updateRocketPosition(transfer, success));
            }
        } else {
            success.complete(transfer);
        }

        DestinationRocketTransfer pos = success.join();
        if (pos == null) {
            event.kickMessage(Component.text("Unable process rocket location at this time, please try again later"));
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            return;
        }

        try {
            cachedPlayers.put(playerId, this.plugin.getRocketTransferDao().getPlayer(transfer.transferId(), playerId));
        } catch (SQLException e) {
            event.kickMessage(Component.text("Unable process passenger at this time, please try again later"));
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            return;
        }
        cachedRockets.put(playerId, pos);
    }

    @EventHandler
    public void on(AsyncPlayerSpawnLocationEvent event) {
        UUID playerId = event.getConnection().getProfile().getId();
        DestinationRocketTransfer transfer = cachedRockets.get(playerId);
        RocketPassengerTransfer passenger = cachedPlayers.get(playerId);
        if (transfer == null || passenger == null) {
            return;
        }

        final World world = Bukkit.getWorld(transfer.destinationWorld());
        RocketBlockPosition pos = transfer.destinationOrigin();

        RocketEntityPosition passengerPos = passenger.relativePosition();
        event.setSpawnLocation(new Location(world,
            pos.x() + passengerPos.x(),
            pos.y() + passengerPos.y() + 1.2, // teleport above seat
            pos.z() + passengerPos.z(),
            passengerPos.yaw(),
            passengerPos.pitch()
        ));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void on(PlayerJoinEvent event) {
        DestinationRocketTransfer transfer = cachedRockets.remove(event.getPlayer().getUniqueId());
        RocketPassengerTransfer passenger = cachedPlayers.remove(event.getPlayer().getUniqueId());

        final Player player = event.getPlayer();
        if (transfer == null || passenger == null) {
            return;
        }

        player.getPersistentDataContainer().set(RocketTransferKeys.NO_OTT, PersistentDataType.BOOLEAN, true);
        player.getPersistentDataContainer().set(RocketTransferKeys.NO_STARTER_KIT, PersistentDataType.BOOLEAN, true);

        if (hasAppliedTransferMarker(player, transfer.transferId())) {
            markPassengerApplied(player.getUniqueId(), transfer.transferId());
            return;
        }

        player.getInventory().setContents(ItemStack.deserializeItemsFromBytes(passenger.serializedInventory()));
        player.setHealth(Math.min(passenger.health(), player.getAttribute(Attribute.MAX_HEALTH).getValue()));
        player.setLevel(passenger.xpLevel());
        player.setExp(passenger.xpProgress());
        player.setFoodLevel(passenger.foodLevel());
        player.setSaturation(passenger.saturation());
        player.setExhaustion(passenger.exhaustion());
        player.getInventory().setHeldItemSlot(passenger.heldSlot());
        player.setGameMode(passenger.gameMode());
        player.getPersistentDataContainer().set(RocketTransferKeys.DESTINATION_APPLIED_TRANSFER_ID,
            PersistentDataType.STRING, transfer.transferId().toString());
        markPassengerApplied(player.getUniqueId(), transfer.transferId());
    }

    private boolean hasAppliedTransferMarker(final Player player, final UUID transferId) {
        return transferId.toString().equals(player.getPersistentDataContainer()
            .get(RocketTransferKeys.DESTINATION_APPLIED_TRANSFER_ID, PersistentDataType.STRING));
    }

    private void markPassengerApplied(final UUID playerId, final UUID transferId) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try {
                if (!this.plugin.getRocketTransferDao().markPassengerApplied(transferId, playerId)) {
                    this.plugin.getLogger().warning("Unable to mark rocket passenger applied for "
                        + playerId + " in transfer " + transferId);
                    return;
                }
            } catch (final SQLException exception) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to mark rocket passenger applied", exception);
                return;
            }

            Bukkit.getScheduler().runTask(this.plugin, () -> {
                final Player onlinePlayer = Bukkit.getPlayer(playerId);
                if (onlinePlayer != null && hasAppliedTransferMarker(onlinePlayer, transferId)) {
                    onlinePlayer.getPersistentDataContainer().remove(RocketTransferKeys.DESTINATION_APPLIED_TRANSFER_ID);
                }
            });
        });
    }

    private void updateRocketPosition(final DestinationRocketTransfer transfer, CompletableFuture<DestinationRocketTransfer> success) {
        RocketBlockPosition position;
        try {
            position = findRocketPosition(transfer);
        } catch (RuntimeException ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to find rocket position", ex);
            success.complete(null);
            return;
        }
        if (position == null) {
            success.complete(null);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                List<RocketChestTransfer> chests = this.plugin.getRocketTransferDao().getChests(transfer.transferId());
                if (this.plugin.getRocketTransferDao().setConfirmedDestinationOrigin(transfer.transferId(), position)) {
                    DestinationRocketTransfer updated = transfer.withPosition(position);
                    success.complete(updated);

                    Bukkit.getScheduler().runTask(plugin, () -> ensureDestinationRocketPasted(updated, chests));
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to update destination rocket position", e);
            } finally {
                success.complete(null);
            }
        });
    }

    private RocketBlockPosition findRocketPosition(final DestinationRocketTransfer transfer) {
        int rx = transfer.requestedX() - FlightComputer.RELATIVE_POSITION.getX();
        int rz = transfer.requestedZ() - FlightComputer.RELATIVE_POSITION.getZ();

        final double distanceSquared = getDistanceSquaredFromCenter(rx, rz);
        final double radiusSquared = plugin.getWorldRadius() * plugin.getWorldRadius();
        if (distanceSquared > radiusSquared) {
            final double distance = Math.sqrt(distanceSquared);
            final double scale = plugin.getWorldRadius() / distance;
            rx = DESTINATION_CENTER_X + (int) ((rx - DESTINATION_CENTER_X) * scale);
            rz = DESTINATION_CENTER_Z + (int) ((rz - DESTINATION_CENTER_Z) * scale);
        }

        final World world = Bukkit.getWorld(transfer.destinationWorld());
        if (world == null) {
            return null;
        }
        final Clipboard clipboard = this.plugin.getRocketClipboard();
        final Region region = clipboard.getRegion();
        final BlockVector3 schematicNorthWestCorner = region.getMinimumPoint();

        int distance = 0;
        OUTER:
        while (distance < 1000) {
            final double angle = ThreadLocalRandom.current().nextDouble(0.0, 2.0 * Math.PI);
            final int tx = rx + (int) (distance * Math.cos(angle));
            final int tz = rz + (int) (distance * Math.sin(angle));

            if (getDistanceSquaredFromCenter(tx, tz) > radiusSquared) {
                distance += SPIRAL_ITERATION_DISTANCE;
                continue;
            }

            int highestY = world.getMinHeight();
            for (final BlockVector3 position : region) {
                if (position.getY() != schematicNorthWestCorner.getY()) {
                    continue;
                }
                final BlockVector3 relative = position.subtract(schematicNorthWestCorner);
                final int blockY = world.getHighestBlockYAt(tx + relative.getX(), tz + relative.getZ());
                highestY = Math.max(highestY, blockY);

                final Location location = new Location(world, tx + relative.getX(), blockY, tz + relative.getZ());
                if (!Bastion.getBastionManager().getBlockingBastionsWithoutPermission(location,
                    transfer.pilotUuid(), CitadelPermissionHandler.getReinforce()).isEmpty()) {
                    distance += SPIRAL_ITERATION_DISTANCE;
                    continue OUTER;
                }

                if (highestY + 1 + region.getHeight() >= world.getMaxHeight()) {
                    distance += SPIRAL_ITERATION_DISTANCE;
                    continue OUTER;
                }
            }

            final RocketBlockPosition origin = new RocketBlockPosition(tx, highestY + 1, tz);
            boolean empty = true;
            for (final BlockVector3 position : region) {
                final BlockVector3 relative = position.subtract(schematicNorthWestCorner);
                final BlockState block = clipboard.getBlock(position);
                if (Bukkit.createBlockData(block.getAsString()).getMaterial().isAir()) {
                    continue;
                }
                final Block target = world.getBlockAt(
                    origin.x() + relative.getX(),
                    origin.y() + relative.getY(),
                    origin.z() + relative.getZ()
                );
                if (!target.getType().isAir()) {
                    empty = false;
                    break;
                }
            }

            if (empty) {
                return origin;
            }

            distance += SPIRAL_ITERATION_DISTANCE;
        }

        plugin.getLogger().warning("Unable to find position for rocket " + transfer.transferId());
        return null;
    }

    private double getDistanceSquaredFromCenter(final int x, final int z) {
        final double relativeX = x - DESTINATION_CENTER_X;
        final double relativeZ = z - DESTINATION_CENTER_Z;
        return relativeX * relativeX + relativeZ * relativeZ;
    }

    private boolean ensureDestinationRocketPasted(final DestinationRocketTransfer transfer, List<RocketChestTransfer> chests) {
        final World world = Bukkit.getWorld(transfer.destinationWorld());
        if (world == null) {
            this.plugin.getLogger().warning("Destination world is not loaded: " + transfer.destinationWorld());
            return false;
        }

        pasteRocket(world, transfer.destinationOrigin());
        pasteChests(world, transfer.destinationOrigin(), chests);
        markDestinationComputer(getDestinationComputerBlock(world, transfer.destinationOrigin()), transfer);
        return true;
    }

    private void pasteChests(World world, RocketBlockPosition origin, List<RocketChestTransfer> chests) {
        for (RocketChestTransfer chest : chests) {
            RocketBlockPosition chestPos = chest.relativePosition();
            Block block = world.getBlockAt(
                origin.x() + chestPos.x(),
                origin.y() + chestPos.y(),
                origin.z() + chestPos.z());

            ItemStack[] contents = ItemStack.deserializeItemsFromBytes(chest.serializedInventory());

            if (!(block.getState(false) instanceof Chest chestState)) {
                this.plugin.getLogger().warning("Expected chest at " + block);
                continue;
            }

            chestState.getBlockInventory().setContents(contents);
        }
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
        dispenser.getPersistentDataContainer().set(FlightComputer.ROCKET_FUEL_KEY, PersistentDataType.DOUBLE, transfer.fuelKg());
        dispenser.getPersistentDataContainer().set(FlightComputer.ROCKET_USES_REMAINING_KEY,
            PersistentDataType.INTEGER, transfer.usesRemaining());
        if (transfer.flightComputerGroupId() != null) {
            FlightComputer.reinforceFlightComputer(computer, transfer.flightComputerGroupId());
        }
    }

    private Block getDestinationComputerBlock(final World world, final RocketBlockPosition origin) {
        return world.getBlockAt(
            origin.x() + FlightComputer.RELATIVE_POSITION.getX(),
            origin.y() + FlightComputer.RELATIVE_POSITION.getY(),
            origin.z() + FlightComputer.RELATIVE_POSITION.getZ()
        );
    }
}
