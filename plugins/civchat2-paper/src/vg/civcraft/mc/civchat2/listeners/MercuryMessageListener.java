package vg.civcraft.mc.civchat2.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.mercury.events.AsyncPluginBroadcastMessageEvent;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;

public class MercuryMessageListener extends BukkitRunnable implements Listener {
	private static CivChat2 cc;
	
	
	@EventHandler
	public void handleMessage(AsyncPluginBroadcastMessageEvent event){
		if (!event.getChannel().equals("civchat2")){return;}
		
		String[] message = event.getMessage().split("~|");
		if (message[0].equals("pm")){
			UUID receiverUUID = cc.getCivChat2Manager().getPlayerUUID(message[2]);
			Player receiver = Bukkit.getPlayer(receiverUUID);
			if (receiver == null || !receiver.isOnline()){return;
			} else {
				UUID senderUUID = cc.getCivChat2Manager().getPlayerUUID(message[1]);
				Player sender = Bukkit.getPlayer(senderUUID);
				cc.getCivChat2Manager().sendPrivateMsg(sender, receiver, message[3]);
			}
			

		}
		
	}

	@Override
	public void run() {
		MercuryMessageListener.cc = CivChat2.getInstance();
		cc.setMercuryEnabled(cc.getServer().getPluginManager().isPluginEnabled("Mercury"));
		if (cc.isMercuryEnabled()){
			cc.getServer().getPluginManager().registerEvents(this, cc);
		}
	}
}
