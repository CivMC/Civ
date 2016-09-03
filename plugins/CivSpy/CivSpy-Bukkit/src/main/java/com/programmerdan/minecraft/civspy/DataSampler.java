package com.programmerdan.minecraft.civspy;

import java.util.logging.Logger;

/**
 * Represents a scheduled sampler that feeds data into the aggregator {@link DataManager}.
 * 
 * Not recommended to subclass this; if you do, follow the instructions found in 
 * {@link com.programmerdan.minecraft.civspy.samplers} package docs.
 * 
 * @see MultiDataSampler
 * @see SingleDataSampler
 * 
 * @author ProgrammerDan
 */
abstract class DataSampler implements Runnable {

	/**
	 * Target for all messages to enqueue against.
	 */
	protected final DataManager target;

	/**
	 * Logger for subclasses to use.
	 */
	protected final Logger logger; 

	private boolean isActive = false;

	private InactiveReason inactiveReason = InactiveReason.NEW;

	/**
	 * How long between executions of this sampler?
	 * Note that once the sampler is scheduled, this attribute is ignored.
	 */
	private long period;

	/**
	 * Basic constructor.
	 * 
	 * @param target The DataManager to send data to
	 * @param logger a Logger for subclasses to use to log errors and status to.
	 */
	public DataSampler(final DataManager target, final Logger logger) {
		this.target = target;
		this.logger = logger;
		this.period = 60000l;
	}

	/**
	 * Can be used by subclasses to redefine the period during construction.
	 * 
	 * @param period The new period. Only used during construction.
	 */
	protected void setPeriod(long period) {
		this.period = period;
	}

	/**
	 * Returns the period inbetween executions of Sampler.
	 * 
	 * @return The period as currently defined.
	 */
	public long getPeriod() {
		return this.period;
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
	 * To ensure orderly submission of samples, don't subclass DataSampler directly,
	 * use one of the previously defined abstract subclasses that offer a <code>sample</code>
	 * method instead.
	 * 
	 * @see MultiDataSampler
	 * @see SingleDataSampler
	 */
	public abstract void run();
	
	enum InactiveReason {
		NEW,
		REQUEST,
		ERROR,
		NOT_INACTIVE
	}
}

