package com.untamedears.JukeAlert.command.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.group.Group;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.tasks.GetSnitchInfoPlayerTask;

public class GroupCommand extends PlayerCommand {

	public GroupCommand() {

		super("Group");
		setDescription("Displays information from a group");
		setUsage("/jagroup <group> <page>");
		setArguments(1, 2);
		setIdentifier("jagroup");
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {

		if (sender instanceof Player) {
			Player player = (Player) sender;
			int offset = 1;
			if (args.length > 1) {
				try {
					offset = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					offset = 1;
				}
			}
			if (offset < 1) {
				offset = 1;
			}
			Group group = GroupManager.getGroup(args[0]);
			if (!sender.hasPermission("jukealert.admin.jagroup")) {
				if (group == null) {
					sender.sendMessage(ChatColor.RED + "That group doesn't exist!");
					return true;
				}
				UUID accountId = player.getUniqueId();
				if (!group.isMember(accountId)) {
					sender.sendMessage(ChatColor.RED + "You are not part of that group!");
					return true;
				}
			}
			sendLog(sender, group, offset);
		} else {
			sender.sendMessage(ChatColor.RED + " You do not own any snitches nearby!");
		}
		return true;
	}

	private void sendLog(CommandSender sender, Group group, int offset) {

		Player player = (Player) sender;
		GetSnitchInfoPlayerTask task = new GetSnitchInfoPlayerTask(JukeAlert.getInstance(), group, offset, player);
		Bukkit.getScheduler().runTaskAsynchronously(JukeAlert.getInstance(), task);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {

		return null;
	}
}
