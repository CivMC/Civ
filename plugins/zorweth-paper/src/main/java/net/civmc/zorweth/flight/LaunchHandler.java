package net.civmc.zorweth.flight;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import net.civmc.zorweth.RocketTransferKeys;
import net.civmc.zorweth.ZorwethPlugin;
import net.civmc.zorweth.transfer.RocketBlockPosition;
import net.civmc.zorweth.transfer.RocketChestTransfer;
import net.civmc.zorweth.transfer.RocketEntityPosition;
import net.civmc.zorweth.transfer.RocketManifest;
import net.civmc.zorweth.transfer.RocketManifestChest;
import net.civmc.zorweth.transfer.RocketManifestFileWriter;
import net.civmc.zorweth.transfer.RocketManifestPassenger;
import net.civmc.zorweth.transfer.RocketManifestSerializer;
import net.civmc.zorweth.transfer.RocketPassengerTransfer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.model.Reinforcement;

// Handles launching logic
public class LaunchHandler {

    public static final double FUEL_ITEM_MASS_KG = 4.0;
    public static final double EXHAUST_VELOCITY_METERS_PER_SECOND = 5_000.0;
    public static final double ROCKET_DRY_MASS_KG = 100.0;
    public static final double SITTING_PLAYER_MASS_KG = 50.0;

    public static FuelStatus calculateFuelStatus(final Block computer, final List<RocketManifestPassenger> passengers,
                                                  final List<RocketManifestChest> chests) {
        final double cargoMass = calculateCargoMass(chests);
        final int sittingPlayers = Math.max(1, passengers.size());
        final double nonFuelMass = ROCKET_DRY_MASS_KG + cargoMass + sittingPlayers * SITTING_PLAYER_MASS_KG;
        final double requiredFuelKg = nonFuelMass * (Math.exp(getDeltaVMetersPerSecond() / EXHAUST_VELOCITY_METERS_PER_SECOND) - 1.0);
        final double fuelKg = FlightComputer.getFuelKg(computer);
        return new FuelStatus(
            (int) (fuelKg / FUEL_ITEM_MASS_KG),
            fuelKg,
            requiredFuelKg,
            (int) Math.ceil(requiredFuelKg / FUEL_ITEM_MASS_KG),
            cargoMass,
            sittingPlayers
        );
    }

    public static RocketWeightPayload collectRocketWeightPayload(final Block computer, final Clipboard rocket) {
        final Region region = rocket.getRegion();
        final BlockVector3 schematicNorthWestCorner = region.getMinimumPoint();
        final Block origin = FlightComputer.getRocketOrigin(computer);
        final List<RocketManifestChest> chests = new ArrayList<>();

        for (final BlockVector3 position : region) {
            final BlockVector3 relative = position.subtract(schematicNorthWestCorner);
            final Block actualBlock = origin.getRelative(relative.getX(), relative.getY(), relative.getZ());
            if (actualBlock.getState(false) instanceof Chest chest) {
                chests.add(new RocketManifestChest(
                    new RocketBlockPosition(relative.getX(), relative.getY(), relative.getZ()),
                    chest.getBlockInventory().getStorageContents()
                ));
            }
        }

        final List<RocketManifestPassenger> passengers = new ArrayList<>();
        for (final Player seated : Bukkit.getOnlinePlayers()) {
            if (!seated.getWorld().equals(origin.getWorld())) {
                continue;
            }
            final Location location = seated.getLocation();
            final BlockVector3 relative = BlockVector3.at(
                location.getBlockX() - origin.getX(),
                location.getBlockY() - origin.getY(),
                location.getBlockZ() - origin.getZ()
            );
            if (!region.contains(schematicNorthWestCorner.add(relative)) || !FlightComputer.isSittingWithGSit(seated)) {
                continue;
            }
            passengers.add(new RocketManifestPassenger(
                seated.getUniqueId(),
                new RocketEntityPosition(
                    location.getX() - origin.getX(),
                    location.getY() - origin.getY(),
                    location.getZ() - origin.getZ(),
                    location.getYaw(),
                    location.getPitch()
                ),
                seated.getInventory().getContents(),
                seated.getHealth(),
                seated.getLevel(),
                seated.getExp(),
                seated.getFoodLevel(),
                seated.getSaturation(),
                seated.getExhaustion(),
                seated.getInventory().getHeldItemSlot(),
                seated.getGameMode()
            ));
        }

        return new RocketWeightPayload(passengers, chests, getRemainingFuel(chests, passengers, computer));
    }

