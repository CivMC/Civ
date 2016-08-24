package com.programmerdan.minecraft.civspy;

/**
 * Simple implementation of a DataSample, this encapuslates a server/world/player (max granularity) sampling of data, done on a distinct period.
 * As it is sampled and not event driven, this is data not meant to be aggregated by CivSpy, as it is best presented using histograms
 * and statistic analysis.
 *
 * Ideally the period should be stable.
 */
public class PeriodicDataSample extends DataSample {

	public PeriodicDataSample(String key, String server, String world, UUID player, Number valueNumber) {
		super(key, server, world, player, null, null, valueNumber);
	}

	@Override
	public boolean forAggregate() {
		return false;
	}
}

