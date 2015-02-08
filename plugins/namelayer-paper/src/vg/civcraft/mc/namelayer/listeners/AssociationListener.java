package vg.civcraft.mc.namelayer.listeners;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.database.AssociationList;
import vg.civcraft.mc.namelayer.misc.ClassHandler;
import vg.civcraft.mc.namelayer.misc.ProfileInterface;

public class AssociationListener implements Listener{
	
	private AssociationList associations = NameAPI.getAssociationList();
	private ClassHandler ch = ClassHandler.ch;
	private ProfileInterface game;
	
	public AssociationListener(){
		if (ch != null)
			game = ch.getProfileClass();
	}
	
	private String packageName = getClass().getPackage().getName();
	private String version = packageName.substring(packageName.lastIndexOf('.') + 1);
	@EventHandler(priority=EventPriority.LOWEST)
	public void OnPlayerLogin(PlayerJoinEvent event)
	{
		String playername = event.getPlayer().getName();
		UUID uuid = event.getPlayer().getUniqueId();
		associations.addPlayer(playername, uuid);
		if (game != null)
			game.setPlayerProfle(event.getPlayer());
		event.setJoinMessage(ChatColor.YELLOW + NameAPI.getCurrentName(uuid) + " joined the game");
	}
}
