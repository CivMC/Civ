package com.untamedears.JukeAlert.command.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.tasks.GetSnitchListPlayerTask;

public class JaListCommand extends PlayerCommand {

	public JaListCommand() {

		super("jalist");
		setDescription("Displays Juke List Information");
		setUsage("/jalist <page number> [groups=<group1>,<group2>,...]");
		setArguments(0, 2);
		setIdentifier("jalist");
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {

		// So /jalistlong can call this as well
		return executeReal(sender, args, true);
	}

	public boolean executeReal(CommandSender sender, String[] args, boolean truncateNames) {

		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + " You do not have access to snitches!");
			return false;
		}

		int offset = 1;
		if (args.length == 0) {
			sendSnitchList(sender, offset, null, truncateNames);
			return true;
		}

		// Reassemble any arguments that are enclosed in quotes and were split
		List<String> fixedArgs = new ArrayList<String>();
		Scanner scanner = new Scanner(String.join(" ", args));
		scanner.useDelimiter("\\s(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
		while (scanner.hasNext()) {
			fixedArgs.add(scanner.next());
		}
		scanner.close();

		List<String> groupNames = null;

		// Parse each argument
		for (String arg : fixedArgs) {
			arg = arg.toLowerCase().trim();
			if (arg.startsWith("groups=")) {
				if (arg.length() > 7) {
					String groupNamesRaw = arg.substring(7);
					// Strip quotes
					groupNamesRaw = groupNamesRaw.replaceAll("^[\"']|[\"']$", "");
					groupNames = Arrays.asList(groupNamesRaw.split(","));
					continue;
				}
			} else {
				try {
					offset = Integer.parseInt(arg);
				} catch (NumberFormatException e) {
					offset = 1;
				}
				continue;
			}

			sender.sendMessage(ChatColor.RED + "Unrecognized argument: '" + arg + "'");
			return false;
		}
		sendSnitchList(sender, offset, groupNames, truncateNames);
		return true;
	}

	private void sendSnitchList(CommandSender sender, int offset, List<String> groupNames, boolean truncateNames) {

		Player player = (Player) sender;
		GetSnitchListPlayerTask task = new GetSnitchListPlayerTask(JukeAlert.getInstance(), offset, player, groupNames,
			truncateNames);
		Bukkit.getScheduler().runTaskAsynchronously(JukeAlert.getInstance(), task);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {

		return null;
	}
}
