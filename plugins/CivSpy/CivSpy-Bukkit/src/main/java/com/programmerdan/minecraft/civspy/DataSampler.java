package com.programmerdan.minecraft.civspy;

import java.util.logging.Logger;

/**
 * Represents a scheduled sampler that feeds data into the aggregator (DataManager)
 */
abstract class DataSampler implements Runnable {

	/**
	 * Target for all messages to enqueue against.
	 */
	protected final DataManager target;

	protected final Logger logger; 

	private boolean isActive = false;

	private InactiveReason inactiveReason = InactiveReason.NEW;

	public DataSampler(final DataManager target, final Logger logger) {
		this.target = target;
		this.logger = logger;
	}

	/**
	 * Be sure to activate immediately (or when safe) to ensure no lost message.
	 */
	public final void activate() {
		isActive = true;
		inactiveReason = InactiveReason.NOT_INACTIVE;
	}

	/**
	 * Please note that while inactive, anything this would have otherwise sampled will be lost.
	 */
	public final void deactivate() {
		isActive = false;
		inactiveReason = InactiveReason.REQUEST;
	}
	
	protected final boolean isActive() {
		return this.isActive;
	}
	
	protected final void inError() {
		isActive = false;
		inactiveReason = InactiveReason.ERROR;
	}
	
	protected final InactiveReason whyInactive() {
		return this.inactiveReason;
	}

	/** 
	 * Two subclasses of Samplerimplement this.
	 */
	public abstract void run();
	
	enum InactiveReason {
		NEW,
		REQUEST,
		ERROR,
		NOT_INACTIVE
	}
}

