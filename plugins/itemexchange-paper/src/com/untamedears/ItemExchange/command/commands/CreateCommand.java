package com.untamedears.ItemExchange.command.commands;

import static com.untamedears.citadel.Utility.getReinforcement;
import static com.untamedears.citadel.Utility.isReinforced;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.untamedears.ItemExchange.ItemExchangePlugin;
import com.untamedears.ItemExchange.command.PlayerCommand;
import com.untamedears.ItemExchange.exceptions.ExchangeRuleCreateException;
import com.untamedears.ItemExchange.exceptions.ExchangeRuleParseException;
import com.untamedears.ItemExchange.utility.ExchangeRule;
import com.untamedears.ItemExchange.utility.ExchangeRule.RuleType;
import com.untamedears.ItemExchange.utility.ItemExchange;
import com.untamedears.citadel.entity.PlayerReinforcement;

/*
 * General command for creating either an entire ItemExchange or 
 * creating an exchange rule, given the context of the player when
 * the command is issued.
 */
public class CreateCommand extends PlayerCommand {
	public CreateCommand() {
		super("Create Exchange");
		setDescription("Automatically creates an exchange inside the chest the player is looking at");
		setUsage("/iecreate");
		setArgumentRange(0, 3);
		setIdentifiers(new String[] { "iecreate", "iec" });
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		Block block = player.getLastTwoTargetBlocks(null, 20).get(1).getLocation().getBlock();
		//If no input or ouptut is specified player attempt to set up ItemExchange at the block the player is looking at
		//The player must have citadel access to the inventory block
		if (args.length == 0) {
			if (ItemExchangePlugin.ACCEPTABLE_BLOCKS.contains(block.getState().getType())) {
				if ((!ItemExchangePlugin.CITADEL_ENABLED || ItemExchangePlugin.CITADEL_ENABLED && !isReinforced(block)) || (((PlayerReinforcement) getReinforcement(block)).isAccessible(player))) {
					player.sendMessage(ItemExchange.createExchange(block.getLocation(), player));
				}
				else {
					player.sendMessage(ChatColor.RED + "You do not have access to that block.");
				}
			}
			else {
				player.sendMessage(ChatColor.RED + "Block in view is not suitable for an Item Exchange.");
			}
		}
		//Create a RuleBlock in the players inventory
		else {
			//Player must have space in their inventory for the RuleBlock
			if (player.getInventory().firstEmpty() != -1) {
				//If only an input/output is specified create the RuleBlock based on the item held in the players hand
				if (args.length == 1) {
					//Assign a ruleType
					RuleType ruleType = null;
					if (args[0].equalsIgnoreCase("input")) {
						ruleType = ExchangeRule.RuleType.INPUT;
					}
					else if (args[0].equalsIgnoreCase("output")) {
						ruleType = ExchangeRule.RuleType.OUTPUT;
					}
					if (ruleType != null) {
						ItemStack inHand = player.getItemInHand();
						
						if(inHand == null || inHand.getType() == Material.AIR) {
							player.sendMessage(ChatColor.RED + "You are not holding anything in your hand!");
							
							return true;
						}
						
						if(ExchangeRule.isRuleBlock(inHand)) {
							player.sendMessage(ChatColor.RED + "You cannot exchange rule blocks!");
							
							return true;
						}
						
						//Creates the ExchangeRule, converts it to an ItemStack and places it in the player's inventory
						try {
							player.getInventory().addItem(ExchangeRule.parseItemStack(inHand, ruleType).toItemStack());
						}
						catch (IllegalArgumentException | ExchangeRuleCreateException e) {
							player.sendMessage(ChatColor.RED + e.getMessage());
							
							return true;
						}
						player.sendMessage(ChatColor.GREEN + "Created Rule Block!");
					}
					else {
						player.sendMessage(ChatColor.RED + "Please specify and input or output.");

					}
				}
				//If additional arguments are specified create an exchange rule based upon the additional arguments and place it in the player's inventory
				else if (args.length >= 2) {
					try {
						//Attemptes to create the ExchangeRule, converts it to an ItemStack and places it in the player's inventory
						player.getInventory().addItem(ExchangeRule.parseCreateCommand(args).toItemStack());
						player.sendMessage(ChatColor.GREEN + "Created Rule Block!");
					}
					catch (ExchangeRuleParseException e) {
						player.sendMessage(ChatColor.RED + "Incorrect entry format.");
					}
				}
			}
			else {
				player.sendMessage(ChatColor.RED + "Player inventory is full!");
			}
		}
		return true;
	}

}
