package vg.civcraft.mc.namelayer.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.mercury.events.AsyncPluginBroadcastMessageEvent;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.events.GroupDeleteEvent;
import vg.civcraft.mc.namelayer.events.GroupInvalidationEvent;
import vg.civcraft.mc.namelayer.events.GroupMergeEvent;
import vg.civcraft.mc.namelayer.events.GroupTransferEvent;
import vg.civcraft.mc.namelayer.group.Group;

public class MercuryMessageListener implements Listener{
	
	private GroupManager gm = NameAPI.getGroupManager();
	private NameLayerPlugin nl = NameLayerPlugin.getInstance();
	
	public MercuryMessageListener() {
		MercuryAPI.instance.registerPluginMessageChannel("namelayer");
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMercuryMessage(AsyncPluginBroadcastMessageEvent event){
		if (!event.getChannel().equalsIgnoreCase("namelayer"))
			return;
		String[] message = event.getMessage().split(" ");
		String reason = message[0];	
		String group = message[1];
		if (reason.equals("recache")){
			GroupInvalidationEvent e = new GroupInvalidationEvent(reason, group);
			Bukkit.getPluginManager().callEvent(e);
			gm.invalidateCache(group);
		}
		else if (reason.equals("delete")){
			GroupInvalidationEvent e = new GroupInvalidationEvent(reason, group);
			Bukkit.getPluginManager().callEvent(e);
			gm.invalidateCache(group);
		}
		else if (reason.equals("merge")){
			GroupInvalidationEvent e = new GroupInvalidationEvent(reason, group, message[2]);
			Bukkit.getPluginManager().callEvent(e);
			gm.invalidateCache(group);
			gm.invalidateCache(message[2]);
		}
		else if (reason.equals("transfer")){
			GroupInvalidationEvent e = new GroupInvalidationEvent(reason, message[2]);
			Bukkit.getPluginManager().callEvent(e);
			gm.invalidateCache(group);
		}
	}

}
