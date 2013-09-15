/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.untamedears.ItemExchange.listeners;

import static org.bukkit.event.block.Action.LEFT_CLICK_BLOCK;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.untamedears.ItemExchange.ItemExchangePlugin;
import com.untamedears.ItemExchange.exceptions.ExchangeRuleParseException;
import com.untamedears.ItemExchange.utility.ExchangeRule;
import com.untamedears.ItemExchange.utility.ItemExchange;

/**
 * 
 * @author Brian Landry
 */
public class ItemExchangeListener implements Listener {
	/**
	 * Constructor
	 */
	public ItemExchangeListener() {
	}

	/*
	 * Responds when a player interacts with a shop
	 */
	@EventHandler
	public void playerInteractionEvent(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		ItemStack itemStack = e.getItem();
		// If a player using an interacting action
		if (e.getAction() == LEFT_CLICK_BLOCK) {
			// If block is a possible exchange
			if (ItemExchangePlugin.ACCEPTABLE_BLOCKS.contains(e.getClickedBlock().getType())) {
				// If the block contains exchangeItems
				Inventory exchangeInventory = ((InventoryHolder) e.getClickedBlock().getState()).getInventory();
				ItemExchange itemExchange = ItemExchange.getItemExchange(exchangeInventory);
				if (itemExchange.isValid()) {
					itemExchange.playerResponse(player, itemStack, e.getClickedBlock().getLocation());
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		final Inventory top = event.getView().getTopInventory();
		
		if(top instanceof CraftingInventory && event.getWhoClicked() instanceof Player) {
			final Player player = (Player) event.getWhoClicked();
			
			Bukkit.getScheduler().scheduleSyncDelayedTask(ItemExchangePlugin.instance, new Runnable() {
				@SuppressWarnings("deprecation")
				public void run() {
					CraftingInventory inv = (CraftingInventory) top;
					
					List<ExchangeRule> exchangeRules = new ArrayList<ExchangeRule>();
					
					for(ItemStack item : inv.getMatrix()) {
						if(item != null && item.getType() != Material.AIR) {
							try {
								exchangeRules.add(ExchangeRule.parseRuleBlock(item));
							}
							catch(ExchangeRuleParseException e) {
								try {
									exchangeRules.addAll(Arrays.asList(ExchangeRule.parseBulkRuleBlock(item)));
								}
								catch(ExchangeRuleParseException e2) {
									return;
								}
							}
						}
					}

					if(exchangeRules.size() > 0) {
						inv.setResult(ExchangeRule.toBulkItemStack(exchangeRules));

						player.updateInventory();
					}
				}
			});
		}
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		try {
			ExchangeRule[] rules = ExchangeRule.parseBulkRuleBlock(event.getItemDrop().getItemStack());
			
			Item drop = event.getItemDrop();
			
			for(ExchangeRule rule : rules) {
				ItemStack item = rule.toItemStack();
				
				Item ruleDrop = drop.getWorld().dropItem(drop.getLocation(), item);
				
				ruleDrop.setVelocity(drop.getVelocity());
			}
			
			drop.remove();
		}
		catch (ExchangeRuleParseException e) {
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onInventoryMove(InventoryMoveItemEvent event) {
		ItemStack item = event.getItem();
		
		try {
			ExchangeRule.parseRuleBlock(item);
		}
		catch(ExchangeRuleParseException e) {
			try {
				ExchangeRule.parseBulkRuleBlock(item);
			}
			catch(ExchangeRuleParseException e2) {
				return;
			}
		}
		
		event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onInventoryPickupItem(InventoryPickupItemEvent event) {
		if(event.getInventory().getType() != InventoryType.HOPPER) {
			return;
		}
		
		ItemStack item = event.getItem().getItemStack();
		
		try {
			ExchangeRule.parseRuleBlock(item);
		}
		catch(ExchangeRuleParseException e) {
			try {
				ExchangeRule.parseBulkRuleBlock(item);
			}
			catch(ExchangeRuleParseException e2) {
				return;
			}
		}
		
		event.setCancelled(true);
	}
}
