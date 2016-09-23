package com.devotedmc.ExilePearl.storage;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.PearlLogger;
import com.devotedmc.ExilePearl.storage.AsyncPearlRecord.WriteType;
import com.devotedmc.ExilePearl.util.Guard;

/**
 * Wrapper class for PluginStorage that performs asynchronous writes
 * @author Gordon
 */
public class MySqlStorageAsync implements PluginStorage, Runnable {

	private final PluginStorage storage;
	private final PearlLogger logger;
	private final BlockingQueue<AsyncPearlRecord> queue = new LinkedBlockingQueue<AsyncPearlRecord>();
	private Thread thread;
	private boolean isEnabled;
	
	public MySqlStorageAsync(final PluginStorage storage, final PearlLogger logger) {
		Guard.ArgumentNotNull(storage, "storage");
		Guard.ArgumentNotNull(logger, "logger");
		
		this.storage = storage;
		this.logger = logger;
	}
	
	@Override
	public boolean connect() {
		if (storage.connect()) {
			logger.log("Starting the async database thread.");
			isEnabled = true;
			thread = new Thread(this);
			thread.start();
			return true;
		}
		return false;
	}

	@Override
	public boolean disconnect() {
		isEnabled = false;
		queue.add(new AsyncPearlRecord(null, WriteType.TERMINATE));
		return storage.disconnect();
	}

	@Override
	public boolean isConnected() {
		return storage.isConnected();
	}
	
	@Override
	public Collection<ExilePearl> loadAllPearls() {
		return storage.loadAllPearls();
	}

	@Override
	public void pearlInsert(ExilePearl pearl) {
		Guard.ArgumentNotNull(pearl, "pearl");
		queue.add(new AsyncPearlRecord(pearl, WriteType.INSERT));
		
	}

	@Override
	public void pearlRemove(ExilePearl pearl) {
		Guard.ArgumentNotNull(pearl, "pearl");
		queue.add(new AsyncPearlRecord(pearl, WriteType.REMOVE));
		
	}

	@Override
	public void pearlUpdateLocation(ExilePearl pearl) {
		Guard.ArgumentNotNull(pearl, "pearl");
		queue.add(new AsyncPearlRecord(pearl, WriteType.UPDATE_LOCATION));
		
	}

	@Override
	public void pearlUpdateHealth(ExilePearl pearl) {
		Guard.ArgumentNotNull(pearl, "pearl");
		queue.add(new AsyncPearlRecord(pearl, WriteType.UPDATE_HEALTH));
		
	}

	@Override
	public void run() {
		logger.log("The async database thread is running.");
		
		while (isEnabled) {
			try {
				processAsyncRecord(queue.poll(100, TimeUnit.MILLISECONDS));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		logger.log("The async database thread has terminated.");
	}
	
	
	private void processAsyncRecord(AsyncPearlRecord record) {
		if (record == null || !isEnabled) {
			return;
		}
		
		switch(record.getWriteType()) {
		case INSERT:
			storage.pearlInsert(record.getPearl());
			break;
			
		case REMOVE:
			storage.pearlRemove(record.getPearl());
			break;
			
		case UPDATE_LOCATION:
			storage.pearlUpdateLocation(record.getPearl());
			break;
			
		case UPDATE_HEALTH:
			storage.pearlUpdateHealth(record.getPearl());
			break;

		case TERMINATE:
		default:
			break;
		}
	}
}
