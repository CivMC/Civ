package net.civmc.zorweth;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockState;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
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

    private static final double CHARCOAL_FUEL_KG = 4.0;
    private static final double DELTA_V_METERS_PER_SECOND = 10_000.0;
    private static final double EXHAUST_VELOCITY_METERS_PER_SECOND = 5_000.0;
    private static final double ROCKET_DRY_MASS_KG = 100.0;
    private static final double SITTING_PLAYER_MASS_KG = 50.0;
    private static final int CONTENT_SLOTS = 45;
    private static final int SCROLL_OFFSET = 5;

    private final ZorwethPlugin plugin;

    public FlightComputer(final ZorwethPlugin plugin) {
        this.plugin = plugin;
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
            final BlockState expectedState = clipboard.getBlock(position);
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

        final ComponableInventory inventory = new ComponableInventory("Flight Computer", 6, player);
        final Scrollbar scrollbar = new Scrollbar(mismatches, CONTENT_SLOTS, SCROLL_OFFSET);
        inventory.addComponent(scrollbar, SlotPredicates.rows(5));

        final FuelStatus fuelStatus = calculateFuelStatus(computer, origin, region, schematicNorthWestCorner);
        final StaticDisplaySection summary = new StaticDisplaySection(9);
        summary.set(createAddFuelButton(computer), 1);
        summary.set(new DecorationStack(createSummaryItem(matching, total, mismatches.isEmpty())), 3);
        summary.set(new DecorationStack(createFuelStatusItem(fuelStatus)), 5);
        summary.set(createSiphonFuelButton(computer), 7);
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
        setFuelItems(computer, getFuelItems(computer) + amount);
        player.getInventory().setItemInMainHand(null);
        player.sendMessage(Component.text("Added " + amount + " charcoal to the rocket.", NamedTextColor.GREEN));
    }

    private void siphonFuel(final Player player, final Block computer) {
        final int stored = getFuelItems(computer);
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

        final int removed = Math.min(stored, space);
        if (hand == null || hand.getType().isAir()) {
            player.getInventory().setItemInMainHand(new ItemStack(Material.CHARCOAL, removed));
        } else {
            hand.setAmount(hand.getAmount() + removed);
        }
        setFuelItems(computer, stored - removed);
        player.sendMessage(Component.text("Siphoned " + removed + " charcoal from the rocket.", NamedTextColor.GREEN));
    }

    private FuelStatus calculateFuelStatus(final Block computer, final Block origin, final Region region,
                                           final BlockVector3 schematicNorthWestCorner) {
        final double cargoMass = calculateCargoMass(origin, region, schematicNorthWestCorner);
        final int sittingPlayers = countSittingPlayers(origin, region, schematicNorthWestCorner);
        final double nonFuelMass = ROCKET_DRY_MASS_KG + cargoMass + sittingPlayers * SITTING_PLAYER_MASS_KG;
        final double requiredFuelKg = nonFuelMass * (Math.exp(DELTA_V_METERS_PER_SECOND / EXHAUST_VELOCITY_METERS_PER_SECOND) - 1.0);
        final int fuelItems = getFuelItems(computer);
        return new FuelStatus(
            fuelItems,
            fuelItems * CHARCOAL_FUEL_KG,
            requiredFuelKg,
            (int) Math.ceil(requiredFuelKg / CHARCOAL_FUEL_KG),
            cargoMass,
            sittingPlayers
        );
    }

    private double calculateCargoMass(final Block origin, final Region region, final BlockVector3 schematicNorthWestCorner) {
        double mass = 0.0;
        for (final BlockVector3 position : region) {
            final BlockVector3 relative = position.subtract(schematicNorthWestCorner);
            final Block block = origin.getRelative(relative.getX(), relative.getY(), relative.getZ());
            if (!(block.getState(false) instanceof Chest chest)) {
                continue;
            }
            mass += calculateChestMass(chest.getBlockInventory());
        }
        return mass;
    }

    private double calculateChestMass(final Inventory inventory) {
        double mass = 0.0;
        for (final ItemStack item : inventory.getStorageContents()) {
            if (item == null || item.getType().isAir()) {
                continue;
            }
            mass += (double) item.getAmount() / item.getMaxStackSize();
        }
        return mass;
    }

    private int countSittingPlayers(final Block origin, final Region region, final BlockVector3 schematicNorthWestCorner) {
        int sittingPlayers = 0;
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (!isSittingWithGSit(player) || !player.getWorld().equals(origin.getWorld())) {
                continue;
            }
            final Location location = player.getLocation();
            final BlockVector3 relative = BlockVector3.at(
                location.getBlockX() - origin.getX(),
                location.getBlockY() - origin.getY(),
                location.getBlockZ() - origin.getZ()
            );
            if (region.contains(schematicNorthWestCorner.add(relative))) {
                sittingPlayers++;
            }
        }
        return sittingPlayers;
    }

    private int getFuelItems(final Block computer) {
        final Dispenser dispenser = (Dispenser) computer.getState(false);
        return dispenser.getPersistentDataContainer().getOrDefault(ROCKET_FUEL_KEY, PersistentDataType.INTEGER, 0);
    }

    private void setFuelItems(final Block computer, final int fuelItems) {
        final Dispenser dispenser = (Dispenser) computer.getState(false);
        if (fuelItems <= 0) {
            dispenser.getPersistentDataContainer().remove(ROCKET_FUEL_KEY);
        } else {
            dispenser.getPersistentDataContainer().set(ROCKET_FUEL_KEY, PersistentDataType.INTEGER, fuelItems);
        }
        dispenser.update(true, false);
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
}
