package com.programmerdan.minecraft.civspy;

/**
 * Represents a scheduled sampler that feeds data into the aggregator (DataManager)
 */
public abstract DataSampler implements Runnable {

	/**
	 * Target for all messages to enqueue against.
	 */
	private final DataManager target;

	private final Logger logger; 

	DataSampler(final DataManager target, final Logger logger) {
		this.target = target;
		this.logger = logger;
	}

	private boolean isActive = false;

	private InactiveReason inactiveReason = InactiveReason.NEW;

	/**
	 * Be sure to activate immediately (or when safe) to ensure no lost message.
	 */
	public void activate() {
		isActive = true;
		inactiveReason = InactiveReason.NOT_INACTIVE;
	}

	/**
	 * Please note that while inactive, anything this would have otherwise sampled will be lost.
	 */
	public void deactivate() {
		isActive = false;
		inactiveReason = InactiveReason.REQUEST;
	}

	public void run() {
		if (isActive) {
			try {
				this.sample();
			} catch ( Exception e ) {
				logger.log(Level.SEVERE, "Uncaught exception while sampling!", e);
				isActive = false;
				inactiveReason = InactiveReason.ERROR;
			}
		}
	}

	public enum InactiveReason {
		NEW,
		REQUEST,
		ERROR,
		NOT_INACTIVE
	}
}

