package vg.civcraft.mc.civmodcore.command;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civmodcore.ratelimiting.RateLimiter;
import vg.civcraft.mc.civmodcore.util.TextUtil;

@Deprecated
public abstract class CommandHandler {

	private static final String cmdMustBePlayer = "<b>This command can only be used by in-game players.";
	private static final String cmdRateLimited = "<b>You have run this command too often and have to wait before running it again.";

	public Map<String, Command> commands = new HashMap<>();

	public abstract void registerCommands();

	protected void addCommands(Command command) {
		commands.put(command.getIdentifier().toLowerCase(), command);
	}

	public boolean execute(CommandSender sender, org.bukkit.command.Command cmd, String[] args) {
		if (commands.containsKey(cmd.getName().toLowerCase())) {
			Command command = commands.get(cmd.getName().toLowerCase());
			boolean isPlayer = sender instanceof Player;
			if (command.getSenderMustBePlayer() && !isPlayer) {
				sender.sendMessage(TextUtil.parse(cmdMustBePlayer));
				return true;
			}
			if (args.length < command.getMinArguments()
					|| (command.getErrorOnTooManyArgs() && args.length > command.getMaxArguments())) {
				helpPlayer(command, sender);
				return true;
			}
			RateLimiter limiter = command.getRateLimiter();
			if (limiter != null && isPlayer) {
				if (!limiter.pullToken((Player) sender)) {
					sender.sendMessage(TextUtil.parse(cmdRateLimited));
					return true;
				}
			}
			command.setSender(sender);
			command.setArgs(args);
			command.execute(sender, args);
		}
		else {
			sender.sendMessage("Command was registered in plugin.yml, but not registered in command handler, tell a dev about this");
		}
		return true;
	}

	public List<String> complete(CommandSender sender, org.bukkit.command.Command cmd, String[] args) {
		if (commands.containsKey(cmd.getName().toLowerCase())) {
			Command command = commands.get(cmd.getName().toLowerCase());
			boolean isPlayer = sender instanceof Player;
			if (command.getSenderMustBePlayer() && !isPlayer) {
				sender.sendMessage(TextUtil.parse(cmdMustBePlayer));
				return null;
			}
			RateLimiter limiter = command.getTabCompletionRateLimiter();
			if (limiter != null && isPlayer) {
				if (!limiter.pullToken((Player) sender)) {
					sender.sendMessage(TextUtil.parse(cmdRateLimited));
					return null;
				}
			}

			command.setSender(sender);
			command.setArgs(args);
			List<String> completes = command.tabComplete(sender, args);
			String completeArg;
			if (args.length == 0) {
				completeArg = "";
			} else {
				completeArg = args[args.length - 1].toLowerCase();
			}
			if (completes == null) {
				completes = new LinkedList<String>();
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (p.getName().toLowerCase().startsWith(completeArg)) {
						completes.add(p.getName());
					}
				}
				return completes;
			} else {
				return completes;
			}
		}
		return null;
	}

	protected void helpPlayer(Command command, CommandSender sender) {
		sender.sendMessage(new StringBuilder().append(ChatColor.RED + "Command: ").append(command.getName()).toString());
		sender.sendMessage(new StringBuilder().append(ChatColor.RED + "Description: ").append(command.getDescription())
				.toString());
		sender.sendMessage(new StringBuilder().append(ChatColor.RED + "Usage: ").append(command.getUsage()).toString());
	}
}
