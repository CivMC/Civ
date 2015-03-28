package vg.civcraft.mc.namelayer.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import vg.civcraft.mc.mercury.events.AsyncPluginBroadcastMessageEvent;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.events.GroupDeleteEvent;
import vg.civcraft.mc.namelayer.events.GroupMergeEvent;
import vg.civcraft.mc.namelayer.events.GroupTransferEvent;
import vg.civcraft.mc.namelayer.events.PromotePlayerEvent;
import vg.civcraft.mc.namelayer.group.Group;

public class MercuryMessageListener implements Listener{
	
	private GroupManager gm = NameAPI.getGroupManager();
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMercuryMessage(AsyncPluginBroadcastMessageEvent event){
		String channel = event.getChannel();
		if (!channel.equals("name_layer_refresh"))
			return;
		String[] message = event.getMessage().split(" ");
		String reason = message[0];
		String group = message[1];
		Group g = gm.getGroup(group);
		if (reason.equals("recache")){
			gm.invalidateCache(g.getName());
		}
		else if (reason.equals("delete")){
			GroupDeleteEvent e = new GroupDeleteEvent(g, true);
			Bukkit.getPluginManager().callEvent(e);
			gm.invalidateCache(g.getName());
		}
		else if (reason.equals("merge")){
			GroupMergeEvent e = new GroupMergeEvent(g, gm.getGroup(message[2]), true);
			Bukkit.getPluginManager().callEvent(e);
			gm.invalidateCache(g.getName());
			gm.invalidateCache(message[2]);
		}
		else if (reason.equals("transfer")){
			GroupTransferEvent e = new GroupTransferEvent(g, UUID.fromString(message[2]));
			Bukkit.getPluginManager().callEvent(e);
			gm.invalidateCache(g.getName());
		}
	}

}
