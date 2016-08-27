package com.programmerdan.minecraft.civspy;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a scheduled sampler that feeds data into the aggregator (DataManager)
 */
public abstract class DataSampler implements Runnable {

	/**
	 * Target for all messages to enqueue against.
	 */
	private final DataManager target;

	private final Logger logger; 

	private boolean isActive = false;

	private InactiveReason inactiveReason = InactiveReason.NEW;

	public DataSampler(final DataManager target, final Logger logger) {
		this.target = target;
		this.logger = logger;
	}

	/**
	 * Samplers should implement this method.
	 */
	public abstract DataSample sample();

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

	/**
	 * Managed run pattern, handles calling {@link #sample} and passing the results to the Manager.
	 */
	public void run() {
		if (isActive) {
			try {
				DataSample data = this.sample();
				if (data != null) {
					target.enqueue(data);
				}
			} catch ( Exception e ) {
				logger.log(Level.SEVERE, "Uncaught exception while sampling!", e);
				isActive = false;
				inactiveReason = InactiveReason.ERROR;
			}
		}
	}

	enum InactiveReason {
		NEW,
		REQUEST,
		ERROR,
		NOT_INACTIVE
	}
}

