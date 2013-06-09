/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.untamedears.ItemExchange.listeners;

import com.untamedears.ItemExchange.ItemExchangePlugin;
import com.untamedears.ItemExchange.utility.InventoryHelpers;
import com.untamedears.ItemExchange.utility.ExchangeRule;
import com.untamedears.ItemExchange.utility.InteractionResponse;
import com.untamedears.ItemExchange.utility.InteractionResponse.InteractionResult;
import com.untamedears.ItemExchange.utility.ItemExchange;
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
			if(ItemExchangePlugin.ACCEPTABLE_BLOCKS.contains(e.getClickedBlock().getType()))
			{
				//If the block contains exchangeItems
				Inventory exchangeInventory = ((InventoryHolder) e.getClickedBlock().getState()).getInventory();
				ItemExchange itemExchange=ItemExchange.getItemExchange(exchangeInventory);
				if(itemExchange.isValid())
				{
					ExchangeRule input=itemExchange.getInputs().get(0);
					ExchangeRule output=itemExchange.getOutputs().get(0);
					PlayerInventory playerInventory=player.getInventory();
					//Check if item in hand is the input
					if(input.followsRules(e.getItem()))
					{
						//If the player has the input and the exchange has the output
						if(input.followsRules(playerInventory))	
						{
							if(output.followsRules(exchangeInventory)){
								ItemStack[] playerInput=InventoryHelpers.getItemStacks(playerInventory,input);
								ItemStack[] exchangeOutput=InventoryHelpers.getItemStacks(exchangeInventory,output);
								//Attempt to exchange items in the players inventory
								ItemStack[] playerInventoryOld=InventoryHelpers.deepCopy(playerInventory);
								playerInventory.removeItem(playerInput);
								if(playerInventory.addItem(exchangeOutput).isEmpty()){
									ItemStack[] exchangeInventoryOld=InventoryHelpers.deepCopy(exchangeInventory);
									exchangeInventory.removeItem(exchangeOutput);
									if(!exchangeInventory.addItem(playerInput).isEmpty()){
										exchangeInventory.setContents(exchangeInventoryOld);
										InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.FAILURE,"The Exchange does not have enough inventory space!"));
									}
								}
								else{
									playerInventory.setContents(playerInventoryOld);
									InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.FAILURE,"You don't have enough inventory space!"));
								}
							}
							else {
								InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.FAILURE,"Chest does not have enough of the output."));
							}
						}
						else {
							InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.FAILURE,"You don't have enough of the input."));
						}
					}
					else {
						InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.FAILURE,"You aren't holding the input."));
					}
				}
				else {
					InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.FAILURE,"Not and exchange!"));
				}
			}
			else {
				InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.FAILURE,"Not Acceptable Block."));
			}
		}
		else {
			InteractionResponse.messagePlayerResult(player, new InteractionResponse(InteractionResult.FAILURE,"Not left click."));
		}
	}
}
