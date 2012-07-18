package com.untamedears.citadel.command.commands;

import org.bukkit.command.CommandSender;

import com.untamedears.citadel.command.PlayerCommand;

/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/21/12
 * Time: 4:09 PM
 */
public class HelpCommand extends PlayerCommand {
    public HelpCommand() {
		super("Help");
	}

	public boolean execute(CommandSender sender, String[] args) {
        return true;
	}
}
