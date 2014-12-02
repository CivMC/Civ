package vg.civcraft.mc.namelayer.listeners;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.database.AssociationList;

public class AssociationListener implements Listener{
	
	private AssociationList associations = NameAPI.getAssociationList();
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void OnPlayerLogin(PlayerJoinEvent event)
	{
		String playername = event.getPlayer().getName();
		UUID uuid = event.getPlayer().getUniqueId();
		associations.addPlayer(playername, uuid);
	}
}
