package com.programmerdan.minecraft.simpleadminhacks.hacks;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_10_R1.EntityHuman;
import net.minecraft.server.v1_10_R1.EntityTracker;
import net.minecraft.server.v1_10_R1.EntityTrackerEntry;
import net.minecraft.server.v1_10_R1.WorldServer;

import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.configs.InvisibleFixTwoConfig;

/**
 * Adapted wholesale from: https://gist.github.com/aadnk/3773860
 *
 * @author aadnk
 * @author ProgrammerDan
 */
public class InvisibleFixTwo extends SimpleHack<InvisibleFixTwoConfig> implements Listener {
	public static final String NAME = "InvisibleFixTwo";

	private Server server;
	
	// Try increasing this. May be dependent on lag.
	private final int TELEPORT_FIX_DELAY = 15; // ticks
	
	public InvisibleFixTwo(SimpleAdminHacks plugin, InvisibleFixTwoConfig config) {
		super(plugin, config);
		this.server = plugin.getServer();

	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if (!config.isEnabled()) return; // ignore if off

		final Player player = event.getPlayer();
		final int visibleDistance = server.getViewDistance() * 16;
		
		// Fix the visibility issue one tick later
		server.getScheduler().scheduleSyncDelayedTask(plugin(), new Runnable() {
			@Override
			public void run() {
				// Refresh nearby clients
				updateEntities(getPlayersWithin(player, visibleDistance));
				
				plugin().debug("Applying fix ... {0}", visibleDistance);
			}
		}, TELEPORT_FIX_DELAY);
	}
	

	public void updateEntities(List<Player> observers) {
		
		// Refresh every single player
		for (Player player : observers) {
			updateEntity(player, observers);
		}
	}
	
	public void updateEntity(Entity entity, List<Player> observers) {
		World world = entity.getWorld();
		WorldServer worldServer = ((CraftWorld) world).getHandle();

		EntityTracker tracker = worldServer.tracker;
		EntityTrackerEntry entry = (EntityTrackerEntry) tracker.trackedEntities
				.get(entity.getEntityId());

		List<EntityHuman> nmsPlayers = getNmsPlayers(observers);

		// Force Minecraft to resend packets to the affected clients
		entry.trackedPlayers.removeAll(nmsPlayers);
		entry.scanPlayers(nmsPlayers);
	}

	private List<EntityHuman> getNmsPlayers(List<Player> players) {
		List<EntityHuman> nsmPlayers = new ArrayList<EntityHuman>();

		for (Player bukkitPlayer : players) {
			CraftPlayer craftPlayer = (CraftPlayer) bukkitPlayer;
			nsmPlayers.add(craftPlayer.getHandle());
		}

		return nsmPlayers;
	}
	
	private List<Player> getPlayersWithin(Player player, int distance) {
		List<Player> res = new ArrayList<Player>();
		int d2 = distance * distance;

		for (Player p : server.getOnlinePlayers()) {
			if (p.getWorld() == player.getWorld()
					&& p.getLocation().distanceSquared(player.getLocation()) <= d2) {
				res.add(p);
			}
		}

		return res;
	}

	@Override
	public void registerListeners() {
		if (config.isEnabled()) {
			plugin().log("Registering InvisibleFixTwo listener");
			plugin().registerListener(this);
		}
	}
	
	@Override
	public void registerCommands() {
	}

	@Override
	public void dataBootstrap() {
	}

	@Override
	public void unregisterListeners() {
	}

	@Override
	public void unregisterCommands() {
	}

	@Override
	public void dataCleanup() {
	}

	@Override
	public String status() {
		if (config != null && config.isEnabled()) {
			return "InvisibleFixTwo active";
		} else {
			return "InvisibleFixTwo not active";
		}
	}
	
	public static InvisibleFixTwoConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new InvisibleFixTwoConfig(plugin, config);
	}
}

