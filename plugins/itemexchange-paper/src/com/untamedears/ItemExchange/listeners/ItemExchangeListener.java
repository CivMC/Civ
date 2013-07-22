/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.untamedears.ItemExchange.listeners;

import com.untamedears.ItemExchange.ItemExchangePlugin;
import com.untamedears.ItemExchange.utility.InventoryHelpers;
import com.untamedears.ItemExchange.utility.ExchangeRule;
import com.untamedears.ItemExchange.utility.ItemExchange;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import static org.bukkit.event.block.Action.LEFT_CLICK_BLOCK;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
/**
 *
 * @author Brian Landry
 */
public class ItemExchangeListener implements Listener{
	/**
	 * Constructor
	 */
	public ItemExchangeListener()
	{
	}
	/*
	 * Responds when a player inteacts with a shop
	 */
	@EventHandler
	public void playerInteractionEvent(PlayerInteractEvent e)
	{
		Player player = e.getPlayer();
		ItemStack itemStack=e.getItem();
		//If a player using an interacting action
		if(e.getAction() == LEFT_CLICK_BLOCK){
			//If block is a possible exchange
			if(ItemExchangePlugin.ACCEPTABLE_BLOCKS.contains(e.getClickedBlock().getType())){
				//If the block contains exchangeItems
				Inventory exchangeInventory = ((InventoryHolder) e.getClickedBlock().getState()).getInventory();
				ItemExchange itemExchange=ItemExchange.getItemExchange(exchangeInventory);
				if(itemExchange.isValid()){
					itemExchange.playerResponse(player,itemStack,e.getClickedBlock().getLocation());					
				}
			}
		}
	}
}
