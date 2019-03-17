package vg.civcraft.mc.civmodcore.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import vg.civcraft.mc.civmodcore.ratelimiting.RateLimiter;
import vg.civcraft.mc.civmodcore.ratelimiting.RateLimiting;
import vg.civcraft.mc.civmodcore.util.ConfigParsing;

public class StandaloneCommandHandler {

	private JavaPlugin plugin;
	private Map<String, StandaloneCommand> commands;

	public StandaloneCommandHandler(JavaPlugin plugin) {
		this.plugin = plugin;
		this.commands = new HashMap<String, StandaloneCommand>();
		loadAll();
	}

	public void registerCommand(StandaloneCommand command) {
		String id = command.getIdentifier();
		if (id == null) {
			plugin.getLogger()
					.warning("Could not register command " + command.getClass().getName() + ". Identifier was null");
			return;
		}
		// we dont do alias resolving, Bukkit will do that for us
		commands.put(id.toLowerCase(), command);
	}

	public boolean executeCommand(CommandSender sender, org.bukkit.command.Command cmd, String[] args) {
		StandaloneCommand command = commands.get(cmd.getName().toLowerCase());
		if (command == null) {
			plugin.getLogger()
					.warning("Could not execute command " + cmd.getName() + ", no implementation was provided");
			sender.sendMessage(ChatColor.RED + "Command not available");
			return true;
		}
		if (command.hasTooManyArgs(args.length)) {
			sender.sendMessage(ChatColor.RED + "You provided too many arguments");
			helpPlayer(command, sender);
			return false;
		}
		if (command.hasTooFewArgs(args.length)) {
			sender.sendMessage(ChatColor.RED + "You provided too few arguments");
			helpPlayer(command, sender);
			return false;
		}
		if (sender instanceof Player) {
			if (!command.canBeRunByPlayers()) {
				sender.sendMessage(ChatColor.RED + "This command can only be run from console");
				return true;
			}
			if (command.isRateLimitedToExecute((Player) sender)) {
				sender.sendMessage(
						ChatColor.RED + "You are rate limited and have to wait before running this command again");
				return true;
			}
		} else {
			// console
			if (!command.canBeRunByConsole()) {
				sender.sendMessage(ChatColor.RED + "This command can only be run by players");
				return true;
			}
		}
		boolean worked = command.execute(sender, args);
		if (!worked) {
			helpPlayer(command, sender);
		}
		return worked;
	}

	public List<String> tabCompleteCommand(CommandSender sender, org.bukkit.command.Command cmd, String[] args) {
		StandaloneCommand command = commands.get(cmd.getName());
		if (command == null) {
			plugin.getLogger().warning(
					"Could not tab complete command " + cmd.getName() + ", no implementation was provided");
			return new LinkedList<String>();
		}
		if (sender instanceof Player) {
			if (!command.canBeRunByPlayers()) {
				sender.sendMessage(ChatColor.RED + "This command can only be used from console");
				return new LinkedList<String>();
			}
			if (command.isRateLimitedToTabComplete((Player) sender)) {
				sender.sendMessage(ChatColor.RED
						+ "You are rate limited and have to wait before tab completing this command again");
				return new LinkedList<String>();
			}
		} else {
			// console
			if (!command.canBeRunByConsole()) {
				sender.sendMessage(ChatColor.RED + "This command can only be used by players");
				return new LinkedList<String>();
			}
		}
		return command.tabComplete(sender, args);
	}
	
	protected void helpPlayer(StandaloneCommand sCommand, CommandSender sender) {
		org.bukkit.command.Command bukCom = getBukkitCommand(sCommand);
		if (bukCom == null) {
			sender.sendMessage(ChatColor.RED + "No help available for command " + sCommand.getIdentifier());
			return;
		}
		sender.sendMessage(ChatColor.RED + "Command: " + sCommand.getIdentifier());
		sender.sendMessage(ChatColor.RED + "Description: " + bukCom.getDescription());
		sender.sendMessage(ChatColor.RED + "Usage: " + bukCom.getUsage());
	}

	public org.bukkit.command.Command getBukkitCommand(StandaloneCommand command) {
		SimpleCommandMap commandMap;
		try {
			Field mapField = SimplePluginManager.class.getDeclaredField("commandMap");
			mapField.setAccessible(true);
			commandMap = (SimpleCommandMap) mapField.get(Bukkit.getPluginManager());
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			plugin.getLogger().severe("Failed to retrieve command map field " + e.toString());
			return null;
		}
		return (org.bukkit.command.Command) commandMap.getCommand(command.getIdentifier());
	}

