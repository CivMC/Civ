package vg.civcraft.mc.civchat2.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.civchat2.event.GlobalChatEvent;
import vg.civcraft.mc.civchat2.utility.CivChat2SettingsManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/*
 * @author jjj5311
 *
 */
public class CivChat2Listener implements Listener {

	private CivChat2Manager chatman;
	private CivChat2SettingsManager settings;
	private Set<UUID> localWarn;

	public CivChat2Listener(CivChat2Manager instance) {
		chatman = instance;
		settings = CivChat2.getInstance().getCivChat2SettingsManager();
		localWarn = new HashSet<>();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerDeath(PlayerDeathEvent playerDeathEvent) {
		playerDeathEvent.setDeathMessage(null);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent playerQuitEvent) {
		playerQuitEvent.setQuitMessage(null);
		for (Player p : Bukkit.getOnlinePlayers()){
			if (settings.getShowLeaves(p.getUniqueId())){
				p.sendMessage(playerQuitEvent.getPlayer().getDisplayName() + ChatColor.YELLOW + " has left the game");
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
		if (!CivChat2.getInstance().getPluginConfig().getLoginAnnounce()) {
			playerJoinEvent.setJoinMessage(null);
		}
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (settings.getShowJoins(p.getUniqueId())) {
				p.sendMessage(playerJoinEvent.getPlayer().getDisplayName() + ChatColor.YELLOW + " has joined the game");
			}
		}

		if (CivChat2.getInstance().getPluginConfig().getChatRangeWarn() && !playerJoinEvent.getPlayer().hasPlayedBefore()) {
			localWarn.add(playerJoinEvent.getPlayer().getUniqueId());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerKick(PlayerKickEvent playerKickEvent) {
		playerKickEvent.setLeaveMessage(null);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onGlobalChatEvent(GlobalChatEvent localchat) {

		if (localWarn.contains(localchat.getPlayer().getUniqueId())) {
			localchat.getPlayer().sendMessage(ChatColor.GOLD
					+ "Only players within "
					+ CivChat2.getInstance().getPluginConfig().getChatRange()
					+ " blocks of you can see your messages. Join a group to chat with players farther away!");
			localWarn.remove(localchat.getPlayer().getUniqueId());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerChatEvent(final AsyncPlayerChatEvent asyncPlayerChatEvent) {

		asyncPlayerChatEvent.setCancelled(true);
		// This needs to be done sync to avoid a rare deadlock due to minecraft
		// internals
		new BukkitRunnable() {

			@Override
			public void run() {

				String chatMessage = asyncPlayerChatEvent.getMessage();
				Player sender = asyncPlayerChatEvent.getPlayer();
				UUID chatChannel = chatman.getChannel(sender);
				Group groupChat = chatman.getGroupChatting(sender);


				if (chatChannel != null) {
					StringBuilder sb = new StringBuilder();
					Player receiver = Bukkit.getPlayer(chatChannel);
					if (receiver != null) {
						chatman.sendPrivateMsg(sender, receiver, chatMessage);
						return;
					} else {
						chatman.removeChannel(sender);
						String offlineMessage = sb.append(ChatColor.GOLD)
								.append("The player you were chatting with has gone offline,")
								.append(" you have been moved to regular chat").toString();
						sb.delete(0, sb.length());
						sender.sendMessage(offlineMessage);
						return;
					}
				}
				if (groupChat != null) {
					// Player is group chatting
					if (NameAPI.getGroupManager().hasAccess(groupChat, sender.getUniqueId(),
							PermissionType.getPermission("WRITE_CHAT"))) {
						chatman.sendGroupMsg(sender, groupChat, chatMessage);
						return;
						// Player lost perm to write in the chat
					} else {
						chatman.removeGroupChat(sender);
						sender.sendMessage(ChatColor.RED
								+ "You have been removed from groupchat because you were removed from the group or lost the permission required to groupchat");
					}
				}
				chatman.broadcastMessage(sender, chatMessage, asyncPlayerChatEvent.getFormat(),
						asyncPlayerChatEvent.getRecipients());
			}
		}.runTask(CivChat2.getInstance());
	}
}
