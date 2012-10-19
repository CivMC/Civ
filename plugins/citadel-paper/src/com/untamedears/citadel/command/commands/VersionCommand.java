package com.untamedears.citadel.command.commands;

import org.bukkit.command.CommandSender;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.command.PlayerCommand;

/**
 * User: JonnyD
 * Date: 7/18/12
 * Time: 11:57 PM
 */
public class VersionCommand extends PlayerCommand {

	public VersionCommand() {
		super("Current Version");
		setDescription("Shows current version of Citadel");
		setUsage("/ctversion");
		setIdentifiers(new String[] {"ctversion", "ctv"});
	}

	public boolean execute(CommandSender sender, String[] args) {
		String version = Citadel.getPlugin().getDescription().getVersion();
		sender.sendMessage(new StringBuilder().append("§cCitadel Version:§e " ).append(version).toString());
		return true;
	}

}
