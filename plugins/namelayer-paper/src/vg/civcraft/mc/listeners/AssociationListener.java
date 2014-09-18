package vg.civcraft.mc.listeners;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import vg.civcraft.mc.NameAPI;
import vg.civcraft.mc.database.AssociationList;

public class AssociationListener implements Listener{
	
	private AssociationList associations = NameAPI.getAssociationList();
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void OnPlayerLogin(AsyncPlayerPreLoginEvent event)
	{
		String playername = event.getName();
		UUID uuid = event.getUniqueId();
		associations.addPlayer(playername, uuid);
	}
}
