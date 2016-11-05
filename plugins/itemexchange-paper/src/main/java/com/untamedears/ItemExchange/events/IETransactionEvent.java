package com.untamedears.ItemExchange.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class IETransactionEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private Player player;
	private Location location;
	private ItemStack[] input;
	private ItemStack[] output;

	public IETransactionEvent(Player player, Location location, ItemStack[] input, ItemStack[] output) {
		this.player = player;
		this.location = location;
		this.input = input;
		this.output = output;
	}

	public Player getPlayer() {
		return player;
	}

	public Location getExchangeLocation() {
		return location;
	}
	
	public ItemStack[] getInput() {
		return input;
	}
	
	public ItemStack[] getOutput() {
		return output;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
