/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.untamedears.ItemExchange.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.untamedears.ItemExchange.ItemExchangePlugin;
import com.untamedears.ItemExchange.events.IETransactionEvent;
import com.untamedears.ItemExchange.exceptions.ExchangeRuleParseException;

/**
 *
 * @author Brian Landry
 */
public class ItemExchange {
	private List<ExchangeRule> inputs;
	private List<ExchangeRule> outputs;
	private Inventory inventory;

	public ItemExchange(List<ExchangeRule> inputs, List<ExchangeRule> outputs, Inventory inventory) {
		this.inputs = inputs;
		this.outputs = outputs;
		this.inventory = inventory;
	}

	public static ItemExchange getItemExchange(Inventory inventory) {
		List<ExchangeRule> inputs = new ArrayList<ExchangeRule>();
		List<ExchangeRule> outputs = new ArrayList<ExchangeRule>();
		for (ItemStack itemStack : inventory.getContents()) {
			if (itemStack != null) {
				try {
					ExchangeRule exchangeRule = ExchangeRule.parseRuleBlock(itemStack);
					if (exchangeRule.getType() == ExchangeRule.RuleType.INPUT) {
						inputs.add(exchangeRule);
					}
					else if (exchangeRule.getType() == ExchangeRule.RuleType.OUTPUT) {
						outputs.add(exchangeRule);
					}
				}
				catch (ExchangeRuleParseException e) {

				}
			}
		}
		return new ItemExchange(inputs, outputs, inventory);
	}

	public List<ExchangeRule> getInputs() {
		return inputs;
	}

	public List<ExchangeRule> getOutputs() {
		return outputs;
	}

	/*
	 * Checks if the exchange has both at least one input and one output
	 */
	public boolean isValid() {
		return inputs.size() > 0 && outputs.size() > 0;
	}

	/*
	 * Reports the number of valid input/output rule sets contained within the exchange
	 */
	public int getNumberRules() {
		return inputs.size() < outputs.size() ? inputs.size() : outputs.size();
	}

	public static String createExchange(Location location, Player player) {
		//Bail if location doesn't contain an an accpetable inventory block
		if (ItemExchangePlugin.ACCEPTABLE_BLOCKS.contains(location.getBlock().getType()) && location.getBlock().getState() instanceof InventoryHolder) {
			Inventory inventory = ((InventoryHolder) location.getBlock().getState()).getInventory();
			ItemStack input = null;
			ItemStack output = null;
			//Checks for two different unique types of items in the inventory and sums up their amounts from the individual itemStacks
			for (ItemStack itemStack : inventory) {
				if (itemStack != null) {
					if (input == null) {
						input = itemStack.clone();
					}
					else if (itemStack.isSimilar(input)) {
						input.setAmount(input.getAmount() + itemStack.getAmount());
					}
					else if (output == null) {
						output = itemStack.clone();
					}
					else if (output.isSimilar(itemStack)) {
						output.setAmount(output.getAmount() + itemStack.getAmount());
					}
					else {
						return ChatColor.RED + "Inventory should only contain two types of items!";
					}
				}
			}
			//If acceptable input and output itemStacks were found create exchange rule blocks for each and place them in the inventory blcok
			if (input != null && output != null) {
				ExchangeRule inputRule = ExchangeRule.parseItemStack(input, ExchangeRule.RuleType.INPUT);
				ExchangeRule outputRule = ExchangeRule.parseItemStack(output, ExchangeRule.RuleType.OUTPUT);
				//Place input in inventory, if this fails drop it on the ground
				if (inventory.addItem(inputRule.toItemStack()).size() > 0) {
					player.getWorld().dropItem(player.getLocation(), inputRule.toItemStack());
				}
				//place output in the inventory, if this fails drop it on the ground
				if (inventory.addItem(outputRule.toItemStack()).size() > 0) {
					player.getWorld().dropItem(player.getLocation(), outputRule.toItemStack());
				}
				return ChatColor.GREEN + "Created Exchange Succesfully";
			}
			else {
				return ChatColor.RED + "Inventory should have at least two types of items";
			}
		}
		else {
			return ChatColor.RED + "Not a valid exchange block";
		}
	}

