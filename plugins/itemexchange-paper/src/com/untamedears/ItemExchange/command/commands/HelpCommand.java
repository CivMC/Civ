/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.untamedears.ItemExchange.command.commands;

import com.untamedears.ItemExchange.command.PlayerCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * Provide helpful information pertain to commands of the mod
 */
public class HelpCommand extends PlayerCommand {
	public HelpCommand() {
		super("Item Exchange Help");
		setDescription("Item Exchange Help");
		setUsage("/iehelp");
		setArgumentRange(0, 1);
		setIdentifiers(new String[] { "iehelp", "ieh" });
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (args.length == 1) {
			sender.sendMessage(ChatColor.YELLOW + "Item Exchange Commands:\n /iecreate (or /iec) [input or output] [common name or ID:durability] [amount]\n/ieset (or /ies) <field> [value] [modifier]\nType /iehelp [command] for more information on a command");
		}
		else if (args.length == 2) {
			if (args[1].charAt(0) == '/') {
				args[1] = args[1].substring(1);
			}
			if (args[1].equalsIgnoreCase("iecreate") || args[1].equalsIgnoreCase("iec")) {
				sender.sendMessage("/iecreate (or /iec) [input or output] [common name or ID:durability] [amount]");
				sender.sendMessage("/iecreate: Creates an ItemExchange when facing an inventory block with 2 types of items");
			}
			else if (args[1].equalsIgnoreCase("ieset") || args[1].equalsIgnoreCase("ies")) {

			}

		}
		return true;
	}
}
