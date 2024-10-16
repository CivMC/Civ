package com.devotedmc.ExilePearl.storage;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.PearlLogger;
import com.devotedmc.ExilePearl.storage.AsyncPearlRecord.WriteType;
import com.google.common.base.Preconditions;
import java.nio.channels.NotYetConnectedException;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper class for PluginStorage that performs asynchronous writes
 * @author Gordon
 */
class AsyncStorageWriter implements PluginStorage, Runnable {

	private PluginStorage storage;
	private final PearlLogger logger;
	private final BlockingQueue<AsyncPearlRecord> queue = new LinkedBlockingQueue<AsyncPearlRecord>();
	private Thread thread;
	private boolean isEnabled;

	public AsyncStorageWriter(final PluginStorage storage, final PearlLogger logger) {
		Preconditions.checkNotNull(storage, "storage");
		Preconditions.checkNotNull(logger, "logger");

		this.storage = storage;
		this.logger = logger;
	}

	@Override
	public boolean connect() {
		if (storage.connect()) {
			logger.log("Starting the async storage thread.");
			isEnabled = true;
			thread = new Thread(this);
			thread.start();
			return true;
		}
		return false;
	}

	@Override
	public void disconnect() {
		isEnabled = false;
		queue.add(new AsyncPearlRecord(null, WriteType.TERMINATE));
		storage.disconnect();
	}

	@Override
	public boolean isConnected() {
		return isEnabled && storage.isConnected();
	}

	@Override
	public Collection<ExilePearl> loadAllPearls() {
		return storage.loadAllPearls();
	}

	@Override
	public void pearlInsert(ExilePearl pearl) {
		Preconditions.checkNotNull(pearl, "pearl");
		checkRunning();

		queue.add(new AsyncPearlRecord(pearl, WriteType.INSERT));
	}

	@Override
	public void pearlRemove(ExilePearl pearl) {
		Preconditions.checkNotNull(pearl, "pearl");
		checkRunning();

		queue.add(new AsyncPearlRecord(pearl, WriteType.REMOVE));
	}

	@Override
	public void updatePearlLocation(ExilePearl pearl) {
		Preconditions.checkNotNull(pearl, "pearl");
		checkRunning();

		queue.add(new AsyncPearlRecord(pearl, WriteType.UPDATE_LOCATION));
	}

	@Override
	public void updatePearlHealth(ExilePearl pearl) {
		Preconditions.checkNotNull(pearl, "pearl");
		checkRunning();

		queue.add(new AsyncPearlRecord(pearl, WriteType.UPDATE_HEALTH));
	}

	@Override
	public void updatePearlFreedOffline(ExilePearl pearl) {
		Preconditions.checkNotNull(pearl, "pearl");
		checkRunning();

		queue.add(new AsyncPearlRecord(pearl, WriteType.UPDATE_FREED_OFFLINE));
	}

	@Override
	public void updatePearlType(ExilePearl pearl) {
		Preconditions.checkNotNull(pearl, "pearl");
		checkRunning();

		queue.add(new AsyncPearlRecord(pearl, WriteType.UPDATE_TYPE));
	}

	@Override
	public void updatePearlKiller(ExilePearl pearl) {
		Preconditions.checkNotNull(pearl, "pearl");
		checkRunning();

		queue.add(new AsyncPearlRecord(pearl, WriteType.UPDATE_KILLER));
	}

	@Override
	public void updatePearlLastOnline(ExilePearl pearl) {
		Preconditions.checkNotNull(pearl, "pearl");
		checkRunning();

		queue.add(new AsyncPearlRecord(pearl, WriteType.UPDATE_LAST_SEEN));
	}

	@Override
	public void updatePearlSummoned(ExilePearl pearl) {
		Preconditions.checkNotNull(pearl, "pearl");
		checkRunning();

		queue.add(new AsyncPearlRecord(pearl, WriteType.UPDATE_SUMMONED));
	}


	@Override
	public void updateReturnLocation(ExilePearl pearl) {
		Preconditions.checkNotNull(pearl, "pearl");
		checkRunning();

		queue.add(new AsyncPearlRecord(pearl, WriteType.UPDATE_RETURN_LOCATION));
	}

	@Override
	public void updatePearledOnDate(ExilePearl pearl) {
		Preconditions.checkNotNull(pearl, "pearl");
		checkRunning();

		queue.add(new AsyncPearlRecord(pearl, WriteType.UPDATE_PEARLED_ON));
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

		isEnabled = false;
		logger.log("The async storage thread has terminated.");
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
			storage.updatePearlLocation(record.getPearl());
			break;

		case UPDATE_HEALTH:
			storage.updatePearlHealth(record.getPearl());
			break;

		case UPDATE_FREED_OFFLINE:
			storage.updatePearlFreedOffline(record.getPearl());
			break;

		case UPDATE_TYPE:
			storage.updatePearlType(record.getPearl());
			break;

		case UPDATE_KILLER:
			storage.updatePearlKiller(record.getPearl());
			break;

		case UPDATE_LAST_SEEN:
			storage.updatePearlLastOnline(record.getPearl());
			break;

		case UPDATE_SUMMONED:
			storage.updatePearlSummoned(record.getPearl());
			break;

		case UPDATE_RETURN_LOCATION:
			storage.updateReturnLocation(record.getPearl());
			break;

		case UPDATE_PEARLED_ON:
			storage.updatePearledOnDate(record.getPearl());

		case TERMINATE:
		default:
			break;
		}
	}


	private void checkRunning() {
		if (!isEnabled) {
			throw new NotYetConnectedException();
		}
	}
}