	private static Map<Player, Location> locationRecord = new HashMap<>(100);
	private static Map<Player, Integer> ruleIndex = new HashMap<>(100);

	public void playerResponse(Player player, ItemStack itemStack, Location location) {
		//If the player has interacted with this exchange previously
		if (ruleIndex.containsKey(player) && locationRecord.containsKey(player) && location.equals(locationRecord.get(player))) {
			//If the hand is not empty
			if (itemStack != null) {
				//If the rule index is at a reasonable index
				if (ruleIndex.get(player) < inputs.size() && ruleIndex.get(player) < outputs.size()) {
					ExchangeRule input = inputs.get(ruleIndex.get(player));
					ExchangeRule output = outputs.get(ruleIndex.get(player));
					//Check if item in hand is the input
					if (input.followsRules(itemStack)) {
						PlayerInventory playerInventory = player.getInventory();
						//If the player has the input and the exchange has the output
						if (input.followsRules(playerInventory)) {
							if (output.followsRules(inventory)) {
								ItemStack[] playerInput = InventoryHelpers.getItemStacks(playerInventory, input);
								ItemStack[] exchangeOutput = InventoryHelpers.getItemStacks(inventory, output);
								/*
								 * Attempts to exchange items in the players inventory, if there ends up not being space in either of the inventories
								 * the inventories are reset back to a copy of their prexisting inventories.
								 * This has the potential for edge cases since efery itemstack in the players inventory is being replaced with a copy
								 * of that item. But I haven't thought of any particular issues yet, probably should be tested in relation to prisonpearl.
								*/
								ItemStack[] playerInventoryOld = InventoryHelpers.deepCopy(playerInventory);
								playerInventory.removeItem(playerInput);
								if (playerInventory.addItem(exchangeOutput).isEmpty()) {
									ItemStack[] exchangeInventoryOld = InventoryHelpers.deepCopy(inventory);
									inventory.removeItem(exchangeOutput);
									if (inventory.addItem(playerInput).isEmpty()) {
										IETransactionEvent event = new IETransactionEvent(player, location);

										Bukkit.getPluginManager().callEvent(event);

										player.sendMessage(ChatColor.GREEN + "Succesful exchange!");
									}
									else {
										inventory.setContents(exchangeInventoryOld);
										player.sendMessage(ChatColor.RED + "The Exchange does not have enough inventory space!");
									}
								}
								else {
									playerInventory.setContents(playerInventoryOld);
									player.sendMessage(ChatColor.RED + "You don't have enough inventory space!");
								}
							}
							else {
								player.sendMessage(ChatColor.RED + "Chest does not have enough of the output.");
							}
						}
						else {
							player.sendMessage(ChatColor.RED + "You don't have enough of the input.");
						}
					}
					//If the item the player is holding is not that of the input of the exchange the rules of the exchange are displayed
					else {
						messagePlayer(player);
					}
				}
				//If the rule index was out of bounds
				else {
					ruleIndex.put(player, 0);
				}
			}
			//If the players hand is empty cycle through exchange rules
			else {
				int currentRuleIndex = ruleIndex.get(player);
				if (currentRuleIndex < getNumberRules() - 1) {
					ruleIndex.put(player, ruleIndex.get(player) + 1);
				}
				else {
					ruleIndex.put(player, 0);
				}
				messagePlayer(player);
			}
		}
		//If the player has not interacted with this exchange previously or doesn't have an itemstack in his hand
		//The rules of the item exchange are displayed and first recipe is selected
		else {
			//Records the player interaction with the item exchange
			locationRecord.put(player, location);
			//Set the exchange recipe to the first one
			ruleIndex.put(player, 0);
			messagePlayer(player);
		}
	}

	public void messagePlayer(Player player) {
		player.sendMessage(ChatColor.YELLOW + "(" + String.valueOf(ruleIndex.get(player) + 1) + "/" + String.valueOf(getNumberRules()) + ") exchanges present.");
		player.sendMessage(inputs.get(ruleIndex.get(player)).display());
		player.sendMessage(outputs.get(ruleIndex.get(player)).display());
	}
}
