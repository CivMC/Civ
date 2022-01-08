package com.github.devotedmc.hiddenore.events;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class HiddenOreEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private final Player player;
	private final Location dropLocation;
	private List<ItemStack> drops;

	public HiddenOreEvent(final Player player, final Location dropLocation, List<ItemStack> drops) {
		super(false);
		this.player = player;
		this.dropLocation = dropLocation;
		this.drops = new ArrayList<>(drops);
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

	public Player getPlayer() {
		return player;
	}

	public Location getDropLocation() {
		return dropLocation;
	}

	public List<ItemStack> getDrops() {
		return drops;
	}

	public void setDrops(List<ItemStack> drops) {
		if (drops == this.drops) return;
		this.drops.clear();
		this.drops.addAll(drops);
	}
}
