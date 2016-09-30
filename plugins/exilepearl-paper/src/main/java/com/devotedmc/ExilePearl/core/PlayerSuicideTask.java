package com.devotedmc.ExilePearl.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.PearlPlayer;
import com.devotedmc.ExilePearl.SuicideHandler;
import com.devotedmc.ExilePearl.util.Guard;

/**
 * Lets exiled players kill themselves with a timer
 * @author Gordon
 */
class PlayerSuicideTask implements Runnable, SuicideHandler {

	private final ExilePearlApi pearlApi;

	private boolean enabled = false;
	private int taskId = 0;

	public static final int TICKS_PER_SECOND = 20;
	
	class SuicideRecord {
		public final PearlPlayer player;
		public int seconds;
		
		public SuicideRecord(final PearlPlayer player, final int seconds) {
			this.player = player;
			this.seconds = seconds;
		}
	};
	
	private final HashMap<UUID, SuicideRecord> players = new HashMap<UUID, SuicideRecord>();
	private final HashSet<Player> toRemove = new HashSet<Player>();

	/**
	 * Creates a new FactoryWorker instance
	 */
	public PlayerSuicideTask(final ExilePearlApi pearlApi) {
		Guard.ArgumentNotNull(pearlApi, "pearlApi");

		this.pearlApi = pearlApi;
	}


	/**
	 * Starts the worker task
	 */
	public void start() {
		if (enabled) {
			pearlApi.log(Level.WARNING, "Tried to start the player suicide task but it was already started.");
			return;
		}
		
		taskId = pearlApi.getScheduler().scheduleSyncRepeatingTask(pearlApi.getPlugin(), this, TICKS_PER_SECOND, TICKS_PER_SECOND);
		if (taskId == -1) {
			pearlApi.log(Level.SEVERE, "Failed to start the player suicide task");
			return;
		} else {
			players.clear();
			enabled = true;
			pearlApi.log("Started the player suicide task");
		}
	}

	/**
	 * Stops the worker task
	 */
	public void stop() {
		if (enabled) {
			pearlApi.getScheduler().cancelTask(taskId);
			enabled = false;
			taskId = 0;
			pearlApi.log("Stopped the player suicide task");
		}
	}

	/**
	 * Restarts the worker task
	 */
	public void restart() {
		stop();
		start();
	}


	@Override
	public boolean isRunning() {
		return enabled;
	}


	@Override
	public void run() {
		if (!enabled) {
			return;
		}
		
		toRemove.clear();
		
		for (SuicideRecord rec : players.values()) {
			rec.seconds--;
			
			if (rec.seconds > 0) {
				// Notify every 10 seconds and last 5 seconds
				if (rec.seconds <= 5 || rec.seconds % 10 == 0) {
					rec.player.msg(Lang.suicideInSeconds, rec.seconds);
				}
			} else {
				toRemove.add(rec.player);
			}
		}
		
		for(Player p : toRemove) {
			players.remove(p.getUniqueId());
			p.setHealth(0);
		}
	}


	@Override
	public void addPlayer(PearlPlayer player) {
		int timeout = pearlApi.getPearlConfig().getSuicideTimeoutSeconds();
		players.put(player.getUniqueId(), new SuicideRecord(player, timeout));
		player.msg(Lang.suicideInSeconds, timeout);
	}


	@Override
	public boolean isAdded(Player player) {
		return players.containsKey(player.getUniqueId());
	}
	
	/**
	 * Cancels a pending suicide if the player moves
	 * @param e
	 */
	public void onPlayerMove(PlayerMoveEvent e) {
		if (players.containsKey(e.getPlayer().getUniqueId())) {
			players.remove(e.getPlayer().getUniqueId()).player.msg(Lang.suicideCancelled);
		}
	}
}
