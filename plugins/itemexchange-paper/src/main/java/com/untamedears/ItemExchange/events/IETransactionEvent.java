package com.untamedears.itemexchange.events;

import com.untamedears.itemexchange.rules.TradeRule;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class IETransactionEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private final Player player;

	private final Inventory inventory;

	private final TradeRule trade;

	private final ItemStack[] input;

	private final ItemStack[] output;

	public IETransactionEvent(Player player, Inventory inventory, TradeRule trade, ItemStack[] input,
							  ItemStack[] output) {
		this.player = player;
		this.inventory = inventory;
		this.trade = trade;
		this.input = input;
		this.output = output;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public Player getPlayer() {
		return this.player;
	}

	public Inventory getInventory() {
		return this.inventory;
	}

	@Deprecated
	public Location getExchangeLocation() {
		return this.inventory.getLocation();
	}

	public TradeRule getTrade() {
		return this.trade;
	}

	public ItemStack[] getInput() {
		return this.input;
	}

	public ItemStack[] getOutput() {
		return this.output;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

}
