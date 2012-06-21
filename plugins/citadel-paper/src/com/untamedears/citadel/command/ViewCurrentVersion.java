package com.untamedears.citadel.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.PluginConsumer;

import static com.untamedears.citadel.Utility.sendMessage;

/**
 * 
 * @author JonnyD
 * 
 */
public class ViewCurrentVersion extends PluginConsumer implements CommandExecutor {

	public ViewCurrentVersion(Citadel plugin){
		super(plugin);
	}

	public boolean onCommand(CommandSender sender, Command command, String s,
			String[] args) {
		String version = plugin.getDescription().getVersion();
		sendMessage(sender, ChatColor.WHITE, "Current version is: " + version);
		return true;
	}
	
	
}
