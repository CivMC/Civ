package com.devotedmc.ExilePearl.util;

import java.util.logging.Level;

import com.devotedmc.ExilePearl.ExilePearlApi;

public abstract class ExilePearlTask implements ExilePearlRunnable {

	protected static final int TICKS_PER_MINUTE = 1200;
	public static final int TICKS_PER_SECOND = 20;
	
	protected final ExilePearlApi pearlApi;
	
	protected boolean enabled = false;
	protected int taskId = 0;
	
	public ExilePearlTask(final ExilePearlApi pearlApi) {
		Guard.ArgumentNotNull(pearlApi, "pearlApi");
		
		this.pearlApi = pearlApi;
	}

	/**
	 * Starts the worker task
	 */
	public void start() {
		if (enabled) {
			pearlApi.log(Level.WARNING, "Failed to start the task '%s' but it was already started.", getTaskName());
			return;
		}
		
		long tickInterval = getTickInterval();
		taskId = pearlApi.getScheduler().scheduleSyncRepeatingTask(pearlApi.getPlugin(), this, tickInterval, tickInterval);
		if (taskId == -1) {
			pearlApi.log("Failed to start the task '%s'.", getTaskName());
			return;
		} else {
			enabled = true;
			pearlApi.log("Started the task '%s'.", getTaskName());
		}
	}

	/**
	 * Stops the worker task
	 */
	public void stop() {
		if (enabled) {
			pearlApi.getScheduler().cancelTask(taskId);
			enabled = false;
			taskId = 0;
			pearlApi.log("Stopped the task '%s'.", getTaskName());
		}
	}

	/**
	 * Restarts the worker task
	 */
	public void restart() {
		stop();
		start();
	}


	public boolean isRunning() {
		return enabled;
	}
	
	public abstract String getTaskName();
	
	public abstract int getTickInterval();
}
