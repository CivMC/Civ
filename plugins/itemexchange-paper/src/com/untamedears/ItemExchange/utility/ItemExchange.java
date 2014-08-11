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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.untamedears.ItemExchange.ItemExchangePlugin;
import com.untamedears.ItemExchange.events.IETransactionEvent;
import com.untamedears.ItemExchange.exceptions.ExchangeRuleCreateException;
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
				
				try {
					ExchangeRule[] exchangeRules = ExchangeRule.parseBulkRuleBlock(itemStack);

					for(ExchangeRule exchangeRule : exchangeRules) {
						if (exchangeRule.getType() == ExchangeRule.RuleType.INPUT) {
							inputs.add(exchangeRule);
						}
						else if (exchangeRule.getType() == ExchangeRule.RuleType.OUTPUT) {
							outputs.add(exchangeRule);
						}
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
	 * Checks if the exchange has at least one input and an input for each output
	 */
	public boolean isValid() {
		// Need at least 1 input, reject negative sizes
		if (inputs.size() <= 0 || outputs.size() < 0) {
			return false;
		}
		if (outputs.size() > 0 && inputs.size() < outputs.size()) {
			return false;
		}
		return true;
	}

	/*
	 * Reports the number of valid input/output rule sets contained within the exchange
	 */
	public int getNumberRules() {
		return inputs.size();
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
			//Allow an input without an output for creating redstone trigger/donation boxes
			if (input != null) {
				if(ExchangeRule.isRuleBlock(input)) {
					return ChatColor.RED + "You cannot exchange rule blocks!";
				}
				ExchangeRule inputRule;
				try {
					inputRule = ExchangeRule.parseItemStack(input, ExchangeRule.RuleType.INPUT);
				}
				catch (ExchangeRuleCreateException e) {
					return ChatColor.RED + e.getMessage();
				}
				//Place input in inventory, if this fails drop it on the ground
				if (inventory.addItem(inputRule.toItemStack()).size() > 0) {
					player.getWorld().dropItem(player.getLocation(), inputRule.toItemStack());
				}
				if (output != null) {
					if(ExchangeRule.isRuleBlock(output)) {
						return ChatColor.RED + "You cannot exchange rule blocks!";
					}
					ExchangeRule outputRule;
					try {
						outputRule = ExchangeRule.parseItemStack(output, ExchangeRule.RuleType.OUTPUT);
					}
					catch (ExchangeRuleCreateException e) {
						return ChatColor.RED + e.getMessage();
					}
					//place output in the inventory, if this fails drop it on the ground
					if (inventory.addItem(outputRule.toItemStack()).size() > 0) {
						player.getWorld().dropItem(player.getLocation(), outputRule.toItemStack());
					}
				}
				return ChatColor.GREEN + "Created exchange successfully.";
			}
			else {
				return ChatColor.RED + "Inventory should have at least one type of item.";
			}
		}
		else {
			return ChatColor.RED + "Not a valid exchange block.";
		}
	}

	private static Map<Player, Location> locationRecord = new HashMap<Player, Location>(100);
	private static Map<Player, Integer> ruleIndex = new HashMap<Player, Integer>(100);

	public void playerResponse(Player player, ItemStack itemStack, Location location) {
		//Check if the player has interacted with this exchange previously
		if (!ruleIndex.containsKey(player) || !locationRecord.containsKey(player) || !location.equals(locationRecord.get(player))) {
			//If the player has not interacted with this exchange previously or doesn't have an itemstack in his hand
			//The rules of the item exchange are displayed and first recipe is selected
			//Records the player interaction with the item exchange
			locationRecord.put(player, location);
			//Set the exchange recipe to the first one
			ruleIndex.put(player, 0);
			messagePlayer(player);
			return;
		}
		//Check if the hand is empty
		if (itemStack == null) {
			//If the players hand is empty cycle through exchange rules
			cycleExchange(player);
			return;
		}
		//If the rule index is at a reasonable index
		if (ruleIndex.get(player) >= inputs.size()) {
			//If the rule index was out of bounds
			ruleIndex.put(player, 0);
			return;
		}
		final boolean hasOutput = ruleIndex.get(player) < outputs.size();
		if(!location.getChunk().isLoaded()) {
			player.sendMessage(ChatColor.RED + "Error: The chunk is not loaded.");
			return;
		}

		ExchangeRule input = inputs.get(ruleIndex.get(player));
		ExchangeRule output = hasOutput ? outputs.get(ruleIndex.get(player)) : null;
		//Check if item in hand is the input
		if (!input.followsRules(itemStack)) {
			// If the item the player is holding is not that of the input of the exchange the rules of the exchange are displayed
			cycleExchange(player);
			return;
		}
		if(!input.followsRules(player)) {
			player.sendMessage(ChatColor.RED + "You are not allowed to use this exchange!");
			return;
		}

		PlayerInventory playerInventory = player.getInventory();
		//If the player has the input
		if (!input.followsRules(playerInventory)) {
			player.sendMessage(ChatColor.RED + "You don't have enough of the input.");
			return;
		}
		ItemStack[] exchangeOutput = null;
		if (output != null) {
			if (!output.followsRules(inventory)) {
				player.sendMessage(ChatColor.RED + "Chest does not have enough of the output.");
				return;
			}
			exchangeOutput = InventoryHelpers.getItemStacks(inventory, output);
		}
		ItemStack[] playerInput = InventoryHelpers.getItemStacks(playerInventory, input);
		/*
		 * Attempts to exchange items in the players inventory, if there ends up not being space in either of the inventories
		 * the inventories are reset back to a copy of their prexisting inventories.
		 * This has the potential for edge cases since efery itemstack in the players inventory is being replaced with a copy
		 * of that item. But I haven't thought of any particular issues yet, probably should be tested in relation to prisonpearl.
		 * A try/finally is used to reset the inventories in the event of an error to remove the chance for copy/paste errors
		 * which cause item dup bugs.
		*/
		ItemStack[] playerInventoryOld = InventoryHelpers.deepCopy(playerInventory);
		ItemStack[] exchangeInventoryOld = InventoryHelpers.deepCopy(inventory);
		boolean successfulTransfer = false;
		try {
			if (!playerInventory.removeItem(InventoryHelpers.deepCopy(playerInput)).isEmpty()) {
				player.sendMessage(ChatColor.RED + "Failed to remove the item from your inventory.");
				return;
			}
			if (!inventory.addItem(InventoryHelpers.deepCopy(playerInput)).isEmpty()) {
				player.sendMessage(ChatColor.RED + "The exchange does not have enough inventory space!");
				return;
			}
			if (exchangeOutput != null) {
				if (!playerInventory.addItem(InventoryHelpers.deepCopy(exchangeOutput)).isEmpty()) {
					player.sendMessage(ChatColor.RED + "You don't have enough inventory space!");
					return;
				}
				if (!inventory.removeItem(InventoryHelpers.deepCopy(exchangeOutput)).isEmpty()) {
					player.sendMessage(ChatColor.RED + "Failed to remove the item from the shop.");
					return;
				}
			}

			IETransactionEvent event = new IETransactionEvent(player, location, playerInput, exchangeOutput);
			Bukkit.getPluginManager().callEvent(event);

			// Power buttons.
			Block block = location.getBlock();
			ItemExchange.powerBlock(player, block);

			Material type = block.getType();
			if(type == Material.CHEST || type == Material.TRAPPED_CHEST) {
				Block north = block.getRelative(BlockFace.NORTH);
				Block south = block.getRelative(BlockFace.SOUTH);
				Block east = block.getRelative(BlockFace.EAST);
				Block west = block.getRelative(BlockFace.WEST);

				if(north.getType() == type) ItemExchange.powerBlock(player, north);
				if(south.getType() == type) ItemExchange.powerBlock(player, south);
				if(east.getType() == type) ItemExchange.powerBlock(player, east);
				if(west.getType() == type) ItemExchange.powerBlock(player, west);
			}

			if (exchangeOutput != null) {
				player.sendMessage(ChatColor.GREEN + "Successful exchange!");
			} else {
				player.sendMessage(ChatColor.GREEN + "Successful donation!");
			}
			successfulTransfer = true;
		} finally {
			if (!successfulTransfer) {
				inventory.setContents(exchangeInventoryOld);
				playerInventory.setContents(playerInventoryOld);
			}
		}
	}

	public void cycleExchange(Player player) {
		int currentRuleIndex = ruleIndex.get(player);
		if (currentRuleIndex < getNumberRules() - 1) {
			ruleIndex.put(player, ruleIndex.get(player) + 1);
		}
		else {
			ruleIndex.put(player, 0);
		}
		messagePlayer(player);
	}
	
	public static void powerBlock(Player p, Block b) {
		Material type = b.getType();
		
		if(!(type == Material.CHEST || type == Material.TRAPPED_CHEST || type == Material.FURNACE)) {
			return;
		}
		
		byte data = b.getData();
		
		BlockFace face;
		
		if((data & 0x5) == 5) {
			face = BlockFace.EAST;
		}
		else if((data & 0x4) == 4) {
			face = BlockFace.WEST;
		}
		else if((data & 0x3) == 3) {
			face = BlockFace.SOUTH;
		}
		else if((data & 0x2) == 2) {
			face = BlockFace.NORTH;
		}
		else {
			return;
		}
		
		face = face.getOppositeFace();
		
		final Block b2 = b.getRelative(face, 2);
		Material type2 = b2.getType();
		
		if(type2 == Material.STONE_BUTTON || type2 == Material.WOOD_BUTTON) {
			BlockFace face2;
			byte data2 = b2.getData();
			
			if((data2 & 0x4) == 4) {
				face2 = BlockFace.NORTH;
			}
			else if((data2 & 0x3) == 3) {
				face2 = BlockFace.SOUTH;
			}
			else if((data2 & 0x2) == 2) {
				face2 = BlockFace.WEST;
			}
			else if((data2 & 0x1) == 1) {
				face2 = BlockFace.EAST;
			}
			else {
				return;
			}
			
			if(face2 == face) {
				BlockState pressed = b2.getState();
				pressed.setRawData((byte) (pressed.getRawData() | 0x8));
				pressed.update();
				
				Bukkit.getScheduler().scheduleSyncDelayedTask(ItemExchangePlugin.instance, new Runnable() {
					public void run() {
						BlockState pressed = b2.getState();
						Material type = pressed.getType();
						
						if(!(type == Material.STONE_BUTTON || type == Material.WOOD_BUTTON)) {
							return;
						}
						
						pressed.setRawData((byte) (pressed.getRawData() & ~0x8));
						pressed.update();
					}
				}, 30);
			}
		}
	}

	public void messagePlayer(Player player) {
		player.sendMessage(ChatColor.YELLOW + "(" + String.valueOf(ruleIndex.get(player) + 1) + "/" + String.valueOf(getNumberRules()) + ") exchanges present.");
		player.sendMessage(inputs.get(ruleIndex.get(player)).display(player));
		if (ruleIndex.get(player) < outputs.size()) {
			ExchangeRule output = outputs.get(ruleIndex.get(player));
			player.sendMessage(output.display(player));
			int multiples = output.checkMultiples(inventory);
			player.sendMessage(ChatColor.YELLOW + String.valueOf(multiples) + (multiples == 1 ? " exchange available." : " exchanges available."));
		}
	}
}
