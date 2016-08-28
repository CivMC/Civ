package com.programmerdan.minecraft.civspy;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple single-sampling sampler. Used for very basic data queries.
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
	 * @return
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
