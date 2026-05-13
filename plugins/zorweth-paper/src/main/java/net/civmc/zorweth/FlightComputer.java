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
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import vg.civcraft.mc.civmodcore.inventory.gui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;
import vg.civcraft.mc.civmodcore.inventory.gui.components.ComponableInventory;
import vg.civcraft.mc.civmodcore.inventory.gui.components.Scrollbar;
import vg.civcraft.mc.civmodcore.inventory.gui.components.SlotPredicates;
import vg.civcraft.mc.civmodcore.inventory.gui.components.StaticDisplaySection;

public final class FlightComputer implements Listener {

    public static final BlockVector3 RELATIVE_POSITION = BlockVector3.at(5, 17, 7);
    public static final NamespacedKey ROCKET_COMPUTER_KEY = new NamespacedKey("zorweth", "rocket_computer");

    private static final int CONTENT_SLOTS = 45;
    private static final int SCROLL_OFFSET = 5;

    private final ZorwethPlugin plugin;

    public FlightComputer(final ZorwethPlugin plugin) {
        this.plugin = plugin;
    }

    public static boolean isFlightComputerPosition(final BlockVector3 relative) {
        return RELATIVE_POSITION.equals(relative);
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
        final Block origin = computer.getRelative(
            -RELATIVE_POSITION.getX(),
            -RELATIVE_POSITION.getY(),
            -RELATIVE_POSITION.getZ()
        );

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

        final StaticDisplaySection summary = new StaticDisplaySection(9);
        summary.set(new DecorationStack(createSummaryItem(matching, total, mismatches.isEmpty())), 4);
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
                Component.text(complete ? "Rocket is go for launch." : "Mismatched block types are listed above.", NamedTextColor.GRAY)
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

    private Material getDisplayMaterial(final Material material) {
        if (material.isAir() || !material.isItem()) {
            return Material.BARRIER;
        }
        return material;
    }

    private String formatRelativePosition(final Location relative) {
        return "At " + relative.getBlockX() + ", " + relative.getBlockY() + ", " + relative.getBlockZ();
    }
}
