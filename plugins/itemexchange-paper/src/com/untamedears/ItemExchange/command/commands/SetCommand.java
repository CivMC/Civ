package com.untamedears.ItemExchange.command.commands;

import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.untamedears.ItemExchange.ItemExchangePlugin;
import com.untamedears.ItemExchange.command.PlayerCommand;
import com.untamedears.ItemExchange.exceptions.ExchangeRuleParseException;
import com.untamedears.ItemExchange.utility.ExchangeRule;

/*
 * When holding an exchange rule block in the players hand allowes editing of the 
 * different rules.
 */
public class SetCommand extends PlayerCommand {
	public SetCommand() {
		super("Set Field");
		setDescription("Sets the field of the ExchangeRule held in hand");
		setUsage("/ieset");
		setArgumentRange(1, 3);
		setIdentifiers(new String[] { "ieset", "ies" });
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		try {
			ExchangeRule exchangeRule = ExchangeRule.parseRuleBlock(((Player) sender).getItemInHand());
			if ((args[0].equalsIgnoreCase("commonname") || args[0].equalsIgnoreCase("c")) && args.length == 2) {
				if(!ItemExchangePlugin.NAME_MATERIAL.containsKey(args[1])) {
					sender.sendMessage(ChatColor.RED + "Material not found.");
					
					return true;
				}
				
				ItemStack itemStack = ItemExchangePlugin.NAME_MATERIAL.get(args[1]);
				exchangeRule.setMaterial(itemStack.getType());
				exchangeRule.setDurability(itemStack.getDurability());
				
				sender.sendMessage(ChatColor.GREEN + "Material changed successfully.");
			}
			else if ((args[0].equalsIgnoreCase("material") || args[0].equalsIgnoreCase("m")) && args.length == 2) {
				Material m = Material.getMaterial(args[1]);
				
				if(m != null) {
					exchangeRule.setMaterial(m);
					sender.sendMessage(ChatColor.GREEN + "Material changed successfully.");
				}
				else {
					sender.sendMessage(ChatColor.RED + "Material not found.");
					
					return true;
				}
			}
			else if ((args[0].equalsIgnoreCase("amount") || args[0].equalsIgnoreCase("a")) && args.length == 2) {
				try {
					int amount = Integer.valueOf(args[1]);
					
					if(amount < 1) {
						sender.sendMessage(ChatColor.RED + "Invalid amount.");
						
						return true;
					}
					else {
						exchangeRule.setAmount(Integer.valueOf(args[1]));
						sender.sendMessage(ChatColor.GREEN + "Amount changed successfully.");
					}
				}
				catch(NumberFormatException e) {
					sender.sendMessage(ChatColor.RED + "Invalid number.");
					
					return true;
				}
			}
			else if ((args[0].equalsIgnoreCase("durability") || args[0].equalsIgnoreCase("d")) && args.length == 2) {
				try {
					short durability = Short.valueOf(args[1]);

					exchangeRule.setDurability(durability);
					
					sender.sendMessage(ChatColor.GREEN + "Durability changed successfully.");
				}
				catch(NumberFormatException e) {
					sender.sendMessage(ChatColor.RED + "Invalid durability.");
					
					return true;
				}
			}
			else if ((args[0].equalsIgnoreCase("enchantment") || args[0].equalsIgnoreCase("e")) && args.length == 3) {
				String abbrv = args[1].substring(0, args[1].length() - 1);
				
				if(!ItemExchangePlugin.ABBRV_ENCHANTMENT.containsKey(abbrv)) {
					StringBuilder enchantments = new StringBuilder();
					
					Iterator<String> iterator = ItemExchangePlugin.ABBRV_ENCHANTMENT.keySet().iterator();
					
					while(iterator.hasNext()) {
						enchantments.append(iterator.next());
						
						if(iterator.hasNext()) {
							enchantments.append(", ");
						}
					}
					
					sender.sendMessage(ChatColor.RED + "Invalid enchantment specified.");
					sender.sendMessage(ChatColor.YELLOW + "Valid enchantments: " + enchantments.toString());
					
					return true;
				}
				
				Enchantment enchantment = Enchantment.getByName(ItemExchangePlugin.ABBRV_ENCHANTMENT.get(abbrv));
				int level = Character.getNumericValue(args[1].charAt(args[1].length() - 1));
				
				if(level < 1) {
					sender.sendMessage(ChatColor.RED + "Enchantment level must be at least 1.");
					
					return true;
				}
				
				if (args[2].equalsIgnoreCase("required") || args[2].equalsIgnoreCase("r")) {
					exchangeRule.requireEnchantment(enchantment, level);
					
					sender.sendMessage(ChatColor.GREEN + "Successfully added required enchantment.");
				}
				else if (args[2].equalsIgnoreCase("excluded") || args[2].equalsIgnoreCase("e")) {
					exchangeRule.excludeEnchantment(enchantment, level);
					
					sender.sendMessage(ChatColor.GREEN + "Successfully removed required enchantment.");
				}
			}
			else if ((args[0].equalsIgnoreCase("displayname") || args[0].equalsIgnoreCase("n")) && args.length == 2) {
				exchangeRule.setDisplayName(args[1]);
				
				sender.sendMessage(ChatColor.GREEN + "Successfully changed display name.");
			}
			else if ((args[0].equalsIgnoreCase("lore") || args[0].equalsIgnoreCase("l")) && args.length == 2) {
				exchangeRule.setLore(args[1].split(";"));
				
				sender.sendMessage(ChatColor.GREEN + "Successfully changed lore.");
			}
			else if (args[0].equalsIgnoreCase("switchio") || args[0].equalsIgnoreCase("s")) {
				exchangeRule.switchIO();
				
				sender.sendMessage(ChatColor.GREEN + "Successfully switched input/output.");
			}
			else {
				throw new IllegalArgumentException(ChatColor.RED + "Incorrect Field: " + args[0]);
			}
			((Player) sender).setItemInHand(exchangeRule.toItemStack());
		}
		catch (ExchangeRuleParseException e) {
			sender.sendMessage(ChatColor.RED + "You are not holding an exchange rule.");
		}
		catch (IllegalArgumentException e) {
			sender.sendMessage(e.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

}