	private void loadAll() {
		File file = getPluginJar();
		if (file == null) {
			return;
		}
		@SuppressWarnings("deprecation")
		JavaPluginLoader pluginLoader = new JavaPluginLoader(Bukkit.getServer());
		PluginDescriptionFile pluginYml;
		try {
			pluginYml = pluginLoader.getPluginDescription(file);
		} catch (InvalidDescriptionException e1) {
			plugin.getLogger().severe("Plugin " + plugin.getName() + " had invalid plugin.yml");
			return;
		}
		try (JarFile jar = new JarFile(file)) {
			JarEntry entry = jar.getJarEntry(CivConfigAnnotationProcessor.fileLocation);
			if (entry == null) {
				// doesn't exist, that's fine
				return;
			}
			try (InputStream stream = jar.getInputStream(entry);
					InputStreamReader reader = new InputStreamReader(stream);
					BufferedReader buffer = new BufferedReader(reader)) {
				String line;
				while ((line = buffer.readLine()) != null) {
					loadCommand(line, pluginYml);
				}
			}
		} catch (IOException e) {
			plugin.getLogger().severe("Failed to load plugin.yml: " + e.toString());
		}
	}

	private File getPluginJar() {
		try {
			Method method = JavaPlugin.class.getDeclaredMethod("getFile");
			method.setAccessible(true);
			return (File) method.invoke(plugin);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			plugin.getLogger().severe("Failed to retrieve plugin file: " + e.toString());
			return null;
		}
	}

	private void loadCommand(String classPath, PluginDescriptionFile pluginYml) {
		Class<?> commandClass;
		try {
			commandClass = Class.forName(classPath);
		} catch (ClassNotFoundException e) {
			plugin.getLogger().warning("Attempted to load command " + classPath + ", but it could not be found");
			return;
		}
		StandaloneCommand command;
		try {
			command = (StandaloneCommand) commandClass.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			plugin.getLogger().warning("Error occured when loading command " + classPath + ": " + e.toString());
			return;
		}
		Object commandSection = pluginYml.getCommands().get(command.getIdentifier().toLowerCase());
		if (commandSection == null) {
			plugin.getLogger().warning("No command with the identifier " + command.getIdentifier()
					+ " could be found in the plugin.yml. Command will be unavailable");
			return;
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> commandMap = (Map<String, Object>) commandSection;
		command.setRateLimiter(
				parseRateLimiter(command.getIdentifier() + "-ratelimit", commandMap.get("rate-limiter")));
		command.setTabCompletionRateLimiter(
				parseRateLimiter(command.getIdentifier() + "-tabratelimit", commandMap.get("tab-rate-limiter")));
		Boolean playerOnly = attemptBoolean(commandMap.get("player-only"));
		if (playerOnly != null) {
			command.setSenderMustBePlayer(playerOnly);
		}
		Boolean consoleOnly = attemptBoolean(commandMap.get("console-only"));
		if (consoleOnly != null) {
			command.setSenderMustBeConsole(consoleOnly);
			if (consoleOnly && playerOnly != null && playerOnly) {
				plugin.getLogger().severe("Command " + command.getIdentifier()
						+ " is simultaneously console only and player only. It can not be run");
			}
		}
		Integer minArgs = attemptInteger(commandMap.get("min-args"));
		if (minArgs != null) {
			command.setMinArgs(minArgs);
		}
		Integer maxArgs = attemptInteger(commandMap.get("max-args"));
		if (maxArgs != null) {
			command.setMaxArgs(maxArgs);
		}
		this.commands.put(command.getIdentifier().toLowerCase(), command);
	}

	private RateLimiter parseRateLimiter(String name, Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) o;
			if (!(map.containsKey("capacity") && map.containsKey("amount") && map.containsKey("interval"))) {
				plugin.getLogger().severe("Incomplete rate limiting configuration for command " + name);
				return null;
			}
			Integer capacity = attemptInteger(map.get("capacity"));
			if (capacity == null) {
				plugin.getLogger().severe("No capacity provided for rate limiting configuration for " + name);
				return null;
			}
			Integer refillAmount = attemptInteger(map.get("amount"));
			if (refillAmount == null) {
				plugin.getLogger().severe("No amount provided for rate limiting configuration for " + name);
				return null;
			}
			long interval = ConfigParsing.parseTime(String.valueOf(map.get("interval")));
			if (interval == 0) {
				plugin.getLogger().severe("No interval provided for rate limiting configuration for " + name);
				return null;
			}
			// multiply by 50 to convert ticks into ms
			return RateLimiting.createRateLimiter("command-" + name, capacity, capacity, refillAmount, interval * 50);
		}
		return null;
	}

	// if only java generics allowed instanceof...

	private Integer attemptInteger(Object o) {
		if (o instanceof Integer) {
			return (Integer) o;
		}
		return null;
	}

	private Boolean attemptBoolean(Object o) {
		if (o instanceof Boolean) {
			return (Boolean) o;
		}
		return null;
	}
}
