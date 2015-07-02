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

import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.mercury.MercuryPlugin;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;

/*
 * @author jjj5311
 * 
 */

public class CivChat2Listener implements Listener {
	
	private CivChat2Manager chatman;
	private GroupManager gm;
	
	public CivChat2Listener(CivChat2Manager instance){
		chatman = instance;
		gm = NameAPI.getGroupManager();
	}
	
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerDeath(PlayerDeathEvent playerDeathEvent){
		CivChat2.debugmessage("PlayerDeathEvent occured");
		playerDeathEvent.setDeathMessage(null);
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent playerQuitEvent){
		CivChat2.debugmessage("playerQuitEvent occured");
		playerQuitEvent.setQuitMessage(null);
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent playerJoinEvent){
		CivChat2.debugmessage("playerJoinEvent occured");
		playerJoinEvent.setJoinMessage(null);
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerKick(PlayerKickEvent playerKickEvent){
		CivChat2.debugmessage("playerKickEvent occured");
		playerKickEvent.setLeaveMessage(null);
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void PlayerChatEvent(AsyncPlayerChatEvent asyncPlayerChatEvent){
		CivChat2.debugmessage("PlayerChatEvent occured");
		asyncPlayerChatEvent.setCancelled(true);
		
		String chatMessage = asyncPlayerChatEvent.getMessage();
		Player sender = asyncPlayerChatEvent.getPlayer();
		String chatChannel = chatman.getChannel(sender.getName());
		String groupChat = chatman.getGroupChatting(sender.getName());
		
		CivChat2.debugmessage(String.format("ChatEvent properties: chatMessage =[ %s ], sender = [ %s ], chatChannel = [ %s ], groupchatting = [ %s ];", chatMessage, sender.getName(), chatChannel, groupChat));
		if(!(chatChannel == null)){
			StringBuilder sb = new StringBuilder();
			CivChat2.debugmessage("PlayerChatEvent chatChannel does not equal null");
			Player receive = Bukkit.getPlayer(NameAPI.getUUID(chatChannel));
			CivChat2.debugmessage("player chat event receive = [" + receive + "]");
			if(!(receive == null)){	
				if(chatman.isIgnoringPlayer(sender.getName(), chatChannel)){
					CivChat2.debugmessage("PlayerChatEvent receive != null isIgnoringGroups is true");
					String muteMessage = sb.append(ChatColor.YELLOW) 
											.append( chatChannel) 
											.append( ChatColor.RED) 
											.append( " has muted you")
											.toString();
					sb.delete(0, sb.length());
					sender.sendMessage(muteMessage);
					return;
				}
				else{
					CivChat2.debugmessage("PlayerChatEvent chatman.sendPrivateMessage being sent");
					chatman.sendPrivateMsg(sender, receive, chatMessage);
					return;
				}
			}
			else{
				if (CivChat2.getInstance().isMercuryEnabled()){
					if (NameLayerPlugin.getOnlineAllServers().containsKey(chatChannel)){
						//This separator needs to be changed to load from config.
						String sep = "|";
						MercuryPlugin.handler.sendMessage(NameLayerPlugin.getOnlineAllServers().get(chatChannel.toLowerCase()), "civchat2", "pm"+sep+sender.getName()+sep+chatChannel+sep+chatMessage.replace(sep, ""));
						sender.sendMessage(ChatColor.LIGHT_PURPLE+"To "+chatChannel+": "+chatMessage);
						return;
					}
				}
				chatman.removeChannel(sender.getName());
				String offlineMessage = sb.append(ChatColor.GOLD )
											.append( "The player you were chatting with has gone offline,")
											.append(" you have been moved to regular chat").toString();
				sb.delete(0, sb.length());
				sender.sendMessage(offlineMessage);
				return;
			}
		}
		if(!(groupChat == null)){
			//player is group chatting
			chatman.sendGroupMsg(sender.getName(), chatMessage, GroupManager.getGroup(groupChat));
			return;
		}
		
		CivChat2.debugmessage("PlayerChatEvent calling chatman.broadcastMessage()");
		chatman.broadcastMessage(sender, chatMessage, asyncPlayerChatEvent.getRecipients());
	}
	
	
	
	

}
