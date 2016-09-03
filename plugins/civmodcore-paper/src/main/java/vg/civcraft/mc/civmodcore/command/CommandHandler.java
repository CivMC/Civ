package vg.civcraft.mc.civmodcore.command;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.mercury.MercuryAPI;

public abstract class CommandHandler {

	public Map<String, Command> commands = new HashMap<>();
	private Boolean mercuryEnabled;

	public abstract void registerCommands();

	protected void addCommands(Command command) {
		commands.put(command.getIdentifier().toLowerCase(), command);
		if (mercuryEnabled == null) {
			mercuryEnabled = Bukkit.getPluginManager().getPlugin("Mercury") != null;
		}
	}

	public boolean execute(CommandSender sender, org.bukkit.command.Command cmd, String[] args) {
		if (commands.containsKey(cmd.getName().toLowerCase())) {
			Command command = commands.get(cmd.getName().toLowerCase());
			if (args.length < command.getMinArguments() || args.length > command.getMaxArguments()) {
				helpPlayer(command, sender);
				return true;
			}
			command.execute(sender, args);
		}
		return true;
	}

	public List<String> complete(CommandSender sender, org.bukkit.command.Command cmd, String[] args) {
		if (commands.containsKey(cmd.getName().toLowerCase())) {
			Command command = commands.get(cmd.getName().toLowerCase());
			List <String> completes = command.tabComplete(sender, args);
			String completeArg;
			if (args.length == 0) {
				completeArg = "";
			}
			else {
				completeArg = args [args.length - 1].toLowerCase();
			}
			if (completes == null) {
				completes = new LinkedList<String>();
				if (mercuryEnabled) {
					for(String p : MercuryAPI.getAllPlayers()) {
						if (p.toLowerCase().startsWith(completeArg)) {
							completes.add(p);
						}
					}
				}
				else {
					for(Player p : Bukkit.getOnlinePlayers()) {
						if (p.getName().toLowerCase().startsWith(completeArg)) {
							completes.add(p.getName());
						}
					}
				}
				return completes;
			}
		}
		return null;
	}

	protected void helpPlayer(Command command, CommandSender sender) {
		sender.sendMessage(new StringBuilder().append(ChatColor.RED + "Command: ")
				.append(command.getName()).toString());
		sender.sendMessage(new StringBuilder().append(ChatColor.RED + "Description: ")
				.append(command.getDescription()).toString());
		sender.sendMessage(new StringBuilder().append(ChatColor.RED + "Usage: ")
				.append(command.getUsage()).toString());
	}

}
