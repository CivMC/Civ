package com.untamedears.itemexchange;

import com.untamedears.itemexchange.events.IETransactionEvent;
import com.untamedears.itemexchange.glue.NameLayerGlue;
import com.untamedears.itemexchange.rules.BulkExchangeRule;
import com.untamedears.itemexchange.rules.ExchangeRule;
import com.untamedears.itemexchange.rules.ShopRule;
import com.untamedears.itemexchange.rules.TradeRule;
import com.untamedears.itemexchange.utility.Utilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.api.BlockAPI;
import vg.civcraft.mc.civmodcore.api.InventoryAPI;
import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.api.RecipeAPI;
import vg.civcraft.mc.civmodcore.util.NullCoalescing;
import vg.civcraft.mc.namelayer.group.Group;

public class ItemExchangeListener implements Listener {

    private static final long TIME_BETWEEN_CLICKS = 200L;
    private static final long TIME_BEFORE_TIMEOUT = 10000L;

    private final Map<Player, Long> playerInteractionCooldowns = new Hashtable<>();
    private final Map<Player, Location> shopRecord = new HashMap<>();
    private final Map<Player, Integer> ruleIndex = new HashMap<>();

    private final ItemExchangePlugin plugin;

    public ItemExchangeListener(ItemExchangePlugin plugin) {
    	this.plugin = plugin;
	}

