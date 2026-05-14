package net.civmc.zorweth.flight;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.civmc.zorweth.ZorwethPlugin;
import static net.civmc.zorweth.flight.FlightComputer.ROCKET_COMPUTER_KEY;
import static net.civmc.zorweth.flight.LaunchHandler.SITTING_PLAYER_MASS_KG;
import net.civmc.zorweth.transfer.RocketManifest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
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

// GUI representation of flight computer state
public final class FlightComputerGui implements Listener {

    private static final int CONTENT_SLOTS = 9;

    private final ZorwethPlugin plugin;
    private final Map<Player, Inventory> coordinateAnvils = new HashMap<>();
    private final Map<Player, Block> coordinateComputers = new HashMap<>();

    public FlightComputerGui(final ZorwethPlugin plugin) {
        this.plugin = plugin;
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

        FlightComputer.setDestination(computer, coordinates);
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
        final Block origin = FlightComputer.getRocketOrigin(computer);

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

        final ComponableInventory inventory = new ComponableInventory("Flight Computer", 2, player);
        final Scrollbar scrollbar = new Scrollbar(mismatches, CONTENT_SLOTS);
        inventory.addComponent(scrollbar, SlotPredicates.rows(1));

        final LaunchHandler.RocketWeightPayload payload = LaunchHandler.collectRocketWeightPayload(computer, plugin.getRocketClipboard());
        final LaunchHandler.FuelStatus fuelStatus = LaunchHandler.calculateFuelStatus(computer, payload.passengers(), payload.chests());
        final StaticDisplaySection summary = new StaticDisplaySection(9);
        summary.set(createAddFuelButton(computer), 1);
        summary.set(createSiphonFuelButton(computer), 2);
        summary.set(new DecorationStack(createFuelStatusItem(fuelStatus)), 3);

        summary.set(new DecorationStack(createSummaryItem(matching, total, mismatches.isEmpty())), 5);

        summary.set(createCoordinatesButton(computer), 7);
        summary.set(createLaunchButton(computer, payload, fuelStatus), 8);

        inventory.addComponent(summary, SlotPredicates.rows(1));
        inventory.show();
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

    private IClickable createLaunchButton(final Block computer, final LaunchHandler.RocketWeightPayload payload,
                                          final LaunchHandler.FuelStatus fuelStatus) {
        final ItemStack item = new ItemStack(Material.FIREWORK_ROCKET);
        item.unsetData(DataComponentTypes.FIREWORKS);
        item.editMeta(meta -> {
            meta.displayName(Component.text("Launch", NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                Component.text("Conduct pre-flight systems check", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false),
                Component.text("and start main engine throttle.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false),
                Component.text("Fuel consumption: " + roundUpTenths(calculateFuelConsumptionKg(payload.fuelKg(), fuelStatus)) + " kg ("
                        + calculateFuelConsumptionItems(payload.fuelKg(), fuelStatus) + " charcoal)", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
            ));
        });
        return new Clickable(item) {

            @Override
            public void clicked(final Player clicker) {
                final LaunchHandler.RocketManifestResult manifestResult = LaunchHandler.collectLaunchManifest(plugin, computer, clicker, plugin.getRocketClipboard());
                if (manifestResult.failure() != null) {
                    clicker.sendMessage(manifestResult.failure());
                    ClickableInventory.forceCloseInventory(clicker);
                    return;
                }
                final LaunchHandler.FuelStatus fuelStatus = LaunchHandler.calculateFuelStatus(computer, manifestResult.manifest().passengers(), manifestResult.manifest().chests());
                if (fuelStatus.currentFuelKg() < fuelStatus.requiredFuelKg()) {
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
            final Coordinates destination = FlightComputer.getDestination(computer);
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
                                        final LaunchHandler.FuelStatus fuelStatus) {
        final ClickableInventory inventory = new ClickableInventory(27, "Begin ignition?");
        inventory.setSlot(new DecorationStack(createConfirmInfoItem(manifest, fuelStatus)), 13);
        inventory.setSlot(new Clickable(createConfirmLaunchItem()) {

            @Override
            public void clicked(final Player clicker) {
                ClickableInventory.forceCloseInventory(clicker);
                LaunchHandler.commitLaunch(plugin, computer, clicker);
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

    private ItemStack createConfirmInfoItem(final RocketManifest manifest, final LaunchHandler.FuelStatus fuelStatus) {
        final ItemStack item = new ItemStack(Material.BELL);
        item.editMeta(meta -> {
            meta.displayName(Component.text("Confirm launch and ignite engines", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                Component.text("Destination: " + manifest.destinationRequestedX() + " " + manifest.destinationRequestedZ(), NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false),
                Component.text("Passengers: " + manifest.passengers().size(), NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false),
                Component.text("Fuel consumption: " + roundUpTenths(calculateFuelConsumptionKg(manifest.fuelKg(), fuelStatus)) + " kg ("
                        + calculateFuelConsumptionItems(manifest.fuelKg(), fuelStatus) + " charcoal)", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false),
                Component.text("Once engines have been started, there is no going back.", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
            ));
        });
        return item;
    }

    private double calculateFuelConsumptionKg(final double fuelKg, final LaunchHandler.FuelStatus fuelStatus) {
        return fuelStatus.currentFuelKg() - fuelKg;
    }

    private int calculateFuelConsumptionItems(final double fuelKg, final LaunchHandler.FuelStatus fuelStatus) {
        return (int) Math.ceil(calculateFuelConsumptionKg(fuelKg, fuelStatus) / LaunchHandler.FUEL_ITEM_MASS_KG);
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

    private ItemStack createFuelStatusItem(final LaunchHandler.FuelStatus status) {
        final boolean enoughFuel = status.fuelItems() >= status.requiredFuelItems();
        final ItemStack item = new ItemStack(enoughFuel ? Material.LIME_DYE : Material.RED_DYE);
        item.editMeta(meta -> {
            meta.displayName(Component.text("Fuel Status", enoughFuel ? NamedTextColor.GREEN : NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                Component.text("Fuel: " + roundUpTenths(status.currentFuelKg()) + " kg (" + status.fuelItems() + " charcoal)", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false),
                Component.text("Required: " + roundUpTenths(status.requiredFuelKg()) + " kg (" + status.requiredFuelItems() + " charcoal)", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false),
                Component.text("Cargo mass: " + roundUpTenths(status.cargoMassKg()) + " kg", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false),
                Component.text("Players sitting: " + roundUpTenths(status.sittingPlayers() * SITTING_PLAYER_MASS_KG) + " kg (" + status.sittingPlayers() + " × " + roundUpTenths(SITTING_PLAYER_MASS_KG) + " kg)", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)
            ));
        });
        return item;
    }

    private void addFuel(final Player player, final Block computer) {
        final ItemStack hand = player.getInventory().getItemInMainHand();
        if (!FlightComputer.isFuel(hand)) {
            player.sendMessage(Component.text("Hold charcoal in your main hand to add fuel.", NamedTextColor.RED));
            return;
        }

        final int amount = hand.getAmount();
        FlightComputer.setFuelKg(computer, FlightComputer.getFuelKg(computer) + (amount * LaunchHandler.FUEL_ITEM_MASS_KG));
        player.getInventory().setItemInMainHand(null);
        player.sendMessage(Component.text("Added " + amount + " charcoal to the rocket.", NamedTextColor.GREEN));
    }

    private void siphonFuel(final Player player, final Block computer) {
        final double stored = FlightComputer.getFuelKg(computer);
        if (stored <= 0) {
            player.sendMessage(Component.text("This rocket has no fuel to siphon.", NamedTextColor.RED));
            return;
        }

        final ItemStack hand = player.getInventory().getItemInMainHand();
        final int space;
        if (hand.getType().isAir()) {
            space = Material.CHARCOAL.getMaxStackSize();
        } else if (FlightComputer.isFuel(hand)) {
            space = hand.getMaxStackSize() - hand.getAmount();
        } else {
            player.sendMessage(Component.text("Empty your main hand or hold charcoal to siphon fuel.", NamedTextColor.RED));
            return;
        }
        if (space <= 0) {
            player.sendMessage(Component.text("Your main hand cannot hold more fuel.", NamedTextColor.RED));
            return;
        }

        final int removed = Math.min((int) (stored / LaunchHandler.FUEL_ITEM_MASS_KG), space);
        if (hand.getType().isAir()) {
            player.getInventory().setItemInMainHand(new ItemStack(Material.CHARCOAL, removed));
        } else {
            hand.setAmount(hand.getAmount() + removed);
        }
        FlightComputer.setFuelKg(computer, stored - (removed * LaunchHandler.FUEL_ITEM_MASS_KG));
        player.sendMessage(Component.text("Siphoned " + removed + " charcoal from the rocket.", NamedTextColor.GREEN));
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

    public record Coordinates(int x, int z) {

    }
}
