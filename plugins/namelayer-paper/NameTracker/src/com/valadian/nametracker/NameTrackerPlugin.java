package com.valadian.nametracker;

import java.io.IOException;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class NameTrackerPlugin extends JavaPlugin implements Listener {
	public static AssociationList associations;

	@Override
	public void onEnable() {
		associations = new AssociationList(getDataFolder());
	    getServer().getPluginManager().registerEvents(this, this);
	} 
	
	public void onDisable() {
		 try {
			associations.save();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void OnPlayerLogin(PlayerLoginEvent loginEvent)
	{
		Player player = loginEvent.getPlayer();
		associations.addPlayerName(player.getUniqueId(), player.getName());
		player.setDisplayName(associations.getCurrentName(player.getUniqueId()));
	}
//	 public void setPlayerName(EntityPlayer player, String newname)
//	 {
//		 WorldServer world = (WorldServer)player.world;
//		 EntityTracker tracker = world.tracker;
//		 tracker.untrackEntity(player);
//		 player.name = newname;
//		 tracker.track(player);
//	 }
}