    private static double calculateCargoMass(final List<RocketManifestChest> chests) {
        double mass = 0.0;
        for (final RocketManifestChest chest : chests) {
            for (final ItemStack item : chest.contents()) {
                if (item == null || item.getType().isAir()) {
                    continue;
                }
                double itemMass = item.getAmount() / (double) item.getMaxStackSize();
                if (FlightComputer.isFuel(item)) {
                    itemMass *= FUEL_ITEM_MASS_KG * item.getMaxStackSize();
                }
                mass += itemMass;
            }
        }
        return mass;
    }

    public static RocketManifestResult collectLaunchManifest(final ZorwethPlugin plugin, final Block computer, final Player player, Clipboard rocket) {
        final Region region = rocket.getRegion();
        final BlockVector3 schematicNorthWestCorner = region.getMinimumPoint();
        final Block origin = FlightComputer.getRocketOrigin(computer);
        final RocketWeightPayload payload = collectRocketWeightPayload(computer, rocket);

        for (final BlockVector3 position : region) {
            final BlockVector3 relative = position.subtract(schematicNorthWestCorner);
            final com.sk89q.worldedit.world.block.BlockState expectedState = rocket.getBlock(position);
            final Material expected = Bukkit.createBlockData(expectedState.getAsString()).getMaterial();
            final Block actualBlock = origin.getRelative(relative.getX(), relative.getY(), relative.getZ());
            final Material actual = actualBlock.getType();
            if (actual != expected) {
                return new RocketManifestResult(null,
                    Component.text("Rocket is not structurally intact", NamedTextColor.RED));
            }
        }

        for (final Player seated : Bukkit.getOnlinePlayers()) {
            if (!seated.getWorld().equals(origin.getWorld())) {
                continue;
            }
            final Location location = seated.getLocation();
            final BlockVector3 relative = BlockVector3.at(
                location.getBlockX() - origin.getX(),
                location.getBlockY() - origin.getY(),
                location.getBlockZ() - origin.getZ()
            );
            final boolean insideRocket = region.contains(schematicNorthWestCorner.add(relative));
            if (insideRocket && !FlightComputer.isSittingWithGSit(seated)) {
                return new RocketManifestResult(null,
                    Component.text((seated.equals(player) ? "Pilot" : "Passenger") + " " + seated.getName()
                        + " is not seated.", NamedTextColor.RED));
            }
        }

        final FlightComputerGui.Coordinates destination = FlightComputer.getDestination(computer);
        if (destination == null) {
            return new RocketManifestResult(null,
                Component.text("Destination not set.", NamedTextColor.RED));
        }

        return new RocketManifestResult(new RocketManifest(
            UUID.randomUUID(),
            plugin.getServerName(),
            plugin.getDestinationServer(),
            origin.getWorld().getName(),
            plugin.getDestinationWorld(),
            new RocketBlockPosition(origin.getX(), origin.getY(), origin.getZ()),
            destination.x(),
            destination.z(),
            player.getUniqueId(),
            getDiamondFlightComputerGroupId(computer),
            payload.passengers(),
            payload.chests(),
            getRemainingFuel(payload.chests(), payload.passengers(), computer)
        ), null);
    }

    private static double getRemainingFuel(final List<RocketManifestChest> chests,
                                           final List<RocketManifestPassenger> passengers,
                                           final Block computer) {
        final double cargoMass = calculateCargoMass(chests);
        final double dryMass = ROCKET_DRY_MASS_KG + cargoMass + passengers.size() * SITTING_PLAYER_MASS_KG;
        final double wetMass = FlightComputer.getFuelKg(computer) + dryMass;
        return wetMass / (Math.exp(getDeltaVMetersPerSecond() / EXHAUST_VELOCITY_METERS_PER_SECOND)) - dryMass;
    }

