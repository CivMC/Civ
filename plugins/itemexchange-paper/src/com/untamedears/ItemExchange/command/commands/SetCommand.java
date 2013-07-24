package com.untamedears.ItemExchange.command.commands;

import com.untamedears.ItemExchange.ItemExchangePlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.ItemExchange.command.PlayerCommand;
import com.untamedears.ItemExchange.utility.ExchangeRule;
import com.untamedears.ItemExchange.exceptions.ExchangeRuleParseException;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import java.lang.IllegalArgumentException;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;

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
			if ((args[0].equalsIgnoreCase("commonname") || args[0].equalsIgnoreCase("c"))) {
				try {
					ItemStack itemStack = ItemExchangePlugin.NAME_MATERIAL.get(args[1]);
					exchangeRule.setMaterial(itemStack.getType());
					exchangeRule.setDurability(itemStack.getDurability());
				}
				catch (Exception e) {
					throw new IllegalArgumentException(ChatColor.RED+"Invalid common name (Normally displayed name), database may need fixing :(.");
				}
			}
			else if ((args[0].equalsIgnoreCase("material") || args[0].equalsIgnoreCase("m"))) {
				try {
					exchangeRule.setMaterial(Material.getMaterial(args[1]));
				}
				catch (Exception e) {
					try {
						exchangeRule.setMaterial(Material.getMaterial(Integer.parseInt(args[1])));
					}
					catch (Exception e2) {
						throw new IllegalArgumentException(ChatColor.RED+"Invalid Material value, either use a minecraft material # or Bukkit name.");
					}
				}
			}
			else if ((args[0].equalsIgnoreCase("amount") || args[0].equalsIgnoreCase("a"))) {
				try {
					exchangeRule.setAmount(Integer.valueOf(args[1]));
				}
				catch (Exception e) {
					throw new IllegalArgumentException(ChatColor.RED+"Invalid amount value.");
				}
			}
			else if ((args[0].equalsIgnoreCase("durability") || args[0].equalsIgnoreCase("d"))) {
				try {
					exchangeRule.setDurability(Short.valueOf(args[1]));
				}
				catch (Exception e) {
					throw new IllegalArgumentException(ChatColor.RED+"Invalid durability value.");
				}
			}
			else if ((args[0].equalsIgnoreCase("enchantment") || args[0].equalsIgnoreCase("e")) && args.length >= 2) {
				Enchantment enchantment = null;
				Integer level = null;
				//Check if its an abbreviated enchantment
				if(ItemExchangePlugin.ABBRV_ENCHANTMENT.containsKey(Pattern.compile("\\w+").matcher(args[1]).group())) {
					enchantment = Enchantment.getByName(ItemExchangePlugin.ABBRV_ENCHANTMENT.get(args[1].substring(1, args[1].length() - 1)));
					try {
						level = Integer.parseInt(Pattern.compile("\\d+").matcher(args[1]).group());
					}
					catch (Exception e) {
						throw new IllegalArgumentException(ChatColor.RED+"No level included, use format \"Abbr#\".");
					}
				}
				//Otherwise cycle through remaining args and try to match an enchantment name
				else {
					for(int i=1;i<args.length;i++) {
						if(ItemExchangePlugin.NAME_ENCHANTMENT.containsKey(StringUtils.join(args, " ", i, args.length))) {
							enchantment = Enchantment.getByName(ItemExchangePlugin.NAME_ENCHANTMENT.get(StringUtils.join(args, " ", i, args.length)));
							try {
								level = Integer.parseInt(Pattern.compile("\\d+").matcher(args[i+1]).group());
							}
							catch (Exception e) {
								throw new IllegalArgumentException(ChatColor.RED+"No level included, use format \"Displayed Enchantment Name #\".");
							}
						}
					}
				}
				if(enchantment==null) {
					throw new IllegalArgumentException(ChatColor.RED+"Invalid enchantment entry, use format  \"Abbr#\" or \"Displayed Enchantment Name #\".");
				}
				//Search args for a value representing required or excluded, if neither is found default to included
				boolean enchantmentSet=false;
				for(String arg:args){
					if (arg.equalsIgnoreCase("required") || arg.equalsIgnoreCase("r")) {
						exchangeRule.requireEnchantment(enchantment, level);
						enchantmentSet = true;
						break;
					}
					else if (arg.equalsIgnoreCase("excluded") || arg.equalsIgnoreCase("e")) {
						exchangeRule.excludeEnchantment(enchantment, level);
						enchantmentSet = true;
						break;
					}
				}
				if(!enchantmentSet) {
					exchangeRule.requireEnchantment(enchantment, level);
				}
			}
			else if ((args[0].equalsIgnoreCase("displayname") || args[0].equalsIgnoreCase("n"))) {
				try {
					exchangeRule.setDisplayName(StringUtils.join(args, " ", 1, args.length));
				}
				catch (Exception e) {
					throw new IllegalArgumentException(ChatColor.RED+"Include a display name.");
				}
			}
			else if ((args[0].equalsIgnoreCase("lore") || args[0].equalsIgnoreCase("l"))) {
				try {
					exchangeRule.setLore(StringUtils.join(args, " ", 1, args.length).split(";"));
				}
				catch (Exception e) {
					throw new IllegalArgumentException(ChatColor.RED+"Include a lore string.");
				}
			}
			else if (args[0].equalsIgnoreCase("switchio") || args[0].equalsIgnoreCase("s")) {
				exchangeRule.switchIO();
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
