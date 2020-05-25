package com.untamedears.itemexchange.rules;

import static com.untamedears.itemexchange.rules.ExchangeRule.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vg.civcraft.mc.civmodcore.api.InventoryAPI;
import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.api.LocationAPI;
import vg.civcraft.mc.civmodcore.util.Iteration;
import vg.civcraft.mc.civmodcore.util.Validation;

/**
 * Class that represents an entire shop.
 */
public final class ShopRule implements Validation {

	private static final Logger LOGGER = LoggerFactory.getLogger(ShopRule.class.getSimpleName());

	private final Inventory inventory;

	private final List<TradeRule> trades = new ArrayList<>();

	private int currentTradeIndex;

	private ShopRule(Inventory inventory) {
		this.inventory = inventory;
	}

	@Override
	public boolean isValid() {
		if (!InventoryAPI.isValidInventory(this.inventory)) {
			return false;
		}
		if (!LocationAPI.isValidLocation(this.inventory.getLocation())) {
			return false;
		}
		if (Iteration.isNullOrEmpty(this.trades)) {
			return false;
		}
		return true;
	}

	public List<TradeRule> getTrades() {
		return this.trades;
	}

	public int getCurrentTradeIndex() {
		return this.currentTradeIndex;
	}

	public void setCurrentTradeIndex(int currentTrade) {
		this.currentTradeIndex = currentTrade;
	}

	public TradeRule getCurrentTrade() {
		if (this.trades.isEmpty()) {
			return null;
		}
		if (this.currentTradeIndex < 0) {
			return null;
		}
		if (this.currentTradeIndex >= this.trades.size()) {
			return null;
		}
		return this.trades.get(this.currentTradeIndex);
	}

	public TradeRule cycleTrades(boolean forward) {
		if (this.trades.isEmpty()) {
			return null;
		}
		this.currentTradeIndex += forward ? 1 : -1;
		if (this.currentTradeIndex < 0) {
			this.currentTradeIndex = this.trades.size() - 1;
		}
		else if (this.currentTradeIndex >= this.trades.size()) {
			this.currentTradeIndex = 0;
		}
		return getCurrentTrade();
	}

	public void presentShopToPlayer(Player player) {
		TradeRule trade = getCurrentTrade();
		if (trade == null) {
			throw new NullPointerException("Could not message player about trade... this shouldn't happen.");
		}
		player.sendMessage(String.format("%s(%d/%d) exchanges present.",
				ChatColor.YELLOW, this.currentTradeIndex + 1, this.trades.size()));
		for (String line : trade.getInput().getDisplayedInfo()) {
			player.sendMessage(line);
		}
		if (trade.getOutput() != null) {
			for (String line : trade.getOutput().getDisplayedInfo()) {
				player.sendMessage(line);
			}
			LOGGER.debug("[ShopRule] Calculating stock.");
			int stock = trade.getOutput().calculateStock(inventory);
			player.sendMessage(ChatColor.YELLOW + "" + stock + " exchange" + (stock == 1 ? "" : "s") + " available.");
		}
	}

	/**
	 * Attempts to
	 */
	public static ShopRule getShopFromInventory(Inventory inventory) {
		if (!InventoryAPI.isValidInventory(inventory)) {
			return null;
		}
		List<ExchangeRule> found = new ArrayList<>();
		inventory.setContents(Arrays.stream(inventory.getContents()).
				map((item) -> {
					if (!ItemAPI.isValidItem(item)) {
						return item;
					}
					ExchangeRule rule = ExchangeRule.fromItem(item);
					if (rule != null) {
						found.add(rule);
						return rule.toItem();
					}
					BulkExchangeRule bulk = BulkExchangeRule.fromItem(item);
					if (bulk != null) {
						found.addAll(bulk.getRules());
						return bulk.toItem();
					}
					return item;
				}).
				toArray(ItemStack[]::new));
		ShopRule shop = new ShopRule(inventory);
		Type previousType = null;
		TradeRule currentTrade = new TradeRule();
		for (ExchangeRule rule : found) {
			if (rule == null || rule.isBroken()) {
				previousType = null;
				continue;
			}
			Type type = rule.getType();
			if (type == Type.INPUT) {
				if (previousType != null) {
					if (currentTrade.isValid()) {
						shop.trades.add(currentTrade);
						currentTrade = new TradeRule();
					}
				}
				else {
					currentTrade = new TradeRule();
				}
				currentTrade.setInput(rule);
				previousType = Type.INPUT;
			}
			else if (type == Type.OUTPUT) {
				if (previousType != Type.INPUT) {
					previousType = Type.OUTPUT;
					continue;
				}
				currentTrade.setOutput(rule);
				previousType = Type.OUTPUT;
			}
		}
		if (currentTrade.isValid()) {
			shop.trades.add(currentTrade);
		}
		return shop;
	}

}
