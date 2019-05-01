package vg.civcraft.mc.civmodcore.util.cooldowns;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Cooldown implementation that keeps track of objects in ticks. The value given in the constructor is assumed to be in
 * ticks and time stamps are stored as a tick timestamp, which is powered by an internal counter that's incremented
 * every tick
 *
 * @param <E>
 *            Object that cooldowns are assigned to
 * @author Maxopoly
 */
public class TickCoolDownHandler<E> implements ICoolDownHandler<E> {

	private Map<E, Long> cds;

	private long cooldown;

	private long tickCounter;

	public TickCoolDownHandler(JavaPlugin executingPlugin, long cooldown) {
		this.cooldown = cooldown;
		cds = new HashMap<>();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(executingPlugin, new Runnable() {

			@Override
			public void run() {
				tickCounter++; // increment every tick
			}
		}, 1L, 1L);
	}

	@Override
	public void putOnCoolDown(E e) {
		cds.put(e, tickCounter);
	}

	@Override
	public boolean onCoolDown(E e) {
		Long lastUsed = cds.get(e);
		if (lastUsed == null || (tickCounter - lastUsed) > cooldown) {
			return false;
		}
		return true;
	}

	@Override
	public long getRemainingCoolDown(E e) {
		Long lastUsed = cds.get(e);
		if (lastUsed == null) {
			return 0L;
		}
		long leftOver = tickCounter - lastUsed;
		if (leftOver < cooldown) {
			return cooldown - leftOver;
		}
		return 0L;
	}

	@Override
	public long getTotalCoolDown() {
		return cooldown;
	}

}
