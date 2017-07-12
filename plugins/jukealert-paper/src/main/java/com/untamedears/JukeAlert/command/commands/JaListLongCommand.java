package com.untamedears.JukeAlert.command.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.command.commands.JaListCommand;
import com.untamedears.JukeAlert.tasks.GetSnitchListPlayerTask;

public class JaListLongCommand extends PlayerCommand {

	public JaListLongCommand() {

		super("jalistlong");
		setDescription("Displays Juke List information with full-length Snitch and group names");
		setUsage("/jalistlong <page number> [groups=<group1>,<group2>,...]");
		setArguments(0, 2);
		setIdentifier("jalistlong");
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {

		return JukeAlert.getInstance().getJaListCommand().executeReal(sender, args, false);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {

		return null;
	}
}
