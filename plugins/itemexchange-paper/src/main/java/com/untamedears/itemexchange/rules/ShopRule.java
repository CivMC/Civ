package com.untamedears.itemexchange.rules;

import static com.untamedears.itemexchange.rules.ExchangeRule.Type;
import static vg.civcraft.mc.civmodcore.util.NullCoalescing.exists;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.untamedears.itemexchange.ItemExchangeConfig;
import com.untamedears.itemexchange.ItemExchangePlugin;
import com.untamedears.itemexchange.events.BlockInventoryRequestEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import vg.civcraft.mc.civmodcore.api.BlockAPI;
import vg.civcraft.mc.civmodcore.api.InventoryAPI;
import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.api.MaterialAPI;
import vg.civcraft.mc.civmodcore.util.Iteration;
import vg.civcraft.mc.civmodcore.util.Validation;

/**
 * Class that represents an entire shop.
 */
public final class ShopRule implements Validation {

	private final ItemExchangePlugin PLUGIN = ItemExchangePlugin.getInstance();

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
			PLUGIN.debug("[ShopRule] Calculating stock.");
			int stock = trade.calculateStock();
			player.sendMessage(ChatColor.YELLOW + "" + stock + " exchange" + (stock == 1 ? "" : "s") + " available.");
		}
	}

	// ------------------------------------------------------------
	// Shop Resolution
	// ------------------------------------------------------------

	private boolean resolveInventories(final Block block,
											  final Set<Inventory> found,
											  final Set<Material> shopBlocks,
											  final Set<Material> relayBlocks,
											  final int maxDistance,
											  final int relayLimit,
											  final boolean isPermeable,
											  final BlockFace cameFrom) {
		Material material = block.getType();
		if (shopBlocks.contains(material)) {
			PLUGIN.debug("[RELAY] Found shop block. (Total: " + found.size() + ")");
			BlockInventoryRequestEvent event = BlockInventoryRequestEvent.emit(block, null);
			exists(event.getInventory(), found::add);
			return false;
		}
		// If it's not a shop chest, the search continues
		if (relayLimit < 1) {
			PLUGIN.debug("[RELAY] Relay limit reached.");
			return false;
		}
		if (maxDistance < 1) {
			PLUGIN.debug("[RELAY] Relay max distance reached.");
			return false;
		}
		if (relayBlocks.contains(material)) {
			PLUGIN.debug("[RELAY] Found relay block.");
			for (BlockFace face : BlockAPI.ALL_SIDES) {
				if (face.equals(cameFrom)) {
					continue;
				}
				PLUGIN.debug("[RELAY] Emitting relay ray trace: " + face.name());
				BlockIterator iterator = BlockAPI.getBlockIterator(block.getRelative(face), face, maxDistance);
				while (iterator.hasNext()) {
					if (!resolveInventories(
							iterator.next(),
							found,
							shopBlocks,
							relayBlocks,
							maxDistance,
							relayLimit - 1,
							isPermeable,
							face.getOppositeFace())) {
						break;
					}
				}
			}
			return false;
		}
		if (MaterialAPI.isAir(material)) {
			PLUGIN.debug("[RELAY] Found air, continuing search.");
			return true;
		}
		if (material.isOccluding()) {
			PLUGIN.debug("[RELAY] Hit occluding block, cannot continue.");
			return false;
		}
		if (isPermeable) {
			PLUGIN.debug("[RELAY] Found permeable block, continuing search.");
			return true;
		}
		PLUGIN.debug("[RELAY] Hit solid block, cannot continue.");
		return false;
	}

	private List<ExchangeRule> extractRulesFromInventory(Inventory inventory) {
		List<ExchangeRule> found = Lists.newArrayList();
		PLUGIN.debug("[Resolve] Searching inventory [" + inventory.getType().name() + "] for exchange rules");
		for (ItemStack item : inventory.getContents()) {
			if (!ItemAPI.isValidItem(item)) {
				PLUGIN.debug("[Resolve] \tInvalid item skipped.");
				continue;
			}
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
		if (!BlockAPI.isValidBlock(block)) {
			return null;
		}
		ShopRule shop = new ShopRule();
		Set<Inventory> inventories = Sets.newHashSet();
		shop.resolveInventories(
				block,
				inventories,
				ItemExchangeConfig.getShopCompatibleBlocks(),
				ItemExchangeConfig.getShopRelayBlocks(),
				ItemExchangeConfig.getShopRelayReach(),
				ItemExchangeConfig.getShopRelayLimit(),
				ItemExchangeConfig.isShopRelayPermeable(),
				BlockFace.SELF);
		inventories.stream()
				.filter(InventoryAPI::isValidInventory)
				.map(shop::extractTradesFromInventory)
				.forEachOrdered(shop.trades::addAll);
		return shop;
	}

}
