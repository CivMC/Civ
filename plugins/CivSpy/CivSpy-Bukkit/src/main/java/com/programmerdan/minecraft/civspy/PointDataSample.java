package com.programmerdan.minecraft.civspy;

import java.util.UUID;

/**
 * Simple implementation of a DataSample, this encapsulates an event's point of data, done on event or demand.
 * This data is meant to be aggregated into a "time bucket" before database insertion.
 */
public class PointDataSample extends DataSample {

	public PointDataSample(String key, String server, String world, UUID player, Integer chunkX, Integer chunkZ, 
			String valueString, Number valueNumber) {
		super(key, server, world, player, chunkX, chunkZ, valueString, valueNumber);
	}

	@Override
	public boolean forAggregate() {
		return true;
	}
}


