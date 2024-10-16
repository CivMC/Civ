package com.programmerdan.minecraft.civspy;

import java.util.UUID;

/**
 * A data sample; can be destined for aggregation or stand alone.
 * 
 * Use one of the definite abstract subclasses.
 * 
 * @see PeriodicDataSample
 * @see PointDataSample
 * 
 * @author ProgrammerDan
 */
public abstract class DataSample {
	private final String valueString;
	private final Number valueNumber;

	private final long timestamp;

	private final DataSampleKey key;

	DataSample(String key, String server, String world, UUID player, Integer chunkX, Integer chunkZ, Number valueNumber) {
		this(key, server, world, player, chunkX, chunkZ, null, valueNumber);
	}
	DataSample(String key, String server, String world, UUID player, Integer chunkX, Integer chunkZ, String valueString) {
		this(key, server, world, player, chunkX, chunkZ, valueString, null);
	}
	DataSample(String key, String server, String world, UUID player, Integer chunkX, Integer chunkZ, String valueString, Number valueNumber) {
		this.timestamp = System.currentTimeMillis();
		this.valueString = valueString;
		this.valueNumber = valueNumber;
		this.key = new DataSampleKey(server, world, player, chunkX, chunkZ, key);
	}
	
	public abstract boolean forAggregate();

	public DataSampleKey getKey() {
		return key;
	}

	public String getValueString() {
		return valueString;
	}

	public Number getValueNumber() {
		return valueNumber;
	}

	public long getTimestamp() {
		return this.timestamp;
	}

	/**
	 * Convenient betweenness test for aggregation purposes;
	 * inclusive of start, exclusive of end.
	 */
	public boolean isBetween(long start, long end) {
		return this.timestamp >= start && this.timestamp < end;
	}
}

