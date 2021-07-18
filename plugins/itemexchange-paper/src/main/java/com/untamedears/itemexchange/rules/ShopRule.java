package com.untamedears.itemexchange.rules;

import static com.untamedears.itemexchange.rules.ExchangeRule.Type;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.untamedears.itemexchange.ItemExchangeConfig;
import com.untamedears.itemexchange.ItemExchangePlugin;
import com.untamedears.itemexchange.events.BlockInventoryRequestEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import vg.civcraft.mc.civmodcore.inventory.InventoryUtils;
import vg.civcraft.mc.civmodcore.utilities.Validation;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

/**
 * Class that represents an entire shop.
 */
public final class ShopRule implements Validation {

	private final ItemExchangePlugin PLUGIN = ItemExchangePlugin.getInstance();

	private final List<TradeRule> trades = new ArrayList<>();

	private int currentTradeIndex;

	@Override
	public boolean isValid() {
		if (CollectionUtils.isEmpty(this.trades)) {
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
		for (String line : trade.getInput().getDisplayInfo()) {
			player.sendMessage(line);
		}
		if (trade.getOutput() != null) {
			for (String line : trade.getOutput().getDisplayInfo()) {
				player.sendMessage(line);
			}
			PLUGIN.debug("[ShopRule] Calculating stock.");
			int stock = trade.calculateStock();
			player.sendMessage(ChatColor.YELLOW + "" + stock + " exchange" + (stock == 1 ? "" : "s") + " available.");
		}
	}

	// ------------------------------------------------------------
	// Shop Resolution
	// ------------------------------------------------------------

	private void resolveInventories(final Block block,
									   final Set<Inventory> found,
									   final int remainingRecursion,
									   final BlockFace cameFrom) {
		if (ItemExchangeConfig.hasCompatibleShopBlock(block.getType())) {
			PLUGIN.debug("[RELAY] Found shop block. (Total: " + found.size() + ")");
			BlockInventoryRequestEvent event = BlockInventoryRequestEvent.emit(block, null,
					BlockInventoryRequestEvent.Purpose.INSPECTION);
			final Inventory inventory = event.getInventory();
			if (inventory != null) {
				found.add(inventory);
			}
			return;
		}
		if (ItemExchangeConfig.hasRelayCompatibleBlock(block.getType())) {
			PLUGIN.debug("[RELAY] Found relay block.");
			int reach = ItemExchangeConfig.getRelayReachDistance();
			if (reach <= 0) {
				PLUGIN.debug("[RELAY] Relay has no reach distance.");
				return;
			}
			if (remainingRecursion < 0) {
				PLUGIN.debug("[RELAY] Relay recursion limit reached.");
				return;
			}
			for (BlockFace face : WorldUtils.ALL_SIDES) {
				if (face.equals(cameFrom)) {
					continue;
				}
				PLUGIN.debug("[RELAY] Emitting relay ray trace: " + face.name());
				BlockIterator iterator = WorldUtils.getBlockIterator(block.getRelative(face), face, reach);
				while (iterator.hasNext()) {
					Block current = iterator.next();
					if (ItemExchangeConfig.hasRelayPermeableBlock(current.getType())) {
						PLUGIN.debug("[RELAY] Found permeable block.");
						continue;
					}
					if (!ItemExchangeConfig.canBeInteractedWith(current.getType())) {
						PLUGIN.debug("[RELAY] Ending search.");
						break;
					}
					resolveInventories(current, found, remainingRecursion - 1, face.getOppositeFace());
					break;
				}
			}
		}
	}

	private List<ExchangeRule> extractRulesFromInventory(Inventory inventory) {
		List<ExchangeRule> found = Lists.newArrayList();
		PLUGIN.debug("[Resolve] Searching inventory [" + inventory.getType().name() + "] for exchange rules");
		for (ItemStack item : inventory.getContents()) {
			ExchangeRule rule = ExchangeRule.fromItem(item);
			if (rule != null) {
				PLUGIN.debug("[Resolve] \tExchange Rule found.");
				found.add(rule);
				continue;
			}
			BulkExchangeRule bulk = BulkExchangeRule.fromItem(item);
			if (bulk != null) {
				PLUGIN.debug("[Resolve] \tBulk Exchange Rule found.");
				found.addAll(bulk.getRules());
				//continue;
			}
		}
		return found;
	}

	private List<TradeRule> extractTradesFromInventory(Inventory inventory) {
		List<TradeRule> trades = Lists.newArrayList();
		List<ExchangeRule> rules = extractRulesFromInventory(inventory);
		Type previousType = null;
		TradeRule currentTrade = new TradeRule(inventory);
		// TODO: Future me should git gud and make this better and more readable... like seriously!
		for (ExchangeRule rule : rules) {
			if (rule == null || rule.isBroken()) {
				previousType = null;
				continue;
			}
			Type type = rule.getType();
			if (type == Type.INPUT) {
				if (previousType != null) {
					if (currentTrade.isValid()) {
						trades.add(currentTrade);
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
			trades.add(currentTrade);
		}
		return trades;
	}

	public static ShopRule resolveShop(Block block) {
		if (!WorldUtils.isValidBlock(block)) {
			return null;
		}
		ShopRule shop = new ShopRule();
		Set<Inventory> inventories = Sets.newHashSet();
		shop.resolveInventories(
				block,
				inventories,
				ItemExchangeConfig.getRelayRecursionLimit(),
				BlockFace.SELF);
		inventories.stream()
				.filter(InventoryUtils::isValidInventory)
				.map(shop::extractTradesFromInventory)
				.forEachOrdered(shop.trades::addAll);
		return shop;
	}

}
