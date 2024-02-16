package vg.civcraft.mc.citadel.listener;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import vg.civcraft.mc.citadel.activity.ActivityMap;

public class ActivityListener implements Listener {

	private final ActivityMap map;

	public ActivityListener(ActivityMap map) {
		this.map = map;
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Location from = event.getFrom();
		Location to = event.getTo();

		if (to == null || from == null || !event.hasChangedBlock()) {
			return;
		}

		map.savePlayerActivity(from, to, event.getPlayer());
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Location location = event.getPlayer().getLocation();
		map.savePlayerActivity(location, event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChunkLoad(ChunkLoadEvent e) {
		map.loadChunk(e.getChunk());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChunkUnload(ChunkUnloadEvent e) {
		map.unloadChunk(e.getChunk());
	}
}
