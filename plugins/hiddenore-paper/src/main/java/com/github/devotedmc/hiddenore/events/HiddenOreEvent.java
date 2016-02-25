package com.github.devotedmc.hiddenore.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HiddenOreEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private final Player player;
	private final Location dropLocation;
	private List<ItemStack> drops;
	
	public HiddenOreEvent(final Player player, final Location dropLocation, List<ItemStack> drops) {
		
	}

	public HiddenOreEvent(boolean isAsync) {
		super(isAsync);
		// TODO Auto-generated constructor stub
	}

	private boolean cancel = false;
	
	@Override
	public boolean isCancelled() {
		return cancel;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancel = cancelled;
	}

	@Override
	public HandlerList getHandlers() {
		return HiddenOreEvent.handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	}

}
