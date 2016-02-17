package com.github.devotedmc.hiddenore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.github.devotedmc.hiddenore.Config;
import com.github.devotedmc.hiddenore.HiddenOre;

public class CommandHandler implements CommandExecutor {

	final HiddenOre plugin;

	public CommandHandler(HiddenOre instance) {
		plugin = instance;

		plugin.getCommand("hiddenore").setExecutor(this);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (cmd.getName().equalsIgnoreCase("hiddenore")) {
			if (sender.hasPermission(plugin.getCommand("hiddenore").getPermission())) {
				if (args.length >= 2) {
					if ("debug".equals(args[0])) {
						Config.isDebug = Boolean.parseBoolean(args[1]);
						sender.sendMessage("HiddenOre debug mode now " + (Config.isDebug?"on":"off"));
						return true;
					}
				} else if (args.length >= 1) {
					if ("save".equals(args[0])) {
						HiddenOre.getTracking().liveSave();
						sender.sendMessage("HiddenOre tracking forced live save scheduled");
						return true;
					}
				} else {
					Bukkit.getPluginManager().disablePlugin(plugin);
					Bukkit.getPluginManager().enablePlugin(plugin);
					sender.sendMessage("HiddenOre reloaded");
					return true;
				}
			}
		}
		
		return false;
	}
}
