package com.untamedears.JukeAlert.listener;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;




import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.mercury.events.AsyncPluginBroadcastMessageEvent;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;

import com.untamedears.JukeAlert.external.Mercury;
import com.untamedears.JukeAlert.util.Utility;

public class MercuryListener implements Listener{

	private List<String> channels = new ArrayList<String>();
	private GroupManager gm = NameAPI.getGroupManager();
	
	public MercuryListener(){
		MercuryAPI.instance.registerPluginMessageChannel(Mercury.getChannels());
		for (String x: Mercury.getChannels())
			channels.add(x);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void asyncMercuryMessageEvent(AsyncPluginBroadcastMessageEvent event){
		String channel = event.getChannel();
		if (!channels.contains(channel))
			return;
		String m = event.getMessage();
		String[] comp = m.split(" ");
		// split the message into the different parts.
		// They are all realistically the same effect except that how it gets sent to player.
		StringBuilder message = new StringBuilder();
		for (int x = 1; x < comp.length; x++)
			message.append(comp[x]+" ");
		
		try {
			Utility.notifyGroup(gm.getGroup(comp[0]), ChatColor.AQUA+message.toString());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
