package com.programmerdan.minecraft.civspy;

import java.util.logging.Logger;

import org.bukkit.event.Listener;

public abstract class DataListener implements Listener {
	
	/**
	 * Target for all messages to enqueue against.
	 */
	private final DataManager target;

	protected final Logger logger;
	
	public DataListener(final DataManager target, final Logger logger) {
		this.target = target;
		this.logger = logger;
	}
	
	/**
	 * Expose this method in case this listener needs cleanup (data structures, tasks, etc).
	 */
	public abstract void shutdown();

	protected void record(DataSample sample) {
		if (sample != null) {
			target.enqueue(sample);
		}
	}
	
}
