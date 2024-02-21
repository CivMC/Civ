package com.untamedears.itemexchange.events;

import com.untamedears.itemexchange.rules.TradeRule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class SuccessfulPurchaseEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private final Player player;
	private final TradeRule trade;
	private final ItemStack[] input;
	private final ItemStack[] output;

	private SuccessfulPurchaseEvent(Player player, TradeRule trade, ItemStack[] input, ItemStack[] output) {
		this.player = player;
		this.trade = trade;
		this.input = input;
		this.output = output;
	}

	public Player getPurchaser() {
		return this.player;
	}

	public TradeRule getTrade() {
		return this.trade;
	}

	public ItemStack[] getPaymentItems() {
		return this.input;
	}

	public ItemStack[] getPurchasedItems() {
		return this.output;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public static SuccessfulPurchaseEvent emit(Player player, TradeRule trade, ItemStack[] input, ItemStack[] output) {
		SuccessfulPurchaseEvent event = new SuccessfulPurchaseEvent(player, trade, input, output);
		Bukkit.getPluginManager().callEvent(event);
		return event;
	}

}
