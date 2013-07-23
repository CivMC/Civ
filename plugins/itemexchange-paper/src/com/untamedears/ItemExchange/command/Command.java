package com.untamedears.ItemExchange.command;

import org.bukkit.command.CommandSender;

/**
 * User: JonnyD
 * Date: 07/18/12
 * Time: 11:57 PM
 */
public abstract interface Command {

	public abstract boolean execute(CommandSender sender, String[] args);

	public abstract int getMinArguments();

	public abstract int getMaxArguments();

	public abstract String getName();

	public abstract String getDescription();

	public abstract String getUsage();

	public abstract String[] getIdentifiers();

	public abstract boolean isIdentifier(CommandSender sender, String input);

	public abstract boolean isInProgress(CommandSender sender);

}
