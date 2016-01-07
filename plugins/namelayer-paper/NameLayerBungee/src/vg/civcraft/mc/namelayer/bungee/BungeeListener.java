package vg.civcraft.mc.namelayer.bungee;

import java.util.UUID;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeListener implements Listener{

	private DataBaseManager db;
	private NameLayerBungee plugin;
	
	public BungeeListener(DataBaseManager db) {
		this.db = db;
		plugin = NameLayerBungee.getInstance();
	}
	
	@EventHandler
	public void postLoginEvent(PostLoginEvent event) {
		final ProxiedPlayer player = event.getPlayer();
		final UUID uuid = player.getUniqueId();
		db.addPlayer(player.getName(), uuid);
		String name = db.getCurrentName(uuid);
		player.setDisplayName(name);
	}
}
