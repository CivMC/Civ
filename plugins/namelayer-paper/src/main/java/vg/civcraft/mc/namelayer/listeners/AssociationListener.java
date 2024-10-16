package vg.civcraft.mc.namelayer.listeners;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import vg.civcraft.mc.namelayer.MojangNames;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.database.AssociationList;
import vg.civcraft.mc.namelayer.misc.ClassHandler;
import vg.civcraft.mc.namelayer.misc.NameCleanser;
import vg.civcraft.mc.namelayer.misc.ProfileInterface;

public class AssociationListener implements Listener {

	private AssociationList associations;

	private ClassHandler ch;

	private ProfileInterface game;

	public AssociationListener() {
		Bukkit.getScheduler().runTaskLater(NameLayerPlugin.getInstance(), new Runnable() {

			@Override
			public void run() {
				ch = ClassHandler.ch;
				if (ClassHandler.properlyEnabled)
					game = ch.getProfileClass();

				associations = NameAPI.getAssociationList();
			}

		}, 1);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void OnPlayerJoin(PlayerJoinEvent event) {
		String playername = event.getPlayer().getName();
		if(NameCleanser.isDirty(playername)) {
			if(NameCleanser.isAlertOps()) {
				String msg = playername + " has a dirty name";
				if(NameCleanser.isCleanNames()) {
					msg += ", this will be fixed";
				}
				Bukkit.broadcast(msg, NameCleanser.getAlertPerm());
			}
			if(NameCleanser.isCleanNames()) {
				playername = NameCleanser.cleanName(playername);
			}
		}
		UUID uuid = event.getPlayer().getUniqueId();
		associations.addPlayer(playername, uuid);
		event.setJoinMessage(ChatColor.YELLOW + NameAPI.getCurrentName(uuid) + " joined the game");
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void OnPlayerLogin(PlayerLoginEvent event) {
		final Player player = event.getPlayer();
		MojangNames.declareMojangName(player.getUniqueId(), player.getName());
		String name = associations.getCurrentName(player.getUniqueId());
		if (name == null)
		{
			associations.addPlayer(player.getName(), player.getUniqueId());
			name = associations.getCurrentName(player.getUniqueId());
		}

		if (game != null)
			game.setPlayerProfile(player, name);
	}
}
