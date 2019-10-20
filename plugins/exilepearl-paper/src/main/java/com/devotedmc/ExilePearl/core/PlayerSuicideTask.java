package com.devotedmc.ExilePearl.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.SuicideHandler;
import com.devotedmc.ExilePearl.config.PearlConfig;

import static vg.civcraft.mc.civmodcore.util.TextUtil.*;

/**
 * Lets exiled players kill themselves with a timer
 * @author Gordon
 */
final class PlayerSuicideTask extends ExilePearlTask implements SuicideHandler {

	private int timeout = 180;

	class SuicideRecord {
		public final Location location;
		public int seconds;

		public SuicideRecord(final Location location, final int seconds) {
			this.location = location;
			this.seconds = seconds;
		}
	}

	private final HashMap<UUID, SuicideRecord> players = new HashMap<UUID, SuicideRecord>();
	private final HashSet<UUID> toRemove = new HashSet<UUID>();

	/**
	 * Creates a new FactoryWorker instance
	 */
	public PlayerSuicideTask(final ExilePearlApi pearlApi) {
		super(pearlApi);
	}

	@Override
	public String getTaskName() {
		return "Player Suicide";
	}


	@Override
	public int getTickInterval() {
		return TICKS_PER_SECOND;
	}


	@Override
	public void run() {
		if (!enabled) {
			return;
		}

		toRemove.clear();

		for (Entry<UUID, SuicideRecord> rec : players.entrySet()) {
			Player p = pearlApi.getPlayer(rec.getKey());
			int seconds = rec.getValue().seconds - 1;
			rec.getValue().seconds = seconds;

			if (seconds > 0) {
				// Notify every 10 seconds and last 5 seconds
				if (seconds <= 5 || seconds % 10 == 0) {
					msg(p, Lang.suicideInSeconds, seconds);
				}
			} else {
				toRemove.add(rec.getKey());
			}
		}

		for(UUID uid : toRemove) {
			players.remove(uid);
			Player p = pearlApi.getPlayer(uid);
			if (p != null) {
				p.setHealth(0);
			}
		}
	}


	@Override
	public void addPlayer(Player player) {
		players.put(player.getUniqueId(), new SuicideRecord(player.getLocation(), timeout));
		msg(player, Lang.suicideInSeconds, timeout);
	}


	@Override
	public boolean isAdded(UUID uid) {
		return players.containsKey(uid);
	}

	/**
	 * Cancels a pending suicide if the player moves
	 * @param e
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent e) {
		SuicideRecord rec = players.get(e.getPlayer().getUniqueId());
		if (rec != null && e.getTo().distance(rec.location) > 2) {
			players.remove(e.getPlayer().getUniqueId());
			msg(e.getPlayer(), Lang.suicideCancelled);
		}
	}

	@Override
	public void loadConfig(PearlConfig config) {
		this.timeout = pearlApi.getPearlConfig().getSuicideTimeoutSeconds();
	}
}
