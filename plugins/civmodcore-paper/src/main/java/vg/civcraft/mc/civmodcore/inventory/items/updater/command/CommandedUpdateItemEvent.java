package vg.civcraft.mc.civmodcore.inventory.items.updater.command;

import java.util.Objects;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.items.updater.ItemUpdater;

/**
 * This is emitted by {@link ItemUpdaterCommand#updateHeldItem(org.bukkit.entity.Player)},
 * which is reserved for admins (or anyone else with the "cmc.debug" permission). This MUST NOT be emitted for an empty
 * item (as determined by {@link vg.civcraft.mc.civmodcore.inventory.items.ItemUtils#isEmptyItem(org.bukkit.inventory.ItemStack)})!
 */
public final class CommandedUpdateItemEvent extends Event {
    private final ItemStack item;
    private boolean updated;

    public CommandedUpdateItemEvent(
        final @NotNull ItemStack item
    ) {
        this.item = Objects.requireNonNull(item);
        this.updated = false;
    }

    public void updateItemUsing(
        final @NotNull ItemUpdater updater
    ) {
        this.updated |= updater.updateItem(this.item);
    }

    public boolean wasItemUpdated() {
        return this.updated;
    }

    // ============================================================
    // Bukkit Boilerplate
    // ============================================================

    private static final HandlerList handlers = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }
}
