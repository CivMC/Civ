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
import vg.civcraft.mc.namelayer.group.Group;

/*
 * @author jjj5311
 * 
 */

public class CivChat2Listener implements Listener {
	
	private CivChat2Manager chatman;
	
	public CivChat2Listener(CivChat2Manager instance){
		chatman = instance;
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
		
		if(!(chatChannel == null)){
			Player receive = Bukkit.getPlayerExact(chatChannel);
			
			if(!(receive == null)){
				if(chatman.isIgnoringGroup(sender.getName(), chatChannel)){
					String muteMessage = ChatColor.YELLOW + chatChannel + ChatColor.RED + " has muted you";
					sender.sendMessage(muteMessage);
					return;
				}
				else{
					chatman.sendPrivateMsg(sender, receive, chatMessage);
					return;
				}
			}
			else{
				chatman.removeChannel(sender.getName());
				String offlineMessage = ChatColor.GOLD + "The player you were chatting with has gone offline,"
						+ " you have been moved to regular chat";
				sender.sendMessage(offlineMessage);
				return;
			}
		}
		
		chatman.broadcastMessage(sender, chatMessage, asyncPlayerChatEvent.getRecipients());
	}
	
	
	
	

}
