package com.programmerdan.minecraft.civspy;

import java.util.logging.Logger;

import org.bukkit.event.Listener;

/**
 * Root abstract class that exemplifies a piece of code that listens to Bukkit
 * events and passes some data along for batching in aggregate. 
 * 
 * Use one of the subclasses in {@link com.programmerdan.minecraft.civspy.listeners} instead.
 * 
 * @see com.programmerdan.minecraft.civspy.listeners.ServerDataListener
 * @author ProgrammerDan
 */
public abstract class DataListener implements Listener {
	
	/**
	 * Target for all messages to enqueue against.
	 */
	private final DataManager target;

	protected final Logger logger;
	
	/**
	 * Sets up this listener.
	 * 
	 * @param target The DataManager to pass point data along to
	 * @param logger A logger available for logging of errors or status
	 */
	public DataListener(final DataManager target, final Logger logger) {
		this.target = target;
		this.logger = logger;
	}
	
	/**
	 * Expose this method in case this listener needs cleanup (data structures, tasks, etc).
	 */
	public abstract void shutdown();

	/**
	 * Safely wrap enqueue requests against the privately held DataManager target.
	 * 
	 * @param sample The DataSample to enqueue for aggregation.
	 */
	protected void record(DataSample sample) {
		if (sample != null) {
			target.enqueue(sample);
		}
	}
	
}
