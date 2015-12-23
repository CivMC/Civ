package vg.civcraft.mc.civchat2.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.mercury.events.AsyncPluginBroadcastMessageEvent;
import vg.civcraft.mc.namelayer.group.Group;

public class MercuryMessageListener implements Listener {
	private static CivChat2 cc;
	
	
	
	public MercuryMessageListener(CivChat2 cc2) {
		cc = cc2;
	}



	@EventHandler
	public void handleMessage(AsyncPluginBroadcastMessageEvent event){
		if (!event.getChannel().equals("civchat2")){return;}
		
		//This separator needs to be changed to load from config. It is a regex, so care must be taken to ecape properly.
		String sep = "\\|";
		String[] message = event.getMessage().split(sep);
		
		if (message[0].equalsIgnoreCase("pm")){
			UUID receiverUUID = cc.getCivChat2Manager().getPlayerUUID(message[2]);
			if (receiverUUID == null){
				return;
			}
			Player receiver = Bukkit.getPlayer(receiverUUID);
			if (receiver == null || !receiver.isOnline()){
				return;
			}
			cc.getCivChat2Manager().receivePrivateMsgAcrossShards(message[1], receiver, message[3]);
		}
		else if(message[0].equalsIgnoreCase("gc")) {
			CivChat2Manager man = cc.getCivChat2Manager();
			String senderName = message[1];
			String groupName = message [2];
			String gcMessage = message [3];
			man.sendGroupMsgFromOtherShard(senderName, groupName, gcMessage);
		}
		else if(message[0].equalsIgnoreCase("msg")){
			UUID receiverUUID = cc.getCivChat2Manager().getPlayerUUID(message[1]);
			if (receiverUUID == null){
				return;
			}
			Player receiver = Bukkit.getPlayer(receiverUUID);
			if (receiver == null || !receiver.isOnline()){
				return;
			}
			receiver.sendMessage(message[2]);
		} else if(message[0].equalsIgnoreCase("ignore") || message[0].equalsIgnoreCase("unignore")){
			cc.getDatabaseManager().processMercuryInfo(message);
		}
		
	}

}
