package com.untamedears.itemexchange.events;

import com.google.common.base.Preconditions;
import com.untamedears.itemexchange.rules.ShopRule;
import com.untamedears.itemexchange.rules.TradeRule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import vg.civcraft.mc.civmodcore.utilities.Validation;

/**
 * <p>Event that's emitted when a player is browsing a shop. This event is used to determine whether a player will be
 * allowed to proceed into a purchase or whether they'll be kept to just browsing.</p>
 */
public class BrowseOrPurchaseEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final ShopRule shop;
	private final TradeRule trade;
	private final Player browser;
	private boolean cancelled;

	private BrowseOrPurchaseEvent(ShopRule shop, TradeRule trade, Player browser) {
		Preconditions.checkArgument(Validation.checkValidity(shop));
		Preconditions.checkArgument(Validation.checkValidity(trade));
		Preconditions.checkArgument(browser != null);
		this.shop = shop;
		this.trade = trade;
		this.browser = browser;
	}

	/**
	 * Retrieves the shop that is being browsed.
	 *
	 * @return Returns the shop being browsed.
	 */
	public ShopRule getShop() {
		return this.shop;
	}

	/**
	 * Retrieves the trade currently being viewed.
	 *
	 * @return Returns the trade being viewed.
	 */
	public TradeRule getTrade() {
		return this.trade;
	}

	/**
	 * Retrieves the player who's browsing.
	 *
	 * @return Returns the browsing player.
	 */
	public Player getBrowser() {
		return this.browser;
	}

	/**
	 * @return Returns whether the browsing player has been limited.
	 */
	public boolean isLimited() {
		return this.cancelled;
	}

	/**
	 * Limits the player to browsing.
	 */
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
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	/**
	 * <p>Creates and emits a browse limit event.</p>
	 *
	 * @param shop The shop being browsed.
	 * @param trade The trade being viewed.
	 * @param browser The player who's browsing.
	 * @return Returns the request event that was emitted and has finished processing.
	 */
	public static BrowseOrPurchaseEvent emit(ShopRule shop, TradeRule trade, Player browser) {
		BrowseOrPurchaseEvent event = new BrowseOrPurchaseEvent(shop, trade, browser);
		Bukkit.getPluginManager().callEvent(event);
		return event;
	}

}
