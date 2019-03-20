package com.github.maxopoly.finale.command;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.github.maxopoly.finale.Finale;

import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;

@CivCommand(id = "finalereload")
public class ReloadFinaleCommand extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender arg0, String[] arg1) {
		Finale.getPlugin().reload();
		arg0.sendMessage(ChatColor.GREEN + "Reloaded Finale");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] arg1) {
		return new LinkedList<>();
	}

}