    private static double getDeltaVMetersPerSecond() {
        return JavaPlugin.getPlugin(ZorwethPlugin.class).getDeltaVMetersPerSecond();
    }

    private static Integer getDiamondFlightComputerGroupId(final Block computer) {
        final Reinforcement reinforcement = ReinforcementLogic.getReinforcementAt(computer.getLocation());
        if (reinforcement == null || reinforcement.getType().getItem() == null
            || reinforcement.getType().getItem().getType() != Material.DIAMOND) {
            return null;
        }
        return reinforcement.getGroupId();
    }

    public static void commitLaunch(final ZorwethPlugin plugin, final Block computer, final Player clicker) {
        final LaunchHandler.RocketManifestResult manifestResult = LaunchHandler.collectLaunchManifest(plugin, computer, clicker, plugin.getRocketClipboard());
        if (manifestResult.failure() != null) {
            clicker.sendMessage(manifestResult.failure());
            return;
        }

        final RocketManifest manifest = manifestResult.manifest();
        final LaunchHandler.FuelStatus fuelStatus = LaunchHandler.calculateFuelStatus(computer, manifest.passengers(), manifest.chests());
        if (fuelStatus.currentFuelKg() < fuelStatus.requiredFuelKg()) {
            clicker.sendMessage(Component.text("Rocket is insufficiently fuelled", NamedTextColor.RED));
            return;
        }

        final RocketBlockPosition destinationOrigin = findDestinationOrigin(manifest);
        if (destinationOrigin == null) {
            clicker.sendMessage(Component.text("Destination world is not available for landing calculation.", NamedTextColor.RED));
            return;
        }

        final List<RocketPassengerTransfer> passengers;
        final List<RocketChestTransfer> chests;
        try {
            passengers = RocketManifestSerializer.serializePassengers(manifest);
            chests = RocketManifestSerializer.serializeChests(manifest);
        } catch (final RuntimeException exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to serialize rocket transfer payload", exception);
            clicker.sendMessage(Component.text("Failed to prepare rocket transfer payload.", NamedTextColor.RED));
            return;
        }

        clearPassengerState(manifest);
        setSourceClearedMarkers(manifest);
        clearRocket(plugin.getRocketClipboard(), computer);

        for (RocketPassengerTransfer passenger : passengers) {
            plugin.getStasisHandler().putInStasis(Bukkit.getPlayer(passenger.playerUuid()));
        }

        clicker.sendMessage(Component.text("Preparing rocket transfer.", NamedTextColor.GREEN));
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (int i = 0; i < 5; i++) {
                if (i > 0) {
                    try {
                        Thread.sleep((1 << (i - 1)) * 1000L);
                        plugin.getLogger().log(Level.WARNING, "Retrying transfer #" + i + "...");
                    } catch (final InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                try {
                    plugin.getRocketTransferDao().insertPreparedTransfer(manifest, passengers, chests);
                    break;
                } catch (final Exception exception) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to insert prepared rocket transfer", exception);
                }
                if (i == 4) {
                    clicker.sendMessage(Component.text("Rocket launch failed, please contact admins.", NamedTextColor.RED));

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        for (RocketPassengerTransfer passenger : passengers) {
                            Player player = Bukkit.getPlayer(passenger.playerUuid());
                            if (player != null) {
                                plugin.getStasisHandler().removeStasis(player);
                                clearSourceMarkers(player);
                            } else {
                                plugin.getLogger().log(Level.SEVERE, "Unable to clear launch markers from " + passenger.playerUuid());
                            }
                        }
                    });

                    try {
                        plugin.getLogger().log(Level.SEVERE, "Failed to retry launch, writing file..");
                        File write = RocketManifestFileWriter.write(plugin, manifest);
                        plugin.getLogger().log(Level.SEVERE, "Written launch failure to " + write.getName());
                    } catch (final IOException exception) {
                        throw new RuntimeException(exception);
                    }
                    return;
                }
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                LaunchHandler.connectOrKickPassengers(plugin, manifest);
                clicker.sendMessage(Component.text("Ignition.", NamedTextColor.GREEN));
            });
        });
    }

    private static RocketBlockPosition findDestinationOrigin(final RocketManifest manifest) {
        final World destinationWorld = Bukkit.getWorld(manifest.destinationWorld());
        if (destinationWorld == null) {
            return null;
        }
        final int y = destinationWorld.getHighestBlockYAt(manifest.destinationRequestedX(), manifest.destinationRequestedZ());
        return new RocketBlockPosition(manifest.destinationRequestedX(), y, manifest.destinationRequestedZ());
    }

    private static void clearRocket(Clipboard clipboard, Block computer) {
        final Region region = clipboard.getRegion();
        final BlockVector3 schematicNorthWestCorner = region.getMinimumPoint();
        final Block origin = FlightComputer.getRocketOrigin(computer);

        for (final BlockVector3 position : region) {
            final BlockVector3 relative = position.subtract(schematicNorthWestCorner);
            final Block actualBlock = origin.getRelative(relative.getX(), relative.getY(), relative.getZ());
            Reinforcement reinforcement = ReinforcementLogic.getReinforcementAt(actualBlock.getLocation());
            if (reinforcement != null) {
                reinforcement.setHealth(-1);
            }
            actualBlock.setType(Material.AIR, false);
        }
    }

    private static void clearPassengerState(final RocketManifest manifest) {
        for (final RocketManifestPassenger passenger : manifest.passengers()) {
            final Player player = Bukkit.getPlayer(passenger.playerUuid());
            if (player == null) {
                continue;
            }
            player.getInventory().clear();
            player.setLevel(0);
            player.setExp(0.0f);
            player.setFoodLevel(20);
            player.setSaturation(5.0f);
            player.setExhaustion(0.0f);
            player.getInventory().setHeldItemSlot(0);
        }
    }

    private static void setSourceClearedMarkers(final RocketManifest manifest) {
        for (final RocketManifestPassenger passenger : manifest.passengers()) {
            final Player player = Bukkit.getPlayer(passenger.playerUuid());
            if (player == null) {
                continue;
            }
            player.getPersistentDataContainer().set(RocketTransferKeys.SOURCE_TRANSFER_ID, PersistentDataType.STRING,
                manifest.transferId().toString());
        }
    }

    private static void clearSourceMarkers(Player player) {
        player.getPersistentDataContainer().remove(RocketTransferKeys.SOURCE_TRANSFER_ID);
    }

    public static void connectOrKickPassengers(final ZorwethPlugin plugin, final RocketManifest manifest) {
        for (final RocketManifestPassenger passenger : manifest.passengers()) {
            final Player player = Bukkit.getPlayer(passenger.playerUuid());
            if (player == null) {
                continue;
            }
            connect(plugin, player, manifest.destinationServer());
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                final Player laterPlayer = Bukkit.getPlayer(passenger.playerUuid());
                if (laterPlayer != null
                    && manifest.transferId().toString().equals(laterPlayer.getPersistentDataContainer()
                    .get(RocketTransferKeys.SOURCE_TRANSFER_ID, PersistentDataType.STRING))) {
                    laterPlayer.kick(Component.text(plugin.getTransferFailureMessage(), NamedTextColor.RED));
                }
            }, 150L);
        }
    }

    private static void connect(final JavaPlugin plugin, final Player player, final String server) {
        final ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("Connect");
        output.writeUTF(server);
        player.sendPluginMessage(plugin, "BungeeCord", output.toByteArray());
    }


    public record FuelStatus(int fuelItems, double currentFuelKg, double requiredFuelKg, int requiredFuelItems,
                             double cargoMassKg, int sittingPlayers) {

    }

    public record RocketWeightPayload(List<RocketManifestPassenger> passengers, List<RocketManifestChest> chests,
                                      double fuelKg) {

    }

    public record RocketManifestResult(RocketManifest manifest, Component failure) {

    }
}
