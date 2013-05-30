/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.untamedears.ItemExchange.listeners;

import com.untamedears.ItemExchange.ItemExchangePlugin;
import com.untamedears.ItemExchange.utility.InventoryHelpers;
import com.untamedears.ItemExchange.utility.ItemRule;
import java.util.List;
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
		Block clicked = e.getClickedBlock();
		Player player = e.getPlayer();
		//If a player using an interacting action
		if(e.getAction() == LEFT_CLICK_BLOCK)
		{
			//If block is a possible exchange
			if(ItemExchangePlugin.ACCEPTABLE_BLOCKS.contains(e.getClickedBlock().getType())&& e.getClickedBlock() instanceof InventoryHolder)
			{
				//If the block contains exchangeItems
				Inventory exchangeInventory = ((InventoryHolder) e.getClickedBlock().getState()).getInventory();
				if(InventoryHelpers.hasIO(exchangeInventory))
				{
					ItemRule[] IO=InventoryHelpers.getIO(exchangeInventory);
					ItemRule input=IO[0];
					ItemRule output=IO[1];
					PlayerInventory playerInventory=player.getInventory();
					//Check if item in hand is the input
					if(input.followsRules(e.getItem()))
					{
						//If the player has the input and the exchange has the output
						if(input.followsRules(playerInventory) && output.followsRules(exchangeInventory))	
						{
							List<ItemStack> playerInput=InventoryHelpers.getItemStacks(playerInventory,input);
							List<ItemStack> exchangeOutput=InventoryHelpers.getItemStacks(exchangeInventory,output);
							//Check if inventories hold items
							if(InventoryHelpers.fitsIn(playerInventory, exchangeOutput) && InventoryHelpers.fitsIn(exchangeInventory, playerInput))
							{
								playerInventory.removeItem((ItemStack[])playerInput.toArray());
								exchangeInventory.removeItem((ItemStack[])exchangeOutput.toArray());
								playerInventory.addItem((ItemStack[])exchangeOutput.toArray());
								exchangeInventory.addItem((ItemStack[])playerInput.toArray());
							}
						}
					}
				}
			}
		}
	}
}
