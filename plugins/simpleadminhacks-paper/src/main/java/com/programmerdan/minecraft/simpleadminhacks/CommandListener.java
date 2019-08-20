package com.programmerdan.minecraft.simpleadminhacks;

import java.util.Arrays;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Base command handler that listens for the "hacks" command allowing CnC of all the loaded or unloaded hacks.
 *
 * @author ProgrammerDan
 */
public class CommandListener implements CommandExecutor {
	private SimpleAdminHacks plugin;

	/**
	 * Instantiate using static call
	 */
	public CommandListener() {
		this(SimpleAdminHacks.instance());
	}

	/**
	 * Instantiate with provided plugin, useful for testing
	 */
	public CommandListener(SimpleAdminHacks plugin) {
		this.plugin = plugin;
	}

	/**
	 * Captures invocations of "hacks". Will attempt to command and control the various hacks as best as possible
	 * based on that.
	 * 
	 * @return true if command executed or should fail quietly, false if help should be displayed
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			// display all hacks that are enabled.
			return showHacks(sender);
		} else if (args.length == 1) {
			// display a single hack
			return showHack(sender, args[0]);
		} else if (args.length == 2) {
			// enable/disable a hack
			return controlHack(sender, args[0], args[1]);
		} else if (args.length == 3) {
			// get a config
			return alterHack(sender, args[0], args[1], args[2], null);
		} else {
			// set a config
			return alterHack(sender, args[0], args[1], args[2], StringUtils.join(Arrays.copyOfRange(args, 3, args.length - 1), " "));
		}
	}

	/**
	 * Show a list of all registered hacks and their status.
	 */
	private boolean showHacks(CommandSender sender) {
		if (plugin == null) return false;

		StringBuilder sb = new StringBuilder();

		sb.append(ChatColor.WHITE).append("List of hacks:\n");

		for (SimpleHack<?> hack : plugin.getHackManager().getHacks()) {
			sb.append("  ")
				.append(ChatColor.AQUA).append(hack.getName())
				.append(ChatColor.WHITE).append(": ");
			if (hack.isEnabled()) {
				sb.append(ChatColor.GREEN).append("enabled");
			} else {
				sb.append(ChatColor.RED).append("disabled");
			}
			sb.append("\n");
		}

		sender.sendMessage(sb.toString());
		return true;
	}

	/**
	 * Internal utility to get a hack from name.
	 */
	private SimpleHack<?> findHack(String hackName) {
		for (SimpleHack<?> candidate : plugin.getHackManager().getHacks()) {
			if (candidate.getName().equals(hackName)) {
				return candidate;
			}
		}
		return null;
	}

	/**
	 * Show self-reported details from a single hack.
	 */
	private boolean showHack(CommandSender sender, String hackName) {
		if (plugin == null) return false;

		SimpleHack<?> hack = findHack(hackName);

		StringBuilder sb = new StringBuilder();

		if (hack == null) {
			sb.append(ChatColor.AQUA).append(hackName).append(ChatColor.WHITE).append(" not found.");
		} else {
			sb.append(ChatColor.WHITE).append("Self-reported details from hack ")
				.append(ChatColor.AQUA).append(hackName).append(ChatColor.WHITE).append(":\n")
				.append(ChatColor.GRAY).append(hack.status());
		}

		sender.sendMessage(sb.toString());
		return true;
	}

	/**
	 * Enable/disable a hack.
	 */
	private boolean controlHack(CommandSender sender, String hackName, String control) {
		if (plugin == null || hackName == null || control == null) return false;

		if (!(control.equals("enable") || control.equals("disable"))) return false;

		SimpleHack<?> hack = findHack(hackName);

		StringBuilder sb = new StringBuilder();

		if (hack == null) {
			sb.append(ChatColor.AQUA).append(hackName).append(ChatColor.WHITE).append(" not found.");
		} else {
			sb.append(ChatColor.AQUA).append(hackName);
			if (control.equals("enable")) {
				if (hack.isEnabled()) {
					sb.append(ChatColor.GREEN).append(" is already enabled");
				} else {
					hack.softEnable();
					if (hack.isEnabled()) {
						sb.append(ChatColor.GREEN).append(" enabled successfully");
					} else {
						sb.append(ChatColor.RED).append(" failed to enable");
					}
				}
			} else {
				if (hack.isEnabled()) {
					hack.softDisable();
					if (hack.isEnabled()) {
						sb.append(ChatColor.RED).append(" failed to disable");
					} else {
						sb.append(ChatColor.GREEN).append(" disabled successfully");
					}
				} else {
					sb.append(ChatColor.GREEN).append(" is already disabled");
				}
			}
		}

		sender.sendMessage(sb.toString());
		return true;
	}

	/**
	 * Alter the config within a hack.
	 */
	private boolean alterHack(CommandSender sender, String hackName, String control, String key, String value) {
		if (plugin == null || hackName == null || control == null || key == null) return false;

		if (!(control.equals("get") || control.equals("set"))) return false;

		SimpleHack<?> hack = findHack(hackName);

		StringBuilder sb = new StringBuilder();

		if (hack == null) {
			sb.append(ChatColor.AQUA).append(hackName).append(ChatColor.WHITE).append(" not found.");
		} else {
			// We always get first.
			sb.append(ChatColor.AQUA).append(hackName);
			sb.append(" : ").append(ChatColor.BLUE).append(key).append(ChatColor.WHITE).append(" = ");
			Object currentValue = hack.config().get(key);
			if (currentValue == null) {
				sb.append(ChatColor.GRAY).append(" has no value");
			} else {
				sb.append(ChatColor.GOLD).append(currentValue.toString());
			}
			if (control.equals("set")) {
				// and optionally set.
				try {
					hack.config().set(key, value);
					sb.append("\n").append(ChatColor.WHITE).append("  set to = ")
							.append(ChatColor.GREEN).append(value); 
				} catch (Exception e) {
					plugin.log(Level.WARNING, "Failed to set " + key + " to " + value + " in hack " + hackName, e);
					sb.append("\n").append(ChatColor.RED).append("  failed to set = ")
							.append(ChatColor.DARK_GREEN).append(value); 
				}
			}
		}

		sender.sendMessage(sb.toString());
		return true;
	}
}
