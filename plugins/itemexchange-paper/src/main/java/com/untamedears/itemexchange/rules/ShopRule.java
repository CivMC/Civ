package com.untamedears.itemexchange.rules;

import static com.untamedears.itemexchange.rules.ExchangeRule.Type;

import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vg.civcraft.mc.civmodcore.api.InventoryAPI;
import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.util.Iteration;
import vg.civcraft.mc.civmodcore.util.Validation;

/**
 * Class that represents an entire shop.
 */
public final class ShopRule implements Validation {

	private static final Logger LOGGER = LoggerFactory.getLogger(ShopRule.class.getSimpleName());

	private final List<TradeRule> trades = new ArrayList<>();

	private int currentTradeIndex;

	@Override
	public boolean isValid() {
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
			int stock = trade.calculateStock();
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
		ShopRule shop = new ShopRule();
		Type previousType = null;
		TradeRule currentTrade = new TradeRule(inventory);
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
						currentTrade = new TradeRule(inventory);
					}
				}
				else {
					currentTrade = new TradeRule(inventory);
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

	/**
	 * Gets all the inventories that contain the trades this ShopRule describes.
	 * @return All the inventories backing this shop.
	 */
	public List<Inventory> getInventories() {
		HashMap<Location, Inventory> inventories = new HashMap<>();

		for (TradeRule rule : getTrades()) {
			inventories.put(rule.getInventory().getLocation(), rule.getInventory());
		}

		return new ArrayList<>(inventories.values());
	}

	/**
	 * Merge another ShopRule into this.
	 *
	 * This wil create a ShopRule that draws upon two (or more) chests.
	 *
	 * This ShopRule will be mutated, but the other will be left intact.
	 *
	 * The rules of the other ShopRule will be added to the end of this one.
	 * @param shopRule The rule to be merged into this one.
	 */
	public void mergeWithShopRule(ShopRule shopRule) {
		trades.addAll(shopRule.getTrades());
	}
}
