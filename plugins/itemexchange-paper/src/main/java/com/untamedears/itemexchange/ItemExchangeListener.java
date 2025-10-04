package com.untamedears.itemexchange;

import com.untamedears.itemexchange.events.BrowseOrPurchaseEvent;
import com.untamedears.itemexchange.events.SuccessfulPurchaseEvent;
import com.untamedears.itemexchange.rules.BulkExchangeRule;
import com.untamedears.itemexchange.rules.ExchangeRule;
import com.untamedears.itemexchange.rules.ShopRule;
import com.untamedears.itemexchange.rules.TradeRule;
import com.untamedears.itemexchange.utility.TransactionInventory;
import com.untamedears.itemexchange.utility.Utilities;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vg.civcraft.mc.civmodcore.inventory.InventoryAccessor;
import vg.civcraft.mc.civmodcore.inventory.InventoryUtils;
import vg.civcraft.mc.civmodcore.inventory.RecipeManager;
import vg.civcraft.mc.civmodcore.inventory.items.ItemStash;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.utilities.NullUtils;
import vg.civcraft.mc.civmodcore.utilities.Validation;

/**
 * Listener class that handles shop and rule interactions.
 */
public final class ItemExchangeListener implements Listener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemExchangeListener.class);
    private static final long TIME_BETWEEN_CLICKS = 200L;
    private static final long TIME_BEFORE_TIMEOUT = 10000L;
    private final Map<UUID, Long> playerInteractionCooldowns = new Hashtable<>();
    private final Map<UUID, Location> shopRecord = new HashMap<>();
    private final Map<UUID, Integer> ruleIndex = new HashMap<>();

    /**
     * Responds when a player interacts with a shop.
     *
     * @param event The player interaction event itself.
     */
    @EventHandler(ignoreCancelled = true)
    public void playerInteractionEvent(
        final @NotNull PlayerInteractEvent event
    ) {
        final Player player = event.getPlayer();
        // This was here before because apparently it's a fix to a double event triggering issue. Huh.
        if (player == null) {
            return;
        }
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        Block clicked = event.getClickedBlock();
        if (clicked == null) {
            return;
        }
        // Allow an interaction to pass through a sign attached to a shop
        if (clicked.getBlockData() instanceof final WallSign sign) {
            final BlockFace attached = sign.getFacing().getOppositeFace();
            clicked = clicked.getRelative(attached);
        }
        if (!ItemExchangeConfig.canBeInteractedWith(clicked.getType())) {
            return;
        }
        LOGGER.debug("Shop Parsing Starting---------");
        // Limit player interactions to once per 200ms
        long now = System.currentTimeMillis();
        long pre = this.playerInteractionCooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (now - pre < TIME_BETWEEN_CLICKS) {
            LOGGER.debug("Exiting, interacting too quickly.");
            return;
        }
        this.playerInteractionCooldowns.put(player.getUniqueId(), now);
        final ShopRule shop = ShopRule.resolveShop(clicked);
        if (!Validation.checkValidity(shop)) {
            LOGGER.debug("Exiting, that is not a shop");
            return;
        }
        // Cancel the event if the player is in creative, because the mere action
        // of interacting with the shop will destroy the shop... so prevent that.
        if (player.getGameMode() == GameMode.CREATIVE) {
            LOGGER.debug("Cancelling to prevent creative-mode insta-break");
            event.setCancelled(true);
        }
        LOGGER.debug("Shop successfully parsed: {}", shop);
        // Check if the player has interacted with this specific shop before
        // If not then switch over to this shop and display its catalogue
        boolean justBrowsing = false, shouldCycle = true;
        if (!this.shopRecord.containsKey(player.getUniqueId())
            || !NullUtils.equalsNotNull(clicked.getLocation(), this.shopRecord.get(player.getUniqueId()))
            || !this.ruleIndex.containsKey(player.getUniqueId())
        ) {
            this.shopRecord.put(player.getUniqueId(), clicked.getLocation());
            this.ruleIndex.put(player.getUniqueId(), 0);
            justBrowsing = true;
            shouldCycle = false;
            LOGGER.debug("Buyer hasn't interacted with this shop before. Browsing.");
        }
        // If the player hasn't interacted with the shop for a while, then don't insta-purchase on the next interaction.
        if (now - pre > TIME_BEFORE_TIMEOUT) {
            justBrowsing = true;
            LOGGER.debug("Interaction timed out. Browsing.");
        }
        final ItemStack heldItem = event.getItem();
        if (ItemUtils.isEmptyItem(heldItem)) {
            justBrowsing = true;
            LOGGER.debug("Buyer is not holding an input item. Browsing.");
        }
        // Attempt to get the trade from the shop
        shop.setCurrentTradeIndex(this.ruleIndex.getOrDefault(player.getUniqueId(), 0));
        TradeRule trade = shop.getCurrentTrade();
        if (trade == null || !trade.isValid()) {
            this.ruleIndex.remove(player.getUniqueId());
            LOGGER.debug("Cancelling, could not find a valid trade.");
            return;
        }
        LOGGER.debug("Valid trade found:");
        final ExchangeRule inputRule = trade.getInput();
        LOGGER.debug(" - Input: {}", inputRule);
        final ExchangeRule outputRule = trade.getOutput();
        LOGGER.debug(" - Output: {}", outputRule);
        // Check if the input is limited to a group, and if so whether the viewer
        // has permission to purchase from that group. If NameLayer is enabled.
        final BrowseOrPurchaseEvent limitTester = BrowseOrPurchaseEvent.emit(shop, trade, player);
        if (limitTester.isLimited()) {
            justBrowsing = true;
            LOGGER.debug("Buyer cannot purchase from that Group limited trade. Browsing.");
        }
        // If the player's hand is empty or holding the wrong item, just scroll
        // through the catalogue.
        if (justBrowsing || !inputRule.conforms(heldItem)) {
            if (shouldCycle) {
                trade = shop.cycleTrades(!player.isSneaking());
                if (trade == null) {
                    LOGGER.debug("Cancelling, could not find a valid trade when cycling.");
                    this.ruleIndex.remove(player.getUniqueId());
                    return;
                }
                LOGGER.debug("Catalogue cycled.");
                this.ruleIndex.put(player.getUniqueId(), shop.getCurrentTradeIndex());
            }
            LOGGER.debug("Presenting catalogue to buyer.");
            shop.presentShopToPlayer(player);
            return;
        }
        LOGGER.debug("Attempting transaction.");
        // Check that the buyer has enough of the inputs
        final InventoryAccessor playerInventory = InventoryAccessor.playerStorage(player);
        final ItemStash inputItems = inputRule.getStock(playerInventory);
        if (inputItems == null) {
            LOGGER.debug("Cancelling, buyer doesn't have enough of the input.");
            player.sendMessage(ChatColor.RED + "You don't have enough of the input.");
            return;
        }
        // Check that the shop has enough of the outputs if needed
        final InventoryAccessor tradeInventory = InventoryAccessor.fullContents(trade.getInventory());
        ItemStash outputItems = null;
        if (outputRule != null) {
            outputItems = outputRule.getStock(tradeInventory);
            if (outputItems == null) {
                LOGGER.debug("Cancelling, shop doesn't have enough of the output.");
                player.sendMessage(ChatColor.RED + "Shop does not have enough in stock.");
                return;
            }
        }
        // Attempt to transfer the items between the shop and the buyer
        boolean successfulTransfer; {
            final var transactionPlayerInventory = new TransactionInventory(playerInventory);
            final var transactionTradeInventory = new TransactionInventory(tradeInventory);
            if (outputRule != null) {
                successfulTransfer = Utilities.tradedAllItems(
                    transactionPlayerInventory,
                    transactionTradeInventory,
                    inputItems,
                    outputItems
                );
            }
            else {
                successfulTransfer = Utilities.movedAllItems(
                    transactionPlayerInventory,
                    transactionTradeInventory,
                    inputItems
                );
            }
            if (!successfulTransfer) {
                LOGGER.debug("Could not complete that transaction.");
                player.sendMessage(ChatColor.RED + "Could not complete that transaction!");
                return;
            }
            transactionPlayerInventory.commit();
            transactionTradeInventory.commit();
        }
        Stream.of(clicked, trade.getBlock()).distinct().forEach(Utilities::successfulTransactionButton);
        SuccessfulPurchaseEvent.emit(player, trade, inputItems, outputItems);
        if (outputRule != null) {
            player.sendMessage(ChatColor.GREEN + "Successful exchange!");
        } else {
            player.sendMessage(ChatColor.GREEN + "Successful donation!");
        }
    }

    /**
     * Allow players to craft bulk rule items.
     *
     * @param event The prepare item craft event itself.
     */
    @EventHandler(ignoreCancelled = true)
    public void onBulkRuleCraftPrepare(PrepareItemCraftEvent event) {
        if (!RecipeManager.matchRecipe(ItemExchangeConfig.getBulkItemRecipe(), event.getRecipe())) {
            return;
        }
        CraftingInventory inventory = event.getInventory();
        inventory.setResult(null);
        Bukkit.getScheduler().scheduleSyncDelayedTask(ItemExchangePlugin.getInstance(), () -> {
            final var rules = new ArrayList<ExchangeRule>();
            for (final ItemStack item : inventory.getMatrix()) {
                if (!ItemUtils.isValidItem(item)) {
                    continue;
                }
                ExchangeRule exchangeRule = ExchangeRule.fromItem(item);
                if (Validation.checkValidity(exchangeRule)) {
                    rules.add(exchangeRule);
                    continue;
                }
                BulkExchangeRule bulkRule = BulkExchangeRule.fromItem(item);
                if (Validation.checkValidity(bulkRule)) {
                    rules.addAll(bulkRule.rules());
                    continue;
                }
                return;
            }
            final var rule = new BulkExchangeRule(rules);
            inventory.setResult(rule.toItem());
            InventoryUtils.getViewingPlayers(inventory).forEach(Player::updateInventory);
        });
    }

    /**
     * If the player drops a bulk rule item, split it into its constituent rules.
     *
     * @param event The player drop item event itself.
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Item drop = event.getItemDrop();
        BulkExchangeRule bulk = BulkExchangeRule.fromItem(drop.getItemStack());
        if (bulk == null) {
            return;
        }
        drop.remove();
        for (ExchangeRule rule : bulk.rules()) {
            drop.getWorld().dropItem(drop.getLocation(), rule.toItem()).setVelocity(drop.getVelocity());
        }
    }

    /**
     * Prevent rule items from being moved.
     *
     * @param event The inventory move event itself.
     */
    @EventHandler(ignoreCancelled = true)
    public void onInventoryMove(InventoryMoveItemEvent event) {
        if (Utilities.isExchangeRule(event.getItem())) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevent rule items from being picked up by hoppers.
     *
     * @param event The inventory pickup event itself.
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
