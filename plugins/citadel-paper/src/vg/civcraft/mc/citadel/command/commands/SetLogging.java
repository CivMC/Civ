package vg.civcraft.mc.citadel.command.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelConfigManager;
import vg.civcraft.mc.citadel.Utility;
import vg.civcraft.mc.civmodcore.command.PlayerCommand;

public class SetLogging extends PlayerCommand {

	public SetLogging(String name) {
		super(name);
		setIdentifier("ctsl");
		setDescription("Allows admins to toggle special logging types live or show current status");
		setUsage("/ctsl [internal|playercommands|breaks|hostilebreaks|damage|reinforcements [on|off]]");
		setArguments(0, 2);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (args.length > 0 && !cmds.contains(args[0])) {
			return false;
		}

		if (args.length >= 1 && !flgs.contains(args[1])) {
			return false;
		}

		if (!(sender instanceof ConsoleCommandSender) && !sender.isOp() && !sender.hasPermission("citadel.admin")) {
			// This should never actually happen thanks to the plugin.yml, but
			// we just want to be sure
			Utility.sendAndLog(sender, ChatColor.RED, "Nice try");
			return true;
		}

		// validated, so lets just do it.
		if (args.length == 0) {
			StringBuilder sb = new StringBuilder("Current deep logging set to: \n");
			sb.append("   Internal: ").append(CitadelConfigManager.shouldLogInternal()).append("\n");
			sb.append("   Player Command Responses: ").append(CitadelConfigManager.shouldLogPlayerCommands()).append("\n");
			sb.append("   Friendly/CTB Breaks: ").append(CitadelConfigManager.shouldLogFriendlyBreaks()).append("\n");
			sb.append("   Hostile Breaks: ").append(CitadelConfigManager.shouldLogHostileBreaks()).append("\n");
			sb.append("   Damage: ").append(CitadelConfigManager.shouldLogDamage()).append("\n");
			sb.append("   Reinforcements: ").append(CitadelConfigManager.shouldLogReinforcement()).append("\n");

			Utility.sendAndLog(sender, ChatColor.GREEN, sb.toString());
		}

		String flag = null;
		boolean newval = false;
		if (args.length >= 1) {
			if ("internal".equalsIgnoreCase(args[0])) {
				flag = "internal_logging";
				newval = CitadelConfigManager.shouldLogInternal();
			} else if ("playercommands".equalsIgnoreCase(args[0])) {
				flag = "command_logging";
				newval = CitadelConfigManager.shouldLogPlayerCommands();
			} else if ("breaks".equalsIgnoreCase(args[0])) {
				flag = "break_logging";
				newval = CitadelConfigManager.shouldLogFriendlyBreaks();
			} else if ("hostilebreaks".equalsIgnoreCase(args[0])) {
				flag = "hostile_logging";
				newval = CitadelConfigManager.shouldLogHostileBreaks();
			} else if ("damage".equalsIgnoreCase(args[0])) {
				flag = "damage_logging";
				newval = CitadelConfigManager.shouldLogDamage();
			} else if ("reinforcements".equalsIgnoreCase(args[0])) {
				flag = "reinf_logging";
				newval = CitadelConfigManager.shouldLogReinforcement();
			}
		}

		if (args.length < 2) {
			newval = !newval; // invert current.
		} else {
			// If can't figure it out, leave current.
			newval = "on".equalsIgnoreCase(args[1])? true : "off".equalsIgnoreCase(args[1]) ? false : newval;
		}

		if (flag != null) {
			Citadel.getInstance().getConfig().set(flag, newval);
			Utility.sendAndLog(sender, ChatColor.GREEN, "Flag " + flag + " is " + (newval ? "on" : "off"));
			return true;
		} else {
			Utility.sendAndLog(sender, ChatColor.RED, "Unknown setting!");
			return false;
		}
	}

	private static List<String> cmds = Arrays.asList("internal","playercommands", "hostilebreaks", "breaks", "damage", "reinforcements");
	private static List<String> flgs = Arrays.asList("on", "off");
	@Override
	public List<String> tabComplete(CommandSender arg0, String[] arg1) {
		if (arg1.length == 0) {
			return cmds;
		} else if (arg1.length == 1) {
			ArrayList<String> lst = new ArrayList<String>();
			for (String cmd : cmds) {
				if (cmd.toLowerCase().equals(arg1[0])) {
					return flgs;
				} else if (cmd.contains(arg1[0].toLowerCase())) {
					lst.add(cmd);
				}
			}
			return lst;
		} else if (arg1.length == 2) {
			ArrayList<String> lst = new ArrayList<String>();
			for (String flg : flgs) {
				if (flg.toLowerCase().equals(arg1[1])) {
					return flgs;
				} else if (flg.contains(arg1[1].toLowerCase())) {
					lst.add(flg);
				}
			}
			return lst;
		}
		return null;
	}

}
