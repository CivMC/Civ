package vg.civcraft.mc.civmodcore.inventory.items.network;

import java.util.Objects;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

/**
 * Keep in mind this event is called on the network thread, so please try not to do anything expensive!
 */
public final class NetworkItemEvent extends Event {
	public final ItemMeta meta;
	private boolean wasUpdated;

	public NetworkItemEvent(
		final @NotNull ItemMeta meta
	) {
		this.meta = Objects.requireNonNull(meta, "'meta' cannot be null!");
	}

	public void markAsUpdated() {
		this.wasUpdated = true;
	}

	public boolean hasMetaBeenUpdated() {
		return this.wasUpdated;
	}

	// ============================================================
	// Boilerplate
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
