package com.jjj5311.minecraft.civchat2.listener;

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

import vg.civcraft.mc.namelayer.group.Group;

import com.jjj5311.minecraft.civchat2.CivChat2Manager;

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
		playerDeathEvent.setDeathMessage(null);
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent playerQuitEvent){
		playerQuitEvent.setQuitMessage(null);
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent playerJoinEvent){
		playerJoinEvent.setJoinMessage(null);
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerKick(PlayerKickEvent playerKickEvent){
		playerKickEvent.setLeaveMessage(null);
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void PlayerChatEvent(AsyncPlayerChatEvent asyncPlayerChatEvent){
		asyncPlayerChatEvent.setCancelled(true);
		
		String chatMessage = asyncPlayerChatEvent.getMessage();
		Player sender = asyncPlayerChatEvent.getPlayer();
		String chatChannel = chatman.getChannel(sender.getName());
		Group chatGroup = chatman.getGroupChat(sender.getName());
		
		if(!(chatChannel == null)){
			Player receive = Bukkit.getPlayerExact(chatChannel);
			
			if(!(receive == null)){
				if(chatman.isIgnoring(sender.getName(), chatChannel)){
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
		if(!(chatGroup == null)){
			chatman.groupChat(chatGroup, chatMessage, sender.getName());
			return;
		}
		
		chatman.broadcastMessage(sender, chatMessage, asyncPlayerChatEvent.getRecipients());
	}
	
	
	
	

}