    /**
     * Responds when a player interacts with a shop
     */
    @EventHandler
    public void playerInteractionEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        // This was here before because apparently it's a fix to
        // a double event triggering issue. Huh.
        if (player == null) {
            return;
        }
        // Interaction must be a block punch
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        //
        Block clicked = event.getClickedBlock();
        if (!BlockAPI.isValidBlock(clicked)) {
            return;
        }
        // Block must be a supported block type
        if (!ItemExchangePlugin.SHOP_BLOCKS.contains(clicked.getType())) {
            return;
        }
        this.plugin.debug("[Shop] Shop Parsing Starting---------");
        // Limit player interactions to once per 200ms
        long now = System.currentTimeMillis();
        long pre = this.playerInteractionCooldowns.getOrDefault(player, 0L);
        if (now - pre < TIME_BETWEEN_CLICKS) {
            this.plugin.debug("[Shop] Cancelling, interacting too quickly.");
            return;
        }
        this.playerInteractionCooldowns.put(player, now);
        // Attempt to create parse a shop from the inventory
        Inventory inventory = NullCoalescing.chain(() ->
                ((InventoryHolder) event.getClickedBlock().getState()).getInventory());
        ShopRule shop = ShopRule.getShopFromInventory(inventory);
        if (shop == null || shop.getTrades().isEmpty()) {
            this.plugin.debug("[Shop] Cancelling, that is not a shop.");
            return;
        }
        // Cancel the event if the player is in creative, because the mere action
        // of interacting with the shop will destroy the shop... so prevent that.
        if (player.getGameMode() == GameMode.CREATIVE) {
            event.setCancelled(true);
        }
        this.plugin.debug("[Shop] Shop successfully parsed.");
        // Check if the player has interacted with this specific shop before
        // If not then switch over to this shop and display its catalogue
        boolean justBrowsing = false;
        boolean shouldCycle = true;
        assert inventory.getLocation() != null;
        if (!this.shopRecord.containsKey(player) ||
				!Objects.equals(inventory.getLocation(), this.shopRecord.get(player)) ||
                !this.ruleIndex.containsKey(player)) {
            this.shopRecord.put(player, inventory.getLocation());
            this.ruleIndex.put(player, 0);
            justBrowsing = true;
            shouldCycle = false;
            this.plugin.debug("[Shop] Buyer hasn't interacted with this shop before. Browsing.");
        }
        // If the player hasn't interacted with the shop for a while, then don't
        // insta-purchase on the next interaction.
        if (now - pre > TIME_BEFORE_TIMEOUT) {
            justBrowsing = true;
            this.plugin.debug("[Shop] Interaction timed out. Browsing.");
        }
        // If the player is holding nothing, just browse
        if (!ItemAPI.isValidItem(event.getItem())) {
            justBrowsing = true;
            this.plugin.debug("[Shop] Buyer is not holding an input item. Browsing.");
        }
        // Attempt to get the trade from the shop
        shop.setCurrentTradeIndex(this.ruleIndex.getOrDefault(player, 0));
        TradeRule trade = shop.getCurrentTrade();
        if (trade == null || !trade.isValid()) {
            this.ruleIndex.remove(player);
            this.plugin.debug("[Shop] Cancelling, could not find a valid trade.");
            return;
        }
        this.plugin.debug("[Shop] Valid trade found.");
        ExchangeRule inputRule = trade.getInput();
        ExchangeRule outputRule = trade.getOutput();
        // Check if the input is limited to a group, and if so whether the viewer
        // has permission to purchase from that group. If NameLayer is enabled.
        if (NameLayerGlue.isEnabled()) {
            Group group = inputRule.getGroup();
            if (group != null) {
                if (!NameLayerGlue.getPurchasePermission().hasAccess(group, player)) {
                    justBrowsing = true;
                    this.plugin.debug("[Shop] Buyer cannot purchase from that Group limited trade. Browsing.");
                }
            }
        }
        // If the player's hand is empty or holding the wrong item, just scroll
        // through the catalogue.
        if (justBrowsing || !inputRule.conforms(event.getItem())) {
            if (shouldCycle) {
                trade = shop.cycleTrades(!player.isSneaking());
                if (trade == null) {
                    this.plugin.debug("[Shop] Cancelling, could not find a valid trade when cycling.");
                    this.ruleIndex.remove(player);
                    return;
                }
                this.plugin.debug("[Shop] Catalogue cycled.");
                this.ruleIndex.put(player, shop.getCurrentTradeIndex());
            }
            this.plugin.debug("[Shop] Presenting catalogue to buyer.");
            shop.presentShopToPlayer(player);
            return;
        }
        this.plugin.debug("[Shop] Attempting transaction.");
        // Check that the buyer has enough of the inputs
        ItemStack[] inputItems = inputRule.getStock(player.getInventory());
        if (inputItems.length < 1) {
            this.plugin.debug("[Shop] Cancelling, buyer doesn't have enough of the input.");
            player.sendMessage(ChatColor.RED + "You don't have enough of the input.");
            return;
        }
        // Check that the shop has enough of the outputs if needed
        ItemStack[] outputItems = new ItemStack[0];
        if (trade.hasOutput()) {
            outputItems = outputRule.getStock(inventory);
            if (outputItems.length < 1) {
                this.plugin.debug("[Shop] Cancelling, shop doesn't have enough of the output.");
                player.sendMessage(ChatColor.RED + "Shop does not have enough in stock.");
                return;
            }
        }
        // Attempt to transfer the items between the shop and the buyer
        boolean successfulTransfer;
        if (trade.hasOutput()) {
            successfulTransfer = InventoryAPI.safelyTradeBetweenInventories(
                    player.getInventory(), inputItems, inventory, outputItems);
        }
        else {
            successfulTransfer = InventoryAPI.safelyTransactBetweenInventories(
                    player.getInventory(), inputItems, inventory);
        }
        if (!successfulTransfer) {
            this.plugin.debug("[Shop] Could not complete that transaction.");
            player.sendMessage(ChatColor.RED + "Could not complete that transaction!");
            return;
        }
        Utilities.successfulTransactionButton(event.getClickedBlock());
        trade.lock();
        Bukkit.getServer().getPluginManager().callEvent(
                new IETransactionEvent(player, inventory, trade, inputItems, outputItems));
        if (trade.hasOutput()) {
            player.sendMessage(ChatColor.GREEN + "Successful exchange!");
        }
        else {
            player.sendMessage(ChatColor.GREEN + "Successful donation!");
        }
    }

    /**
     * Allow players to craft bulk rule items
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInventoryClick(PrepareItemCraftEvent event) {
    	if (!RecipeAPI.matchRecipe(ItemExchangePlugin.BULK_RULE_RECIPE, event.getRecipe())) {
			return;
		}
		CraftingInventory inventory = event.getInventory();
    	inventory.setResult(null);
		Bukkit.getScheduler().scheduleSyncDelayedTask(ItemExchangePlugin.getInstance(), () -> {
			List<ExchangeRule> rules = new ArrayList<>();
			for (ItemStack item : inventory.getMatrix()) {
				if (!ItemAPI.isValidItem(item)) {
					continue;
				}
				ExchangeRule exchangeRule = ExchangeRule.fromItem(item);
				if (exchangeRule != null && exchangeRule.isValid()) {
					rules.add(exchangeRule);
					continue;
				}
				BulkExchangeRule bulkRule = BulkExchangeRule.fromItem(item);
				if (bulkRule != null && bulkRule.isValid()) {
					rules.addAll(bulkRule.getRules());
					continue;
				}
				return;
			}
			BulkExchangeRule rule = new BulkExchangeRule();
			rule.setRules(rules);
			inventory.setResult(rule.toItem());
			InventoryAPI.getViewingPlayers(inventory).forEach(Player::updateInventory);
		});
    }

    /**
     * If the player drops a bulk rule item, split it into its constituent rules.
     */
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Item drop = event.getItemDrop();
        BulkExchangeRule bulk = BulkExchangeRule.fromItem(drop.getItemStack());
        if (bulk == null) {
        	return;
		}
        drop.remove();
		for (ExchangeRule rule : bulk.getRules()) {
			drop.getWorld().dropItem(drop.getLocation(), rule.toItem()).setVelocity(drop.getVelocity());
		}
    }

    /**
     * Prevent rule items from being moved
     */
    @EventHandler(ignoreCancelled = true)
    public void onInventoryMove(InventoryMoveItemEvent event) {
        if (Utilities.isExchangeRule(event.getItem())) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevent rule items from being picked up by hoppers
     */
    @EventHandler(ignoreCancelled = true)
    public void onInventoryPickupItem(InventoryPickupItemEvent event) {
        if (event.getInventory().getType() != InventoryType.HOPPER) {
            return;
        }
        if (Utilities.isExchangeRule(event.getItem().getItemStack())) {
            event.setCancelled(true);
        }
    }

}
