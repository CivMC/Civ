package com.untamedears.itemexchange.events;

import com.google.common.base.Preconditions;
import com.untamedears.itemexchange.rules.TradeRule;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.util.Validation;

/**
 *
 */
public class BrowseOrPurchaseEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final TradeRule rule;
	private boolean cancelled;

	public BrowseOrPurchaseEvent(TradeRule rule) {
		Preconditions.checkArgument(Validation.checkValidity(rule));
		this.rule = rule;
	}

	public TradeRule getRule() {
		return this.rule;
	}

	public boolean isLimited() {
		return this.cancelled;
	}

	public void limitToBrowsing() {
		this.cancelled = true;
	}

	/**
	 * @deprecated DO NOT USE THIS! Use {@link BrowseOrPurchaseEvent#isLimited()} ()} instead.
	 */
	@Deprecated
	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	/**
	 * @deprecated DO NOT USE THIS! Use {@link BrowseOrPurchaseEvent#limitToBrowsing()} instead.
	 */
	@Deprecated
	@Override
	public void setCancelled(boolean cancel) {
		throw new NotImplementedException();
	}

	@NotNull
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public static BrowseOrPurchaseEvent emit(TradeRule rule) {
		BrowseOrPurchaseEvent event = new BrowseOrPurchaseEvent(rule);
		Bukkit.getPluginManager().callEvent(event);
		return event;
	}

}
