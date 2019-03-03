package com.programmerdan.minecraft.simpleadminhacks.hacks;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.programmerdan.minecraft.simpleadminhacks.BroadcastLevel;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.configs.NewfriendAssistConfig;

/**
 * A simple Newfriend tracker and assist module. Keeps track of newfriends so far.
 * 
 * @author ProgrammerDan
 */
public class NewfriendAssist extends SimpleHack<NewfriendAssistConfig> implements Listener, CommandExecutor {

	public static final String NAME = "NewfriendAssist";
	private static long newfriendCount = 0l;
	/**
	 * We could use the various Bukkit methods every time we want to peak at our data, but the cost of storing it is low
	 * and this is a hack, after all..
	 */
	private HashMap<UUID, String> newfriendNames;
	private HashMap<UUID, SessionTime> newfriendSessionTime;

	public NewfriendAssist(SimpleAdminHacks plugin, NewfriendAssistConfig config) {
		super(plugin, config);
	}

	/**
	 * Track standard quit events to monitor newfriend playtime on the day of join.
	 */
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void NewLeaveEvent(PlayerQuitEvent exit) {
		if (!config.isEnabled()) return;
		doLeave(exit.getPlayer());
	}

	/**
	 * Track standard kick events to monitor newfriend playtime on the day of join.
	 */
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void NewKickEvent(PlayerKickEvent exit) {
		if (!config.isEnabled()) return;
		doLeave(exit.getPlayer());
	}

	private void doLeave(Player departed) {
		if (departed == null) return;

		UUID depUUID = departed.getUniqueId();
		if (newfriendSessionTime.containsKey(depUUID)) {
			newfriendSessionTime.get(depUUID).endSession(System.currentTimeMillis());
		}
	}

	/**
	 * Track join events to keep track of session time for previous newfriends and
	 * to announce the presence of new newfriends, if configured.
	 *
	 * Monitoring is always on and is not configurable.
	 */
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void NewJoinEvent(PlayerJoinEvent join) {
		if (!config.isEnabled()) return;
		Player newfriend = join.getPlayer();
		if (newfriend == null) return;

		UUID newUUID = newfriend.getUniqueId();
		if (newfriendSessionTime.containsKey(newUUID)) {
			newfriendSessionTime.get(newUUID).startSession(System.currentTimeMillis());
		}

		if (newfriend.hasPlayedBefore()) return;

		NewfriendAssist.newfriendCount ++;

		newfriendNames.put(newUUID, newfriend.getName());
		newfriendSessionTime.put(newUUID, new SessionTime(System.currentTimeMillis()));

		if (config.getAnnounceBroadcast().size() > 0) {
			// Prepare message
			String cleanMessage = cleanMessage(join);

			// Overlap is possible. Some people might get double-notified
			for (BroadcastLevel level : config.getAnnounceBroadcast()) {
				plugin().debug("  Broadcast to {0}", level);
				switch(level) {
				case OP:
					plugin().serverOperatorBroadcast(cleanMessage);
					break;
				case PERM:
					plugin().serverBroadcast(cleanMessage); 
					break;
				case CONSOLE:
					plugin().serverSendConsoleMessage(cleanMessage);
					break;
				case ALL:
					plugin().serverOnlineBroadcast(cleanMessage);
					break;
				}
			}
		}

		if (config.isIntroKitEnabled()) {
			ItemStack[] introKit = config.getIntroKit();
			if (introKit != null && introKit.length > 0) {
			    Inventory inv = newfriend.getInventory();
			    inv.addItem(introKit);
				plugin().log(Level.INFO, "  Gave newbit kit to {0}", newfriend.getDisplayName());
			}
		}

	}

	private String cleanMessage(PlayerJoinEvent event) {
		return ChatColor.translateAlternateColorCodes('&',
				config.getAnnounceMessage()
					.replaceAll("%Player%", event.getPlayer().getDisplayName())
				);
	}

	@Override
	public void registerListeners() {
		if (config.isEnabled()) {
			plugin().log("Registering PlayerJoin/Quit/KickEvent listener");
			plugin().registerListener(this);
		}
	}

	@Override
	public void registerCommands() {
		if (config.isEnabled()) {
			plugin().log("Registering introkit command");
			plugin().registerCommand("introkit", this);
		}
	}

	@Override
	public void dataBootstrap() {
		this.newfriendNames = new HashMap<UUID, String>();
		this.newfriendSessionTime = new HashMap<UUID, SessionTime>();
	}

	@Override
	public void unregisterListeners() {
	}

	@Override
	public void unregisterCommands() {
	}

	@Override
	public void dataCleanup() {
		// Before we dump the data, lets dump a status to the log.
		if (plugin() != null) {
			plugin().log(Level.INFO, status());
		}

		this.newfriendNames.clear();
		this.newfriendNames = null;
		this.newfriendSessionTime.clear();
		this.newfriendSessionTime = null;
	}

	/**
	 * Shows all tracked data on new friends.
	 */
	@Override
	public String status() {
		StringBuffer sb = new StringBuffer();
		if (config != null && config.isEnabled()) {
			sb.append("NewfriendAssist.PlayerJoin/Quit/KickEvent monitoring active");
		} else {
			sb.append("NewfriendAssist.PlayerJoin/Quit/KickEvent monitoring not active");
		}

		sb.append("\n  Since server start, ").append(NewfriendAssist.newfriendCount)
				.append(" new players have joined.");

		if (this.newfriendNames != null && !this.newfriendNames.isEmpty()) {
			sb.append("\n  New players since this hack was last enabled:");
			for (HashMap.Entry<UUID, String> entry : this.newfriendNames.entrySet()) {
				sb.append("\n    ").append(entry.getKey()).append(": ").append(entry.getValue());
				SessionTime soFar = newfriendSessionTime.get(entry.getKey());
				if (soFar != null) {
					sb.append(" online ").append(soFar.totalTime() / 1000l).append(" seconds");
				}
			}
		}

		if (config.isIntroKitEnabled()) {
			sb.append("\n  Introkit gifting is enabled. Current Introkit:");
			if (config.getIntroKit() != null && config.getIntroKit().length > 0 ) {
				for (ItemStack item : config.getIntroKit()) {
					sb.append("\n    ").append(item);
				}
			} else {
				sb.append("\n    ").append(ChatColor.RED).append("-- in error --");
			}
		} else {
			sb.append("\n  Introkit gifting is disabled.");
		}

		return sb.toString();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!config.isIntroKitEnabled()) {
			sender.sendMessage(ChatColor.RED + "Introkit gifting is disabled.");
			return true;
		}

		if (args.length < 1) return false;

		Player p = plugin().getServer().getPlayer(args[0]);

		if (p == null) {
			try {
				UUID pu = UUID.fromString(args[0]);
				p = plugin().getServer().getPlayer(pu);
			} catch (IllegalArgumentException iae) {
				p = null;
			}
		}

		if (p == null) {
			sender.sendMessage(ChatColor.RED + "Unable to find " + args[0]);
		} else {
			plugin().log(Level.INFO, "Sent introkit to {0}", args[0]);
			p.sendMessage(ChatColor.GREEN + "You've been given an introductory kit!");
			Inventory inv = p.getInventory();
			inv.addItem(config.getIntroKit());
		}

		return true;
	}

	public static NewfriendAssistConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new NewfriendAssistConfig(plugin, config);
	}
}