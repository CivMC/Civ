package com.programmerdan.minecraft.simpleadminhacks.hacks;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.Listener;
import org.bukkit.util.NumberConversions;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.bots.Bot;
import com.programmerdan.minecraft.simpleadminhacks.configs.HackBotConfig;

/**
 * Wrapping some NPC entity classes for maximum admin fun
 * 
 * @author ProgrammerDan
 *
 */
public class HackBot extends SimpleHack<HackBotConfig> implements Listener, CommandExecutor {

	private HashMap<String, Bot> bots;
	
	public static final String NAME = "HackBot";
	
	public HackBot(SimpleAdminHacks plugin, HackBotConfig config) {
		super(plugin, config);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length < 2) { 
			return false;
		}
		
		String cmd = args[0];
		if ("all".equals(cmd)) {
			String subcmd = args[1];
			if ("save".equals(subcmd)) {
				for (Bot npc : bots.values()) {
					npc.flushToConfig();
				}
				plugin().saveConfig();
				sender.sendMessage(ChatColor.GREEN + "Saved all bots");
			} else if ("spawn".equals(subcmd)) {
				for (Bot npc : bots.values()) {
					npc.spawn();
				}
				sender.sendMessage(ChatColor.GREEN + "Spawned all unspawned bots");
			} else if ("despawn".equals(subcmd)) {
				for (Bot npc : bots.values()) {
					npc.despawn();
				}
				sender.sendMessage(ChatColor.BLUE + "Despawned all spawned bots");
			} else {
				return false;
			}
		} else if ("help".equals(cmd)) {
			String subcmd = args[1];
			if ("generate".equals(subcmd)) {
				sender.sendMessage(ChatColor.GREEN + "Help for /hackbot generate command:");
				sender.sendMessage(ChatColor.AQUA + "  usage:         " + ChatColor.WHITE + "/hackbot generate [npc name] [name of skin] [spawn <world,x,y,z>] [options and flags]");
				sender.sendMessage(ChatColor.AQUA + "  npc name:      " + ChatColor.WHITE + " The name to give this NPC (shows up in player listing)");
				sender.sendMessage(ChatColor.AQUA + "  name of skin:  " + ChatColor.WHITE + " The Minecraft user from whom to steal a skin (optional)");
				sender.sendMessage(ChatColor.AQUA + "  spawn:         " + ChatColor.WHITE + " The spawn (and respawn) location for this npc given as <WorldName or UUID,x,y,z>");
				sender.sendMessage(ChatColor.AQUA + "  options/flags: " + ChatColor.WHITE + " flags and settings");
				sender.sendMessage(ChatColor.BLUE + "    -alive:  " + ChatColor.WHITE + " Spawn in the bot right away");
				sender.sendMessage(ChatColor.BLUE + "    -dead:   " + ChatColor.WHITE + " Configure the bot but don't spawn it");
				sender.sendMessage(ChatColor.GREEN + "    (more tbd)");
			}
		} else if ("generate".equals(cmd)) {
			String npcname = args[1];
			
			String skinname = null;
			int idx = 2;
			if (!args[idx].startsWith("<")) { // has skinname.
				skinname = args[idx++];
			}
			
			if (idx >= args.length) {
				sender.sendMessage(ChatColor.RED + "Insufficient parameters.");
				return false;
			}
			
			StringBuilder accumulator = new StringBuilder();
			for (; idx < args.length; idx++) {
				accumulator.append(args[idx]);
				if (!args[idx].endsWith(">")) { // not done yet
					accumulator.append(" ");
				} else { 
					break;
				}
			}
			
			String location = accumulator.toString();
			
			Pattern split = Pattern.compile("\\<(.*?),(.*?),(.*?),(.*?)\\>");
			
			plugin().log("Location: " + location);

			Matcher parts = split.matcher(location);
			if (!parts.matches()){
				sender.sendMessage(ChatColor.RED + "Location should be formatted <World, x, y, z>");
				return false;
			}
			
			if (parts.groupCount() != 4) {
				sender.sendMessage(ChatColor.RED + "Location needs four components! Use '/hackbot help generate' for details");
				return false;
			}
			
			String world = parts.group(1);
			
			ConfigurationSection bconfig = config().getBase().createSection(npcname);
			bconfig.set("name", npcname);
			
			if (skinname != null) {
				bconfig.set("skin", skinname);
			}
			
			try {
				double x = Double.valueOf(parts.group(2));
				double y = Double.valueOf(parts.group(3));
				double z = Double.valueOf(parts.group(4));

				World realWorld = plugin().serverGetWorld(world);
				
				if (realWorld == null) {
					sender.sendMessage(ChatColor.RED + "World " + world + " not found!");
					return true;
				}
				Location loc = new Location(realWorld, x, y, z);
				
				bconfig.set("spawnLocation", loc);
			} catch (NumberFormatException nfe) {
				sender.sendMessage(ChatColor.RED + "Location needs x,y,z to be numeric!");
				return true;
			}
			
			if (++idx < args.length) {
				for (; idx < args.length; idx++) {
					if ("-alive".equals(args[idx])) {
						bconfig.set("alive", true);
					} else if ("-dead".equals(args[idx])) {
						bconfig.set("alive", false);
					}
				}
			}
			
			try {
				Bot npcbot = new Bot(bconfig);
				bots.put(npcname, npcbot);
			} catch (InvalidConfigurationException ice) {
				plugin().log(Level.WARNING, "Failed to create " + npcname, ice);
				sender.sendMessage(ChatColor.RED + "Failed to create " + npcname);
				return false;
			}
			
			sender.sendMessage(ChatColor.GREEN + "Generated new bot " + npcname);
		} else {
			String npcname = args[1];
			Bot npc = bots.get(npcname);
			if (npc == null) {
				sender.sendMessage(ChatColor.RED + "Unable to find NPC by name");
				return false;
			}
			if ("save".equals(cmd)) {
				npc.flushToConfig();
				plugin().saveConfig();
				sender.sendMessage(ChatColor.GREEN + "Saved bot " + npcname);
			} else if ("despawn".equals(cmd)) {
				npc.despawn();
				sender.sendMessage(ChatColor.BLUE + "Despawned bot " + npcname);
			} else if ("destroy".equals(cmd)) {
				npc.despawn();
				plugin().getConfig().set(npc.config().getCurrentPath(), null); // permanent.
				npc.invalidate();
				bots.remove(npcname);
				plugin().saveConfig();
				sender.sendMessage(ChatColor.RED + "Destroyed permanently bot " + npcname);
			} else if ("spawn".equals(cmd)) {
				npc.spawn();
				sender.sendMessage(ChatColor.GREEN + "Spawned bot " + npcname);
			} else {
				return false;
			}
		}
		
		return true;
	}

	@Override
	public void registerListeners() {
		plugin().registerListener(this);
	}

	@Override
	public void registerCommands() {
		plugin().registerCommand("hackbot", this);
	}

	@Override
	public void dataBootstrap() {
		this.bots = new HashMap<String, Bot>();
	}

	@Override
	public void unregisterListeners() {
	}

	@Override
	public void unregisterCommands() {
	}

	@Override
	public void dataCleanup() {
		this.bots.clear();
		this.bots = null;
	}

	@Override
	public String status() {
		return null;
	}

}
