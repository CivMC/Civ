package net.civmc.zorweth.flight;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import net.civmc.zorweth.RocketTransferKeys;
import net.civmc.zorweth.StasisHandler;
import net.civmc.zorweth.ZorwethPlugin;
import net.civmc.zorweth.transfer.RocketBlockPosition;
import net.civmc.zorweth.transfer.RocketEntityPosition;
import net.civmc.zorweth.transfer.RocketChestTransfer;
import net.civmc.zorweth.transfer.RocketManifest;
import net.civmc.zorweth.transfer.RocketManifestChest;
import net.civmc.zorweth.transfer.RocketManifestPassenger;
import net.civmc.zorweth.transfer.RocketManifestSerializer;
import net.civmc.zorweth.transfer.RocketPassengerTransfer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.view.AnvilView;
import org.bukkit.persistence.PersistentDataType;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventory.gui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;
import vg.civcraft.mc.civmodcore.inventory.gui.components.ComponableInventory;
import vg.civcraft.mc.civmodcore.inventory.gui.components.Scrollbar;
import vg.civcraft.mc.civmodcore.inventory.gui.components.SlotPredicates;
import vg.civcraft.mc.civmodcore.inventory.gui.components.StaticDisplaySection;

public final class FlightComputer implements Listener {

    public static final BlockVector3 RELATIVE_POSITION = BlockVector3.at(5, 17, 7);
    public static final NamespacedKey ROCKET_COMPUTER_KEY = new NamespacedKey("zorweth", "rocket_computer");
    public static final NamespacedKey ROCKET_FUEL_KEY = new NamespacedKey("zorweth", "rocket_fuel");
    public static final NamespacedKey ROCKET_DESTINATION_X_KEY = new NamespacedKey("zorweth", "rocket_destination_x");
    public static final NamespacedKey ROCKET_DESTINATION_Z_KEY = new NamespacedKey("zorweth", "rocket_destination_z");

    private static final double CHARCOAL_FUEL_KG = 4.0;
    private static final double DELTA_V_METERS_PER_SECOND = 10_000.0;
    private static final double EXHAUST_VELOCITY_METERS_PER_SECOND = 5_000.0;
    private static final double ROCKET_DRY_MASS_KG = 100.0;
    private static final double SITTING_PLAYER_MASS_KG = 50.0;
    private static final int CONTENT_SLOTS = 27;
    private static final int SCROLL_OFFSET = 5;

    private final ZorwethPlugin plugin;
    private final Map<Player, Inventory> coordinateAnvils = new HashMap<>();
    private final Map<Player, Block> coordinateComputers = new HashMap<>();

    private final StasisHandler invincibilityHandler;

    public FlightComputer(final ZorwethPlugin plugin, StasisHandler invincibilityHandler) {
        this.plugin = plugin;
        this.invincibilityHandler = invincibilityHandler;
    }

    public static boolean isFlightComputerPosition(final BlockVector3 relative) {
        return RELATIVE_POSITION.equals(relative);
    }

    public static boolean isFuel(final ItemStack item) {
        return item != null && item.getType() == Material.CHARCOAL;
    }

    public static boolean isSittingWithGSit(final Player player) {
        if (!player.isInsideVehicle()) {
            return false;
        }
        Material on = player.getLocation().getBlock().getRelative(BlockFace.UP).getType();
        if (!(on == Material.POLISHED_ANDESITE_STAIRS || on == Material.POLISHED_ANDESITE_SLAB)) {
            return false;
        }
        final Entity vehicle = player.getVehicle();
        return vehicle != null && vehicle.getScoreboardTags().contains("GSit_sit");
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (event.getAction().isLeftClick()) {
            return;
        }
        final Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.DISPENSER) {
            return;
        }
        final Dispenser dispenser = (Dispenser) block.getState(false);
        if (!dispenser.getPersistentDataContainer().getOrDefault(ROCKET_COMPUTER_KEY, PersistentDataType.BOOLEAN, false)) {
            return;
        }

