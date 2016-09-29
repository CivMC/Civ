package com.devotedmc.ExilePearl.util;

import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.mockito.Mockito;

public abstract class MockScheduler implements BukkitScheduler {

	public static MockScheduler create() {
		MockScheduler s = mock(MockScheduler.class, Mockito.CALLS_REAL_METHODS);
		s.nextId = 0;
		s.curTick = 0;
		s.tasks = new HashMap<Integer, SchedulerEntry>();
		s.pendingRemove = new HashSet<SchedulerEntry>();
		return s;
	}
	
	
	public class SchedulerEntry {
		public final Integer taskId;
		public final Runnable task;
		public final long period;
		public long nextRunTick;
		
		public SchedulerEntry(Integer taskId, Runnable task, long period, long nextRunTick) {
			this.taskId = taskId;
			this.task = task;
			this.period = period;
			this.nextRunTick = nextRunTick;
		}
	}
	
	public Integer nextId = 0;
	public HashMap<Integer, SchedulerEntry> tasks;
	private HashSet<SchedulerEntry> pendingRemove;
	
	private long curTick;

	
	@Override
	public int scheduleSyncRepeatingTask(Plugin plugin, Runnable task, long delay, long period) {
		Integer taskId = nextId++;
		tasks.put(taskId, new SchedulerEntry(taskId, task, period, curTick + delay));
		return taskId;
	}
	
	@Override
	public int scheduleSyncDelayedTask(Plugin plugin, Runnable task, long delay) {
		return scheduleSyncRepeatingTask(plugin, task, delay, 0);
	}
	
	@Override
	public int scheduleSyncDelayedTask(Plugin plugin, Runnable task) {
		return scheduleSyncRepeatingTask(plugin, task, 0, 0);
	}

	@Override
    public void cancelTask(int taskId) {
    	SchedulerEntry entry = tasks.get(taskId);
    	if (entry != null) {
        	pendingRemove.add(entry);
    	}
    }
	
	/**
	 * Performs a single tick
	 */
	public void doTick() {
		
		for(SchedulerEntry entry : pendingRemove) {
			tasks.remove(entry);
		}
		pendingRemove.clear();
		
		for(SchedulerEntry entry : tasks.values()) {
			if (curTick >= entry.nextRunTick) {
				entry.task.run();
				
				if (entry.period > 0) {
					entry.nextRunTick = curTick + entry.period;
				} else {
					pendingRemove.add(entry);
				}
			}
		}
		
		curTick++;
	}
	
	/**
	 * Performs a numer of ticks
	 * @param numTicks the number of ticks to perform
	 */
	public void doTicks(int numTicks) {
		for (int i = 0; i < numTicks; i++) {
			doTick();
		}
	}
}
