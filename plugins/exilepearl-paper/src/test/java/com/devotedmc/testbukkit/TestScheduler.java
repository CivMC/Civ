package com.devotedmc.testbukkit;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitWorker;

public class TestScheduler implements BukkitScheduler {

	@Override
	public int scheduleSyncDelayedTask(Plugin plugin, Runnable task, long delay) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int scheduleSyncDelayedTask(Plugin plugin, BukkitRunnable task, long delay) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int scheduleSyncDelayedTask(Plugin plugin, Runnable task) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int scheduleSyncDelayedTask(Plugin plugin, BukkitRunnable task) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int scheduleSyncRepeatingTask(Plugin plugin, Runnable task, long delay, long period) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int scheduleSyncRepeatingTask(Plugin plugin, BukkitRunnable task, long delay, long period) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int scheduleAsyncDelayedTask(Plugin plugin, Runnable task, long delay) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int scheduleAsyncDelayedTask(Plugin plugin, Runnable task) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int scheduleAsyncRepeatingTask(Plugin plugin, Runnable task, long delay, long period) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> Future<T> callSyncMethod(Plugin plugin, Callable<T> task) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void cancelTask(int taskId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void cancelTasks(Plugin plugin) {
		// TODO Auto-generated method stub

	}

	@Override
	public void cancelAllTasks() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isCurrentlyRunning(int taskId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isQueued(int taskId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<BukkitWorker> getActiveWorkers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<BukkitTask> getPendingTasks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BukkitTask runTask(Plugin plugin, Runnable task) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BukkitTask runTask(Plugin plugin, BukkitRunnable task) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BukkitTask runTaskAsynchronously(Plugin plugin, Runnable task) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BukkitTask runTaskAsynchronously(Plugin plugin, BukkitRunnable task) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BukkitTask runTaskLater(Plugin plugin, Runnable task, long delay) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BukkitTask runTaskLater(Plugin plugin, BukkitRunnable task, long delay) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BukkitTask runTaskLaterAsynchronously(Plugin plugin, Runnable task, long delay)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BukkitTask runTaskLaterAsynchronously(Plugin plugin, BukkitRunnable task, long delay)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BukkitTask runTaskTimer(Plugin plugin, Runnable task, long delay, long period)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BukkitTask runTaskTimer(Plugin plugin, BukkitRunnable task, long delay, long period)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BukkitTask runTaskTimerAsynchronously(Plugin plugin, Runnable task, long delay, long period)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BukkitTask runTaskTimerAsynchronously(Plugin plugin, BukkitRunnable task, long delay, long period)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

}
