package com.devotedmc.ExilePearl.core;

import com.devotedmc.ExilePearl.ExilePearlApi;

/**
 * Interval task that deducts strength from existing prison pearls
 * @author Gordon
 */
final class PearlDecayTask extends ExilePearlTask {

	/**
	 * Creates a new FactoryWorker instance
	 */
	public PearlDecayTask(final ExilePearlApi pearlApi) {
		super(pearlApi);
	}


	@Override
	public String getTaskName() {
		return "Pearl Decay";
	}


	@Override
	public int getTickInterval() {
		return pearlApi.getPearlConfig().getPearlHealthDecayIntervalMin() * TICKS_PER_MINUTE;
	}


	@Override
	public void run() {
		if (!enabled) {
			return;
		}
		
		pearlApi.decayPearls();
	}
}
