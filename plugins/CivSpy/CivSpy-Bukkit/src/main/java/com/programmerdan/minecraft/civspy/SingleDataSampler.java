package com.programmerdan.minecraft.civspy;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple single-sampling sampler. Used for very basic data queries.
 * 
 * Advanced implementers could use this as a base class for their Samplers, but remember
 *  that the Sampler detection code expects DataManager, Logger, String at minimum.
 *
 * @author ProgrammerDan
 *
 */
public abstract class SingleDataSampler extends DataSampler {

	public SingleDataSampler(DataManager target, Logger logger) {
		super(target, logger);
	}

	/**
	 * Subclasses should use this; does the actual sampling
	 * 
	 * @return a DataSample to enqueue
	 */
	public abstract DataSample sample();

	/**
	 * Managed run pattern, handles calling {@link #sample} and passing the results to the Manager.
	 */
	@Override
	public void run() {
		if (isActive()) {
			try {
				DataSample data = this.sample();
				if (data != null) {
					target.enqueue(data);
				}
			} catch ( Exception e ) {
				logger.log(Level.SEVERE, "Uncaught exception while sampling!", e);
				inError();
			}
		}
	}

}
