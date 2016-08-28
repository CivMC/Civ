package com.programmerdan.minecraft.civspy;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Allows samplers to take multiple samples at once; for instance if you want to record the
 * same data against multiple keys.
 * 
 * @author ProgrammerDan
 */
public abstract class MultiDataSampler extends DataSampler {

	public MultiDataSampler(DataManager target, Logger logger) {
		super(target, logger);
	}
	
	/**
	 * Managed run pattern, handles calling {@link #sample} and passing the results to the Manager.
	 */
	@Override
	public final void run() {
		if (isActive()) {
			try {
				List<DataSample> data = this.sample();
				if (data != null) {
					for (DataSample data1 : data) {
						target.enqueue(data1);
					}
				}
			} catch ( Exception e ) {
				logger.log(Level.SEVERE, "Uncaught exception while sampling!", e);
				inError();
			}
		}
	}
	
	/**
	 * Samplers should implement this method.
	 */
	public abstract List<DataSample> sample();


}
