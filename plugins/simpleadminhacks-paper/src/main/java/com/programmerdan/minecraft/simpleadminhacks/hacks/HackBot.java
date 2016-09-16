package com.programmerdan.minecraft.simpleadminhacks.hacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.bots.Bot;
import com.programmerdan.minecraft.simpleadminhacks.configs.CTAnnounceConfig;
import com.programmerdan.minecraft.simpleadminhacks.configs.HackBotConfig;

import de.inventivegames.npc.NPC;
import de.inventivegames.npc.entity.player.NPCPlayerEntityBase;
import de.inventivegames.npc.event.NPCDespawnEvent;

/**
 * Wrapping some NPC entity classes for maximum admin fun
 * 
 * @author ProgrammerDan
 *
 */
public class HackBot extends SimpleHack<HackBotConfig> implements Listener, CommandExecutor {

	private HashMap<String, Bot> bots;
	
	private ProtocolManager pm;
	
	public static final String NAME = "HackBot";
	
	public HackBot(SimpleAdminHacks plugin, HackBotConfig config) {
		super(plugin, config);

		if (!plugin.serverHasPlugin("ProtocolLib")){
			plugin.log("ProtocolLib not found, disabling HackBots.");
			config.setEnabled(false);
		}
		if (!plugin.serverHasPlugin("NPCLib")){
			plugin.log("NPCLib not found, disabling HackBots.");
			config.setEnabled(false);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length < 2) {
			if (args.length == 1 && ("status".equals(args[0]) || "list".equals(args[0]))) {
				sender.sendMessage(ChatColor.AQUA + "HackBot " + ChatColor.WHITE + "status:\n" + status());
				return true;
			}
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
				sender.sendMessage(ChatColor.AQUA + "  usage:         " + ChatColor.WHITE + "/hackbot generate [npc name] [name of skin] [<world,x,y,z>] [options and flags]");
				sender.sendMessage(ChatColor.AQUA + "  npc name:      " + ChatColor.WHITE + " The name to give this NPC (shows up in player listing)");
				sender.sendMessage(ChatColor.AQUA + "  name of skin:  " + ChatColor.WHITE + " The Minecraft user from whom to steal a skin (optional)");
				sender.sendMessage(ChatColor.AQUA + "  <world,x,y,z>: " + ChatColor.WHITE + " The spawn (and respawn) location for this npc given as <WorldName or UUID,x,y,z>");
				sender.sendMessage(ChatColor.AQUA + "  options/flags: " + ChatColor.WHITE + " flags and settings");
				sender.sendMessage(ChatColor.BLUE + "    +alive:    " + ChatColor.WHITE + " Spawn in the bot right away");
				sender.sendMessage(ChatColor.BLUE + "    -alive:    " + ChatColor.WHITE + " Configure the bot but don't spawn it");
				sender.sendMessage(ChatColor.BLUE + "    +collide:  " + ChatColor.WHITE + " Bot responds to collisions");
				sender.sendMessage(ChatColor.BLUE + "    -collide:  " + ChatColor.WHITE + " Bot does not");
				sender.sendMessage(ChatColor.BLUE + "    +list:  " + ChatColor.WHITE + " Bot appears in player list");
				sender.sendMessage(ChatColor.BLUE + "    -list:  " + ChatColor.WHITE + " Bot does not");
				sender.sendMessage(ChatColor.BLUE + "    +frozen:  " + ChatColor.WHITE + " Bot is immobile");
				sender.sendMessage(ChatColor.BLUE + "    -frozen:  " + ChatColor.WHITE + " Bot is not");
				sender.sendMessage(ChatColor.BLUE + "    +god:  " + ChatColor.WHITE + " Bot is invulnerable");
				sender.sendMessage(ChatColor.BLUE + "    -god:  " + ChatColor.WHITE + " Bot is not");
				sender.sendMessage(ChatColor.BLUE + "    +possess:  " + ChatColor.WHITE + " Bot can be controlled (?)");
				sender.sendMessage(ChatColor.BLUE + "    -possess:  " + ChatColor.WHITE + " Bot can not");
				
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
			
			ConfigurationSection bconfig = config.getBots().createSection(npcname);
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

			// Set defaults.Then harvest flags.
			bconfig.set("alive", false);
			bconfig.set("collision", true);
			bconfig.set("invulnerable", false);
			bconfig.set("listed", true);
			bconfig.set("frozen", false);
			bconfig.set("controllable", true);
			if (++idx < args.length) {
				for (; idx < args.length; idx++) {
					if ("+alive".equals(args[idx]) || "alive".equals(args[idx])) {
						bconfig.set("alive", true);
					} else if ("-alive".equals(args[idx])) {
						bconfig.set("alive", false);
					} else if ("+collide".equals(args[idx]) || "collide".equals(args[idx])) {
						bconfig.set("collide", true);
					} else if ("-collide".equals(args[idx])) {
						bconfig.set("collide", false);
					} else if ("+list".equals(args[idx]) || "list".equals(args[idx])) {
						bconfig.set("listed", true);
					} else if ("-list".equals(args[idx])) {
						bconfig.set("listed", false);
					} else if ("+frozen".equals(args[idx]) || "frozen".equals(args[idx])) {
						bconfig.set("frozen", true);
					} else if ("-frozen".equals(args[idx])) {
						bconfig.set("frozen", false);
					} else if ("+god".equals(args[idx]) || "god".equals(args[idx])) {
						bconfig.set("invulnerable", true);
					} else if ("-god".equals(args[idx])) {
						bconfig.set("invulnerable", false);
					} else if ("+possess".equals(args[idx]) || "possess".equals(args[idx])) {
						bconfig.set("controllable", true);
					} else if ("-possess".equals(args[idx])) {
						bconfig.set("controllable", false);
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
				plugin().log(npc.npc().getClass().getName());
				sender.sendMessage(ChatColor.GREEN + "Spawned bot " + npcname);
			} else {
				return false;
			}
		}
		
		return true;
	}

	@Override
	public void registerListeners() {
		if (config.isEnabled()) {
			plugin().log("Registering NPCLib listener");
			plugin().registerListener(this);
			
			plugin().log("Registering ProtocolLib Hooks");
			pm.addPacketListener( new HackBotPingHook(plugin(), this) );
		}
	}

	@Override
	public void registerCommands() {
		if (config.isEnabled()) {
			plugin().log("Registering hackbot command");
			plugin().registerCommand("hackbot", this);
		}
	}

	@Override
	public void dataBootstrap() {
		if (config.isEnabled()) {
			plugin().log("Getting ProtocolLib Manager");
			pm = ProtocolLibrary.getProtocolManager();
			plugin().log("Loading hackbots");
			this.bots = new HashMap<String, Bot>();
			ConfigurationSection aBots = this.config.getBots();
			for (String bot : aBots.getKeys(false)) {
				ConfigurationSection aBot = aBots.getConfigurationSection(bot);
				if (!config.doSpawnOnLoad()) {
					aBot.set("alive", false);
				}
				try {
					Bot savedBot = new Bot(aBot);
					plugin().log(Level.INFO, "Created bot {0}", bot);
					this.bots.put(bot, savedBot);
				} catch (InvalidConfigurationException e) {
					plugin().log(Level.WARNING, "Unable to create bot " + bot, e);
				}
			}
		}
	}

	@Override
	public void unregisterListeners() {
	}

	@Override
	public void unregisterCommands() {
	}

	@Override
	public void dataCleanup() {
		if (this.bots != null) {
			for (String bot : bots.keySet()) {
				Bot abot = bots.get(bot);
				abot.flushToConfig();
				abot.despawn();
			}
		}
		this.bots.clear();
		this.bots = null;
	}
	
	public List<Bot> getAliveBots() {
		List<Bot> alive = new ArrayList<Bot>();
		for (String bot : bots.keySet()) {
			Bot abot = bots.get(bot);
			if (abot.isAlive() && abot.npc().getBukkitEntity() != null && !abot.npc().getBukkitEntity().isDead()) {
				alive.add(abot);
			}
		}
		return alive;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void tellJoiningPlayerAboutBots(PlayerJoinEvent join) {
		for (Bot bot : getAliveBots()) {
			if (bot.npc().isShownInList()) {
				((NPCPlayerEntityBase) bot.npc()).updateToPlayer(join.getPlayer());
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) 
	public void tellPlayersAboutDeath(NPCDespawnEvent npce) {
		NPC npc = npce.getNPC();
		if (npc instanceof NPCPlayerEntityBase) {
			NPCPlayerEntityBase bot = (NPCPlayerEntityBase) npc;
			for (Player p : plugin().serverOnlinePlayers()) {
				bot.updateToPlayer(p);
			}
		}
	}

	@Override
	public String status() {
		StringBuffer sb = new StringBuffer();
		if (config != null && config.isEnabled()) {
			sb.append("HackBots are active");
		} else {
			sb.append("HackBots are not active");
			return sb.toString();
		}

		sb.append("\n  Bots ").append(config.doSpawnOnLoad() ? "do" : "don't").append(" spawn on load.");
		sb.append("\n  All bots:");
		for (String bot : bots.keySet()) {
			Bot abot = bots.get(bot);
			sb.append("\n    ").append(ChatColor.WHITE).append(abot.getName())
					.append(ChatColor.RESET).append(" is ");
			if (!abot.viable()) {
				sb.append(ChatColor.RED).append("non-viable ");
			} else if (abot.isAlive()) {
				sb.append(ChatColor.GREEN).append("alive "); 
			} else {
				sb.append(ChatColor.YELLOW).append("despawned ");
			}
			sb.append(ChatColor.RESET).append("last seen at ").append(ChatColor.AQUA);
			Location qLoc = abot.getLocation() == null ? abot.getSpawnLocation() : abot.getLocation();
			sb.append("<").append(ChatColor.WHITE).append(qLoc.getWorld().getName())
				.append(ChatColor.AQUA).append(",")
				.append(ChatColor.WHITE).append(qLoc.getBlockX())
				.append(ChatColor.AQUA).append(",")
				.append(ChatColor.WHITE).append(qLoc.getBlockY())
				.append(ChatColor.AQUA).append(",")
				.append(ChatColor.WHITE).append(qLoc.getBlockZ())
				.append(ChatColor.AQUA).append(">");
			sb.append(ChatColor.RESET).append("; details: ").append(abot.status());
		}
		return sb.toString();
	}
	
	public static HackBotConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new HackBotConfig(plugin, config);
	}
}
