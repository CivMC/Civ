package vg.civcraft.mc.civmodcore.util.cooldowns;

import java.util.HashMap;
import java.util.Map;

/**
 * Cooldown implementation that keeps track of objects in milliseconds. The value given in the constructor is assumed to
 * be in milliseconds and time stamps are stored as unix timestamp
 *
 * @param <E>
 *            Object that cooldowns are assigned to
 * @author Maxopoly
 */
public class MilliSecCoolDownHandler<E> implements ICoolDownHandler<E> {

	private Map<E, Long> cds;

	private long cooldown;

	public MilliSecCoolDownHandler(long cooldown) {
		this.cooldown = cooldown;
		cds = new HashMap<>();
	}

	@Override
	public void putOnCoolDown(E e) {
		cds.put(e, System.currentTimeMillis());
	}

	@Override
	public boolean onCoolDown(E e) {
		Long lastUsed = cds.get(e);
		if (lastUsed == null || (System.currentTimeMillis() - lastUsed) > cooldown) {
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
		long leftOver = System.currentTimeMillis() - lastUsed;
		if (leftOver < cooldown) {
			return cooldown - leftOver;
		}
		return 0;
	}

	@Override
	public long getTotalCoolDown() {
		return cooldown;
	}
	
	@Override
	public void removeCooldown(E e) {
		cds.remove(e);
	}

}
