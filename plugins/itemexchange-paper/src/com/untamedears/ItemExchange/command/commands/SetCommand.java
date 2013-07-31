package com.untamedears.ItemExchange.command.commands;

import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
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
import com.untamedears.ItemExchange.utility.ExchangeRule.RuleType;
import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.entity.Faction;

/*
 * When holding an exchange rule block in the players hand allows editing of the 
 * different rules.
 */
public class SetCommand extends PlayerCommand {
	public SetCommand() {
		super("Set Field");
		setDescription("Sets the field of the ExchangeRule held in hand");
		setUsage("/ieset");
		setArgumentRange(1, 200);
		setIdentifiers(new String[] { "ieset", "ies" });
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		try {
			ExchangeRule exchangeRule = ExchangeRule.parseRuleBlock(((Player) sender).getItemInHand());
			int itemAmount = ((Player) sender).getItemInHand().getAmount();
			if ((args[0].equalsIgnoreCase("commonname") || args[0].equalsIgnoreCase("c"))) {
				if(args.length == 2) {
					if(!ItemExchangePlugin.NAME_MATERIAL.containsKey(args[1])) {
						sender.sendMessage(ChatColor.RED + "Material not found.");

						return true;
					}

					ItemStack itemStack = ItemExchangePlugin.NAME_MATERIAL.get(args[1]);
					exchangeRule.setMaterial(itemStack.getType());
					exchangeRule.setDurability(itemStack.getDurability());

					sender.sendMessage(ChatColor.GREEN + "Material changed successfully.");
				}
				else {
					sender.sendMessage(ChatColor.RED + "Usage: /ies commonname <name>");
					
					return true;
				}
			}
			else if ((args[0].equalsIgnoreCase("material") || args[0].equalsIgnoreCase("m"))) {
				if(args.length == 2) {
					Material m = Material.getMaterial(args[1]);

					if(m == null) {
						try {
							m = Material.getMaterial(Integer.parseInt(args[1]));
						}
						catch(NumberFormatException e) {}
					}

					if(m != null) {
						exchangeRule.setMaterial(m);
						sender.sendMessage(ChatColor.GREEN + "Material changed successfully.");
					}
					else {
						sender.sendMessage(ChatColor.RED + "Material not found.");

						return true;
					}
				}
				else {
					sender.sendMessage(ChatColor.RED + "Usage: /ies material <name|id>");
					
					return true;
				}
			}
			else if ((args[0].equalsIgnoreCase("amount") || args[0].equalsIgnoreCase("a"))) {
				if(args.length == 2) {
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
				else {
					sender.sendMessage(ChatColor.RED + "Usage: /ies amount <name>");
					
					return true;
				}
			}
			else if ((args[0].equalsIgnoreCase("durability") || args[0].equalsIgnoreCase("d"))) {
				if(args.length == 2) {
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
				else {
					sender.sendMessage(ChatColor.RED + "Usage: /ies durability <amount>");
					
					return true;
				}
			}
			else if (args[0].equalsIgnoreCase("allowenchantments") || args[0].equalsIgnoreCase("allowenchants")) {
				exchangeRule.setUnlistedEnchantmentsAllowed(true);

				sender.sendMessage(ChatColor.GREEN + "Unlisted enchantments are now allowed.");
			}
			else if (args[0].equalsIgnoreCase("denyenchantments") || args[0].equalsIgnoreCase("denyenchants")) {
				exchangeRule.setUnlistedEnchantmentsAllowed(false);

				sender.sendMessage(ChatColor.GREEN + "Unlisted enchantments are now denied.");
			}
			else if ((args[0].equalsIgnoreCase("enchantment") || args[0].equalsIgnoreCase("e"))) {
				if(args[1].length() != 2) {
					sender.sendMessage(ChatColor.RED + "Usage: /ieset enchantment <+/?/-><enchantment abbrv.>[level]");

					return true;
				}

				char first = args[1].charAt(0);
				boolean requiresLevel = first == '+';

				if(!requiresLevel)
					args[1] = args[1].replaceAll("[0-9]", "");

				String abbrv = args[1].substring(1, requiresLevel ? args[1].length() - 1 : args[1].length());

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
				
				int level;
				
				try {
					level = requiresLevel ? Integer.parseInt(String.valueOf((args[1].charAt(args[1].length() - 1)))) : 1;
				}
				catch (NumberFormatException e) {
					sender.sendMessage(ChatColor.RED + "This command requires a level.");
					
					return true;
				}

				if(level < 1) {
					sender.sendMessage(ChatColor.RED + "Enchantment level must be at least 1.");

					return true;
				}

				if (first == '+') {
					exchangeRule.requireEnchantment(enchantment, level);
					exchangeRule.removeExcludedEnchantment(enchantment);

					sender.sendMessage(ChatColor.GREEN + "Successfully added required enchantment.");
				}
				else if (first == '-') {
					exchangeRule.excludeEnchantment(enchantment);
					exchangeRule.removeRequiredEnchantment(enchantment);

					sender.sendMessage(ChatColor.GREEN + "Successfully added excluded enchantment.");
				}
				else if (first == '?') {
					exchangeRule.removeRequiredEnchantment(enchantment);
					exchangeRule.removeExcludedEnchantment(enchantment);

					sender.sendMessage(ChatColor.GREEN + "Successfully removed rules relating to enchantment.");
				}
				else {
					sender.sendMessage(ChatColor.RED + "Usage: /ieset enchantment <+/?/-><enchantment abbrv.>[level]");
					
					return true;
				}
			}
			else if ((args[0].equalsIgnoreCase("displayname") || args[0].equalsIgnoreCase("n"))) {
				if(args.length == 2) {
					exchangeRule.setDisplayName(StringUtils.join(args, ' ', 1, args.length));

					sender.sendMessage(ChatColor.GREEN + "Successfully changed display name.");
				}
				else if(args.length == 1) {
					exchangeRule.setDisplayName("");

					sender.sendMessage(ChatColor.GREEN + "Successfully removed display name.");
				}
				else {
					sender.sendMessage(ChatColor.RED + "Usage: /ies displayname [name]");
					
					return true;
				}
			}
			else if ((args[0].equalsIgnoreCase("lore") || args[0].equalsIgnoreCase("l"))) {
				if(args.length == 2) {
					exchangeRule.setLore(args[1].split(";"));

					sender.sendMessage(ChatColor.GREEN + "Successfully changed lore.");
				}
				else if(args.length == 1) {
					exchangeRule.setLore(new String[0]);

					sender.sendMessage(ChatColor.GREEN + "Successfully removed lore.");
				}
				else {
					sender.sendMessage(ChatColor.RED + "Usage: /ies lore [line 1[;line 2[; ...]]]");
					
					return true;
				}
			}
			else if (args[0].equalsIgnoreCase("group")) {
				if(exchangeRule.getType() != RuleType.INPUT) {
					sender.sendMessage(ChatColor.RED + "This command can only be run on input blocks!");

					return true;
				}

				if(args.length == 2) {
					Faction group = Citadel.getGroupManager().getGroup(args[1]);

					if(group != null) {
						exchangeRule.setCitadelGroup(group);
						sender.sendMessage(ChatColor.GREEN + "Successfully changed Citadel group.");
					}
					else {
						sender.sendMessage(ChatColor.RED + "The specified Citadel group does not exist!");
					}
				}
				else if(args.length == 1) {
					exchangeRule.setCitadelGroup(null);
					sender.sendMessage(ChatColor.GREEN + "Successfully removed Citadel group.");
				}
				else {
					sender.sendMessage(ChatColor.RED + "Usage: /ies group [citadel group]");
					
					return true;
				}
			}
			else if (args[0].equalsIgnoreCase("switchio") || args[0].equalsIgnoreCase("s")) {
				exchangeRule.switchIO();

				sender.sendMessage(ChatColor.GREEN + "Successfully switched input/output.");
			}
			else {
				throw new IllegalArgumentException(ChatColor.RED + "Incorrect Field: " + args[0]);
			}
			
			ItemStack itemstack = exchangeRule.toItemStack();
			itemstack.setAmount(itemAmount);
			((Player) sender).setItemInHand(itemstack);
		}
		catch (ExchangeRuleParseException e) {
			sender.sendMessage(ChatColor.RED + "You are not holding an exchange rule.");
		}
		catch (NumberFormatException e) {
			sender.sendMessage(ChatColor.RED + "Error when parsing number.");
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
