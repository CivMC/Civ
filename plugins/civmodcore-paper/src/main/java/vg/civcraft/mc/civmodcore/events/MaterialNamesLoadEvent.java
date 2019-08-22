package vg.civcraft.mc.civmodcore.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;

public final class MaterialNamesLoadEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	@Override
	@Nonnull
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
