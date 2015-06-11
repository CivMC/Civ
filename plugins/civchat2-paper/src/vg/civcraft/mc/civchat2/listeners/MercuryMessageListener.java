package vg.civcraft.mc.civchat2.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.mercury.events.AsyncPluginBroadcastMessageEvent;

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
			if (receiverUUID == null){return;}
			Player receiver = Bukkit.getPlayer(receiverUUID);
			if (receiver == null || !receiver.isOnline()){return;}
			if (!CivChat2.getInstance().getCivChat2Manager().isIgnoringPlayer(receiver.getName(), message[1])){
				receiver.sendMessage(ChatColor.LIGHT_PURPLE+"From "+message[1]+": "+message[3]);
			}
			

		}
		
	}

}
