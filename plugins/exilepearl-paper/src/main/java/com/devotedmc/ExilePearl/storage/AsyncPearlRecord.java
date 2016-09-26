package com.devotedmc.ExilePearl.storage;

import com.devotedmc.ExilePearl.ExilePearl;

/**
 * Storage class for async database writes 
 * @author Gordon
 */
class AsyncPearlRecord {
	
	/**
	 * The type of database write action
	 */
	public enum WriteType {
		TERMINATE,
		INSERT,
		REMOVE,
		UPDATE_LOCATION,
		UPDATE_HEALTH,
		UPDATE_FREED_OFFLINE
	}
	
	private final ExilePearl pearl;
	private final WriteType writeType;
	
	/**
	 * Creates a new AsyncPearlRecord instance
	 * @param pearl The pearl instance
	 * @param writeType The write type
	 */
	public AsyncPearlRecord(final ExilePearl pearl, final WriteType writeType) {		
		this.pearl = pearl;
		this.writeType = writeType;
	}
	
	/**
	 * Gets the pearl instance
	 * @return The pearl instance
	 */
	public ExilePearl getPearl() {
		return pearl;
	}
	
	/**
	 * Gets the write type
	 * @return The write type
	 */
	public WriteType getWriteType() {
		return writeType;
	}
}