        event.setCancelled(true);
        open(event.getPlayer(), block);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInventoryClick(final InventoryClickEvent event) {
        final Inventory anvil = this.coordinateAnvils.get(event.getWhoClicked());
        if (anvil == null || anvil != event.getInventory()) {
            return;
        }

        event.setCancelled(true);
        if (event.getClickedInventory() != event.getInventory() || event.getSlot() != 2) {
            return;
        }

        final String text = ((AnvilView) event.getView()).getRenameText();
        if (text == null) {
            return;
        }
        final Player player = (Player) event.getWhoClicked();
        final Block computer = this.coordinateComputers.get(player);
        final Coordinates coordinates = parseCoordinates(text);
        if (coordinates == null) {
            player.sendMessage(Component.text("Enter coordinates in the form X Y Z, for example: 1 2 3", NamedTextColor.RED));
            return;
        }

        setDestination(computer, coordinates);
        this.coordinateComputers.remove(player);
        this.coordinateAnvils.remove(player).clear();
        if (player.getOpenInventory().getTopInventory() == event.getInventory()) {
            player.closeInventory();
        }
        player.sendMessage(Component.text("Destination set to " + formatCoordinates(coordinates) + ".", NamedTextColor.GREEN));
        Bukkit.getScheduler().runTask(this.plugin, () -> open(player, computer));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInventoryClose(final InventoryCloseEvent event) {
        final Inventory anvil = this.coordinateAnvils.remove(event.getPlayer());
        if (anvil != null) {
            anvil.clear();
            this.coordinateComputers.remove(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        final Inventory anvil = this.coordinateAnvils.remove(event.getPlayer());
        if (anvil != null) {
            anvil.clear();
            this.coordinateComputers.remove(event.getPlayer());
        }
    }

    private void open(final Player player, final Block computer) {
        final Clipboard clipboard = this.plugin.getRocketClipboard();
        final Region region = clipboard.getRegion();
        final BlockVector3 schematicNorthWestCorner = region.getMinimumPoint();
        final Block origin = getRocketOrigin(computer);

        int matching = 0;
        int total = 0;
        final List<IClickable> mismatches = new ArrayList<>();
        for (final BlockVector3 position : region) {
            final BlockVector3 relative = position.subtract(schematicNorthWestCorner);
            final com.sk89q.worldedit.world.block.BlockState expectedState = clipboard.getBlock(position);
            final Material expected = Bukkit.createBlockData(expectedState.getAsString()).getMaterial();
            final Block actualBlock = origin.getRelative(relative.getX(), relative.getY(), relative.getZ());
            final Material actual = actualBlock.getType();
            total++;
            if (actual == expected) {
                matching++;
                continue;
            }
            mismatches.add(new DecorationStack(createMismatchItem(actualBlock.getLocation(), expected, actual)));
        }

        final ComponableInventory inventory = new ComponableInventory("Flight Computer", 4, player);
        final Scrollbar scrollbar = new Scrollbar(mismatches, CONTENT_SLOTS, SCROLL_OFFSET);
        inventory.addComponent(scrollbar, SlotPredicates.rows(3));

        final RocketManifestResult manifestResult = collectLaunchManifest(computer, player);
        final FuelStatus fuelStatus = manifestResult.manifest() == null
            ? calculateFuelStatus(computer, List.of(), List.of())
            : calculateFuelStatus(computer, manifestResult.manifest());
        final StaticDisplaySection summary = new StaticDisplaySection(9);
        summary.set(createAddFuelButton(computer), 1);
        summary.set(createSiphonFuelButton(computer), 2);
        summary.set(new DecorationStack(createFuelStatusItem(fuelStatus)), 3);

        summary.set(new DecorationStack(createSummaryItem(matching, total, mismatches.isEmpty())), 5);

        summary.set(createCoordinatesButton(computer), 7);
        summary.set(createLaunchButton(computer), 8);

        inventory.addComponent(summary, SlotPredicates.rows(1));
        inventory.show();
    }

    private Block getRocketOrigin(final Block computer) {
        return computer.getRelative(
            -RELATIVE_POSITION.getX(),
            -RELATIVE_POSITION.getY(),
            -RELATIVE_POSITION.getZ()
        );
    }

    private ItemStack createSummaryItem(final int matching, final int total, final boolean complete) {
        final ItemStack item = new ItemStack(complete ? Material.LIME_CONCRETE : Material.RED_CONCRETE);
        item.editMeta(meta -> {
            meta.displayName(Component.text("Rocket Structure", complete ? NamedTextColor.GREEN : NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                Component.text("Matching blocks: " + matching + "/" + total, NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false),
                Component.text(complete ? "Rocket is structurally intact." : "Launch not possible due to structural integrity violations.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
            ));
        });
        return item;
    }

    private RocketManifestResult collectLaunchManifest(final Block computer, final Player player) {
        final Clipboard clipboard = this.plugin.getRocketClipboard();
        final Region region = clipboard.getRegion();
        final BlockVector3 schematicNorthWestCorner = region.getMinimumPoint();
        final Block origin = getRocketOrigin(computer);
        final List<RocketManifestChest> chests = new ArrayList<>();

        for (final BlockVector3 position : region) {
            final BlockVector3 relative = position.subtract(schematicNorthWestCorner);
            final com.sk89q.worldedit.world.block.BlockState expectedState = clipboard.getBlock(position);
            final Material expected = Bukkit.createBlockData(expectedState.getAsString()).getMaterial();
            final Block actualBlock = origin.getRelative(relative.getX(), relative.getY(), relative.getZ());
            final Material actual = actualBlock.getType();
            if (actual != expected) {
                return new RocketManifestResult(null,
                    Component.text("Rocket is not structurally intact", NamedTextColor.RED));
            }
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
            final boolean insideRocket = region.contains(schematicNorthWestCorner.add(relative));
            if (insideRocket && !isSittingWithGSit(seated)) {
                return new RocketManifestResult(null,
                    Component.text((seated.equals(player) ? "Pilot" : "Passenger") + " " + seated.getName()
                        + " is not seated.", NamedTextColor.RED));
            }
            if (insideRocket) {
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
        }

        final Coordinates destination = getDestination(computer);
        if (destination == null) {
            return new RocketManifestResult(null,
                Component.text("Destination not set.", NamedTextColor.RED));
        }

        final double cargoMass = calculateCargoMass(chests);
        final double dryMass = ROCKET_DRY_MASS_KG + cargoMass + passengers.size() * SITTING_PLAYER_MASS_KG;
        final double wetMass = getFuelKg(computer) + dryMass;
        final double remainingFuel = wetMass / (Math.exp(DELTA_V_METERS_PER_SECOND / EXHAUST_VELOCITY_METERS_PER_SECOND)) - dryMass;

        return new RocketManifestResult(createManifest(origin, destination, passengers, chests, remainingFuel), null);
    }

    private RocketManifest createManifest(final Block origin, final Coordinates destination,
                                          final List<RocketManifestPassenger> passengers,
                                          final List<RocketManifestChest> chests,
                                          final double fuelKg) {
        return new RocketManifest(
            UUID.randomUUID(),
            this.plugin.getServerName(),
            this.plugin.getDestinationServer(),
            origin.getWorld().getName(),
            this.plugin.getDestinationWorld(),
            new RocketBlockPosition(origin.getX(), origin.getY(), origin.getZ()),
            destination.x(),
            destination.z(),
            passengers,
            chests,
            fuelKg
        );
    }

    private ItemStack createMismatchItem(final Location location, final Material expected, final Material actual) {
        final ItemStack item = new ItemStack(expected.isAir() ? getDisplayMaterial(actual) : getDisplayMaterial(expected));
        item.editMeta(meta -> {
            meta.displayName(Component.text(formatRelativePosition(location), NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                Component.empty().append(Component.text("Expected: ")).append(Component.translatable(expected.translationKey()))
                    .decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY),
                Component.empty().append(Component.text("Actual: ")).append(Component.translatable(actual.translationKey()))
                    .decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY)
            ));
        });
        return item;
    }

    private IClickable createAddFuelButton(final Block computer) {
        final ItemStack item = new ItemStack(Material.CHARCOAL);
        item.editMeta(meta -> {
            meta.displayName(Component.text("Add Fuel", NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                Component.text("Adds charcoal from your hand.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false),
                Component.text("1 charcoal = 4 kg fuel.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
            ));
        });
        return new Clickable(item) {

            @Override
            public void clicked(final Player clicker) {
                addFuel(clicker, computer);
                open(clicker, computer);
            }
        };
    }

    private IClickable createSiphonFuelButton(final Block computer) {
        final ItemStack item = new ItemStack(Material.HOPPER);
        item.editMeta(meta -> {
            meta.displayName(Component.text("Siphon Fuel", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                Component.text("Removes up to one stack of fuel", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false),
                Component.text("into your hand.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
            ));
        });
        return new Clickable(item) {

            @Override
            public void clicked(final Player clicker) {
                siphonFuel(clicker, computer);
                open(clicker, computer);
            }
        };
    }

    private IClickable createLaunchButton(final Block computer) {
        final ItemStack item = new ItemStack(Material.FIREWORK_ROCKET);
        item.unsetData(DataComponentTypes.FIREWORKS);
        item.editMeta(meta -> {
            meta.displayName(Component.text("Launch", NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                Component.text("Conduct pre-flight systems check", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false),
                Component.text("and start main engine throttle.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
            ));
        });
        return new Clickable(item) {

            @Override
            public void clicked(final Player clicker) {
                final RocketManifestResult manifestResult = collectLaunchManifest(computer, clicker);
                if (manifestResult.failure() != null) {
                    clicker.sendMessage(manifestResult.failure());
                    ClickableInventory.forceCloseInventory(clicker);
                    return;
                }
                final FuelStatus fuelStatus = calculateFuelStatus(computer, manifestResult.manifest());
                if (fuelStatus.currentFuelKg < fuelStatus.requiredFuelKg) {
                    clicker.sendMessage(Component.text("Rocket is insufficiently fuelled", NamedTextColor.RED));
                    ClickableInventory.forceCloseInventory(clicker);
                    return;
                }
                openLaunchConfirmation(clicker, computer, manifestResult.manifest(), fuelStatus);
            }
        };
    }

    private IClickable createCoordinatesButton(final Block computer) {
        final ItemStack item = new ItemStack(Material.COMPASS);
        item.editMeta(meta -> {
            final Coordinates destination = getDestination(computer);
            meta.displayName(Component.text("Coordinates", NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                Component.text("Set destination as X Z, for example: 1000 -1000", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false),
                Component.text("Current: " + (destination == null ? "not set" : formatCoordinates(destination)), NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
            ));
        });
        return new Clickable(item) {

            @Override
            public void clicked(final Player clicker) {
                openCoordinateInput(clicker, computer);
            }
        };
    }

    private void openCoordinateInput(final Player player, final Block computer) {
        final InventoryView view = MenuType.ANVIL.builder()
            .title(Component.text("Coordinates"))
            .build(player);
        player.openInventory(view);

        this.coordinateAnvils.put(player, view.getTopInventory());
        this.coordinateComputers.put(player, computer);
        final ItemStack input = new ItemStack(Material.WRITABLE_BOOK);
        input.editMeta(meta -> {
            meta.displayName(Component.text("").decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(Component.text("Format: X Z", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)));
        });
        view.getTopInventory().setItem(0, input);
    }

    private void openLaunchConfirmation(final Player player, final Block computer, final RocketManifest manifest,
                                        final FuelStatus fuelStatus) {
        final ClickableInventory inventory = new ClickableInventory(27, "Begin ignition?");
        inventory.setSlot(new DecorationStack(createConfirmInfoItem(manifest, fuelStatus)), 13);
        inventory.setSlot(new Clickable(createConfirmLaunchItem()) {

            @Override
            public void clicked(final Player clicker) {
                ClickableInventory.forceCloseInventory(clicker);
                commitLaunch(computer, clicker);
            }
        }, 11);
        inventory.setSlot(new Clickable(createCancelLaunchItem()) {

            @Override
            public void clicked(final Player clicker) {
                open(clicker, computer);
            }
        }, 15);
        inventory.showInventory(player);
    }

    private ItemStack createConfirmInfoItem(final RocketManifest manifest, final FuelStatus fuelStatus) {
        final ItemStack item = new ItemStack(Material.BELL);
        item.editMeta(meta -> {
            meta.displayName(Component.text("Confirm launch and ignite engines", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                Component.text("Destination: " + manifest.destinationRequestedX() + " " + manifest.destinationRequestedZ(), NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false),
                Component.text("Passengers: " + manifest.passengers().size(), NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false),
                Component.text("Required fuel: " + roundUpTenths(fuelStatus.requiredFuelKg()) + " kg ("
                        + fuelStatus.requiredFuelItems() + " charcoal)", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false),
                Component.text("Once engines have been started, there is no going back.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
            ));
        });
        return item;
    }

    private ItemStack createConfirmLaunchItem() {
        final ItemStack item = new ItemStack(Material.LIME_CONCRETE);
        item.editMeta(meta -> meta.displayName(Component.text("Confirm launch and ignite engines", NamedTextColor.GREEN)
            .decoration(TextDecoration.ITALIC, false)));
        return item;
    }

    private ItemStack createCancelLaunchItem() {
        final ItemStack item = new ItemStack(Material.RED_CONCRETE);
        item.editMeta(meta -> meta.displayName(Component.text("Cancel", NamedTextColor.RED)
            .decoration(TextDecoration.ITALIC, false)));
        return item;
    }

    private void commitLaunch(final Block computer, final Player clicker) {
        final RocketManifestResult manifestResult = collectLaunchManifest(computer, clicker);
        if (manifestResult.failure() != null) {
            clicker.sendMessage(manifestResult.failure());
            return;
        }

        final RocketManifest manifest = manifestResult.manifest();
        final FuelStatus fuelStatus = calculateFuelStatus(computer, manifest);
        if (fuelStatus.currentFuelKg < fuelStatus.requiredFuelKg) {
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
            this.plugin.getLogger().log(Level.SEVERE, "Failed to serialize rocket transfer payload", exception);
            clicker.sendMessage(Component.text("Failed to prepare rocket transfer payload.", NamedTextColor.RED));
            return;
        }

        clearPassengerState(manifest);
        setSourceClearedMarkers(manifest);
        clearRocket(computer);

        for (RocketPassengerTransfer passenger : passengers) {
            invincibilityHandler.putInStasis(Bukkit.getPlayer(passenger.playerUuid()));
        }

        clicker.sendMessage(Component.text("Preparing rocket transfer.", NamedTextColor.GREEN));
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            for (int i = 0; i < 5; i++) {
                if (i > 0) {
                    try {
                        Thread.sleep((1 << (i - 1)) * 1000L);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                try {
                    this.plugin.getRocketTransferDao().insertPreparedTransfer(manifest, destinationOrigin, passengers, chests);
                    break;
                } catch (final Exception exception) {
                    this.plugin.getLogger().log(Level.SEVERE, "Failed to insert prepared rocket transfer", exception);
                }
                if (i == 4) {
                    this.plugin.getLogger().log(Level.SEVERE, "Failed to retry launch");
                    clicker.sendMessage(Component.text("Rocket launch failed, please contact admins.", NamedTextColor.RED));

                    for (RocketPassengerTransfer passenger : passengers) {
                        Player player = Bukkit.getPlayer(passenger.playerUuid());
                        if (player != null) {
                            invincibilityHandler.removeStasis(player);
                        }
                    }
                }
            }

            Bukkit.getScheduler().runTask(this.plugin, () -> {
                connectOrKickPassengers(manifest);
                clicker.sendMessage(Component.text("Ignition.", NamedTextColor.GREEN));
            });
        });
    }

    private RocketBlockPosition findDestinationOrigin(final RocketManifest manifest) {
        final World destinationWorld = Bukkit.getWorld(manifest.destinationWorld());
        if (destinationWorld == null) {
            return null;
        }
        final int y = destinationWorld.getHighestBlockYAt(manifest.destinationRequestedX(), manifest.destinationRequestedZ());
        return new RocketBlockPosition(manifest.destinationRequestedX(), y, manifest.destinationRequestedZ());
    }

    private void clearPassengerState(final RocketManifest manifest) {
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

    private void setSourceClearedMarkers(final RocketManifest manifest) {
        for (final RocketManifestPassenger passenger : manifest.passengers()) {
            final Player player = Bukkit.getPlayer(passenger.playerUuid());
            if (player == null) {
                continue;
            }
            player.getPersistentDataContainer().set(RocketTransferKeys.SOURCE_TRANSFER_ID, PersistentDataType.STRING,
                manifest.transferId().toString());
            player.getPersistentDataContainer().set(RocketTransferKeys.SOURCE_CLEARED, PersistentDataType.BOOLEAN, true);
        }
    }

    // TODO ??
    private void clearSourceMarkers(final RocketManifest manifest) {
        for (final RocketManifestPassenger passenger : manifest.passengers()) {
            final Player player = Bukkit.getPlayer(passenger.playerUuid());
            if (player == null) {
                continue;
            }
            player.getPersistentDataContainer().remove(RocketTransferKeys.SOURCE_TRANSFER_ID);
            player.getPersistentDataContainer().remove(RocketTransferKeys.SOURCE_CLEARED);
        }
    }

    private void connectOrKickPassengers(final RocketManifest manifest) {
        for (final RocketManifestPassenger passenger : manifest.passengers()) {
            final Player player = Bukkit.getPlayer(passenger.playerUuid());
            if (player == null) {
                continue;
            }
            connect(player, manifest.destinationServer());
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                if (player.isOnline()
                    && manifest.transferId().toString().equals(player.getPersistentDataContainer()
                    .get(RocketTransferKeys.SOURCE_TRANSFER_ID, PersistentDataType.STRING))) {
                    player.kick(Component.text(this.plugin.getTransferFailureMessage(), NamedTextColor.RED));
                }
            }, 100L);
        }
    }

    private void connect(final Player player, final String server) {
        final ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("Connect");
        output.writeUTF(server);
        player.sendPluginMessage(this.plugin, "BungeeCord", output.toByteArray());
    }

    private ItemStack createFuelStatusItem(final FuelStatus status) {
        final boolean enoughFuel = status.fuelItems() >= status.requiredFuelItems();
        final ItemStack item = new ItemStack(enoughFuel ? Material.LIME_DYE : Material.RED_DYE);
        item.editMeta(meta -> {
            meta.displayName(Component.text("Fuel Status", enoughFuel ? NamedTextColor.GREEN : NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                Component.text("Fuel: " + status.currentFuelKg() + " kg (" + status.fuelItems() + " charcoal)", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false),
                Component.text("Required: " + roundUpTenths(status.requiredFuelKg()) + " kg (" + status.requiredFuelItems() + " charcoal)", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false),
                Component.text("Cargo mass: " + roundUpTenths(status.cargoMassKg()) + " kg", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false),
                Component.text("Sitting players: " + roundUpTenths(status.sittingPlayers() * SITTING_PLAYER_MASS_KG) + " kg (" + status.sittingPlayers() + " × " + roundUpTenths(SITTING_PLAYER_MASS_KG) + " kg)", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
            ));
        });
        return item;
    }

    private void addFuel(final Player player, final Block computer) {
        final ItemStack hand = player.getInventory().getItemInMainHand();
        if (!isFuel(hand)) {
            player.sendMessage(Component.text("Hold charcoal in your main hand to add fuel.", NamedTextColor.RED));
            return;
        }

        final int amount = hand.getAmount();
        setFuelKg(computer, getFuelKg(computer) + (amount * CHARCOAL_FUEL_KG));
        player.getInventory().setItemInMainHand(null);
        player.sendMessage(Component.text("Added " + amount + " charcoal to the rocket.", NamedTextColor.GREEN));
    }

    private void siphonFuel(final Player player, final Block computer) {
        final double stored = getFuelKg(computer);
        if (stored <= 0) {
            player.sendMessage(Component.text("This rocket has no fuel to siphon.", NamedTextColor.RED));
            return;
        }

        final ItemStack hand = player.getInventory().getItemInMainHand();
        final int space;
        if (hand.getType().isAir()) {
            space = Material.CHARCOAL.getMaxStackSize();
        } else if (isFuel(hand)) {
            space = hand.getMaxStackSize() - hand.getAmount();
        } else {
            player.sendMessage(Component.text("Empty your main hand or hold charcoal to siphon fuel.", NamedTextColor.RED));
            return;
        }
        if (space <= 0) {
            player.sendMessage(Component.text("Your main hand cannot hold more fuel.", NamedTextColor.RED));
            return;
        }

        final int removed = Math.min((int) (stored / CHARCOAL_FUEL_KG), space);
        if (hand.getType().isAir()) {
            player.getInventory().setItemInMainHand(new ItemStack(Material.CHARCOAL, removed));
        } else {
            hand.setAmount(hand.getAmount() + removed);
        }
        setFuelKg(computer, stored - (removed * CHARCOAL_FUEL_KG));
        player.sendMessage(Component.text("Siphoned " + removed + " charcoal from the rocket.", NamedTextColor.GREEN));
    }

    private FuelStatus calculateFuelStatus(final Block computer, final RocketManifest manifest) {
        return calculateFuelStatus(computer, manifest.passengers(), manifest.chests());
    }

    private FuelStatus calculateFuelStatus(final Block computer, final List<RocketManifestPassenger> passengers,
                                           final List<RocketManifestChest> chests) {
        final double cargoMass = calculateCargoMass(chests);
        final int sittingPlayers = Math.max(1, passengers.size());
        final double nonFuelMass = ROCKET_DRY_MASS_KG + cargoMass + sittingPlayers * SITTING_PLAYER_MASS_KG;
        final double requiredFuelKg = nonFuelMass * (Math.exp(DELTA_V_METERS_PER_SECOND / EXHAUST_VELOCITY_METERS_PER_SECOND) - 1.0);
        final double fuelKg = getFuelKg(computer);
        return new FuelStatus(
            (int) (fuelKg / CHARCOAL_FUEL_KG),
            fuelKg,
            requiredFuelKg,
            (int) Math.ceil(requiredFuelKg / CHARCOAL_FUEL_KG),
            cargoMass,
            sittingPlayers
        );
    }

    private double calculateCargoMass(final List<RocketManifestChest> chests) {
        double mass = 0.0;
        for (final RocketManifestChest chest : chests) {
            mass += calculateChestMass(chest.contents());
        }
        return mass;
    }

    private double calculateChestMass(final ItemStack[] contents) {
        double mass = 0.0;
        for (final ItemStack item : contents) {
            if (item == null || item.getType().isAir()) {
                continue;
            }
            // TODO increase weight for fuel
            mass += (double) item.getAmount() / item.getMaxStackSize();
        }
        return mass;
    }

    private double getFuelKg(final Block computer) {
        final Dispenser dispenser = (Dispenser) computer.getState(false);
        return dispenser.getPersistentDataContainer().getOrDefault(ROCKET_FUEL_KEY, PersistentDataType.DOUBLE, 0D);
    }

    private void setFuelKg(final Block computer, final double fuelKg) {
        final Dispenser dispenser = (Dispenser) computer.getState(false);
        if (fuelKg <= 0) {
            dispenser.getPersistentDataContainer().remove(ROCKET_FUEL_KEY);
        } else {
            dispenser.getPersistentDataContainer().set(ROCKET_FUEL_KEY, PersistentDataType.DOUBLE, fuelKg);
        }
        dispenser.update(true, false);
    }

    private Coordinates getDestination(final Block computer) {
        final Dispenser dispenser = (Dispenser) computer.getState(false);
        final Integer x = dispenser.getPersistentDataContainer().get(ROCKET_DESTINATION_X_KEY, PersistentDataType.INTEGER);
        final Integer z = dispenser.getPersistentDataContainer().get(ROCKET_DESTINATION_Z_KEY, PersistentDataType.INTEGER);
        if (x == null || z == null) {
            return null;
        }
        return new Coordinates(x, z);
    }

    private void setDestination(final Block computer, final Coordinates coordinates) {
        final Dispenser dispenser = (Dispenser) computer.getState(false);
        dispenser.getPersistentDataContainer().set(ROCKET_DESTINATION_X_KEY, PersistentDataType.INTEGER, coordinates.x());
        dispenser.getPersistentDataContainer().set(ROCKET_DESTINATION_Z_KEY, PersistentDataType.INTEGER, coordinates.z());
        dispenser.update(true, false);
    }

    private Coordinates parseCoordinates(final String input) {
        final String[] parts = input.trim().split("\\s+");
        if (parts.length != 2) {
            return null;
        }
        try {
            return new Coordinates(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        } catch (final NumberFormatException exception) {
            return null;
        }
    }

    private String formatCoordinates(final Coordinates coordinates) {
        return coordinates.x() + " " + coordinates.z();
    }

    private String roundUpTenths(final double amount) {
        return String.format("%.1f", Math.ceil(amount * 10.0) / 10.0);
    }

    private Material getDisplayMaterial(final Material material) {
        if (material.isAir() || !material.isItem()) {
            return Material.BARRIER;
        }
        return material;
    }

    private String formatRelativePosition(final Location relative) {
        return "At " + relative.getBlockX() + ", " + relative.getBlockY() + ", " + relative.getBlockZ();
    }

    private record FuelStatus(int fuelItems, double currentFuelKg, double requiredFuelKg, int requiredFuelItems,
                              double cargoMassKg, int sittingPlayers) {

    }

    private record RocketManifestResult(RocketManifest manifest, Component failure) {

    }

    private record Coordinates(int x, int z) {

    }

    private void clearRocket(Block computer) {
        final Clipboard clipboard = this.plugin.getRocketClipboard();
        final Region region = clipboard.getRegion();
        final BlockVector3 schematicNorthWestCorner = region.getMinimumPoint();
        final Block origin = getRocketOrigin(computer);

        for (final BlockVector3 position : region) {
            final BlockVector3 relative = position.subtract(schematicNorthWestCorner);
            final Block actualBlock = origin.getRelative(relative.getX(), relative.getY(), relative.getZ());
            actualBlock.setType(Material.AIR);
        }
    }
}
