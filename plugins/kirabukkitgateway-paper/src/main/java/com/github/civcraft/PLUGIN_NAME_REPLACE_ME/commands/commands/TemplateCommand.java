package com.github.civcraft.PLUGIN_NAME_REPLACE_ME.commands.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;

/**
 * Template for new commands
 *
 */
public class TemplateCommand extends PlayerCommand {

    public TemplateCommand(String name) {
	super(name);
	// internal identifier used, this has to be unique for the plugin
	setIdentifier("exampleTemplate0815");
	// description displayed when the player runs the command wrong
	setDescription("This command doesnt even exist");
	// Correct usage of this command with [optional] and <mandatory>
	// parameters
	setUsage("/examplecommand <parameter1> [parameter2]");
	// minimum and maximum amount of arguments this command takes. If the
	// entered parameter amount is not within the range, the command will
	// error to the player and show the correct usage
	setArguments(0, 10);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
	// code to run if the command is executed. Note that sender may be
	// console, so casting it to player right away is not safe
	// returning false will display the command description and usage to the
	// player
	// returning true will do nothing
	return true;
    }

    @Override
    public List<String> tabComplete(CommandSender arg0, String[] arg1) {
	// Sets custom auto complete functionality for this command. The strings
	// in the list returned will be suggested to the player in alphabetical
	// order, not the order of the list. Additionally returning null will
	// leave the autocompletion to minecrafts default behavior, which
	// attempts to complete the last word as the name of a player currently
	// online on the server
	return null;
    }

}
