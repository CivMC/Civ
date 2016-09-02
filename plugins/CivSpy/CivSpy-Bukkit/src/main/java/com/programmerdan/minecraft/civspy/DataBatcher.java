package com.programmerdan.minecraft.civspy;

import java.sql.PreparedStatement;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.programmerdan.minecraft.civspy.database.Database;

/**
 * Roughly similar approach to Manager; stage() unwinds data to batch it up, then
 * other task(s) come along to deliver to the database.
 * 
 * @author ProgrammerDan
 *
 */
public class DataBatcher {
	private final Database db;
	private final Logger logger;

	private final AtomicLong inflowCount;
	private final AtomicLong outflowCount;
	private final AtomicInteger workerCount;
	
	/**
	 * Threadpool to handle scheduled tasks, liveness tester 
	 */
	private final ScheduledExecutorService scheduler;
	
	/**
	 * Threadpool to handle batch constructors
	 */
	private final ExecutorService batchExecutor;
	
	/**
	 * The queue for unspooled aggregations.
	 */
	private final LinkedTransferQueue<BatchLine> batchQueue;
	
	private boolean active = true;
	
	/**
	 * Maximum number of elements to put into a single batch.
	 * 
	 * TODO: From config
	 */
	private final long maxBatchSize = 100l;
	/**
	 * Maximum number of milliseconds to wait on more elements to join an open batch.
	 * 
	 * TODO: From config
	 */
	private final long maxBatchWait = 1000l;
	/**
	 * Effectively, max simultaneous connections. A single batch is run against a single connection.
	 * 
	 * TODO: From config
	 */
	private final int maxExecutors = 5;
	
	public DataBatcher(final Database db, final Logger logger) {
		this.db = db;
		this.logger = logger;
		this.inflowCount = new AtomicLong(0l);
		this.outflowCount = new AtomicLong(0l);
		this.workerCount = new AtomicInteger(0);
		
		this.batchQueue = new LinkedTransferQueue<BatchLine>();
		
		this.scheduler = Executors.newScheduledThreadPool(1);
		
		this.startWatcher();
		
		this.batchExecutor = Executors.newFixedThreadPool(this.maxExecutors);
	}
	
	public void shutdown() {
		active = false;
		int delay = 0;
		while (!this.batchQueue.isEmpty() && delay < 600) {
			try {
				Thread.sleep(1000l);
			} catch(Exception e) {}
			delay ++;
			if (delay % 30 == 0) {
				this.logger.log(Level.INFO, "Waiting on batch queue workers to finish up, {0} seconds so far", delay);
			}
		}
		if (delay >= 600) {
			this.logger.log(Level.WARNING, "Giving up on waiting. DATA LOSS MAY OCCUR.");
		}
		batchQueue.clear();
		this.scheduler.shutdown();
		this.batchExecutor.shutdown();
		
		try {
			if (!this.batchExecutor.awaitTermination(60l, TimeUnit.SECONDS)) {
				this.logger.log(Level.WARNING, "Giving up on waiting for batch commit; DATA LOSS MAY HAVE OCCURRED.");
			}
		} catch (InterruptedException ie) {
			this.logger.log(Level.WARNING, "Was awaiting batch saving to finish, but was interrupted.", ie);
		}
	}
	
	/** 
	 * This task just keeps a worker running from time to time, just to make sure someone is watching
	 * the incoming queue and help moderate against inflow explosion.
	 */
	private void startWatcher() {
		scheduler.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				if (workerCount.get() < 1) {
					generateWorker();
				}
			}
		}, maxBatchWait, maxBatchWait, TimeUnit.MILLISECONDS);
	}
	
	private void generateWorker() {
		try {
			batchExecutor.execute( new Runnable() {
				
				@Override
				public void run() {
					/* General sketch:
					 *  Based on maximum wait time and max count, pull batch lines off
					 *  and batch up into a new PrepareStatement batch against a new connection.
					 */
					
					PreparedStatement batch = null;
					long startTime = System.currentTimeMillis();
					int count = 0;
					while (System.currentTimeMillis() - startTime < maxBatchWait
							&& count < maxBatchSize) {
						try {
							BatchLine newLine = batchQueue.poll(maxBatchWait, TimeUnit.MILLISECONDS);
							if (newLine != null) {
								batch = db.batchData(newLine.key.getKey(),
										newLine.key.getServer(),
										newLine.key.getWorld(), 
										newLine.key.getChunkX(),
										newLine.key.getChunkZ(),
										newLine.key.getPlayer(),
										newLine.valueString,
										newLine.valueNumber,
										newLine.timestamp, null, batch);
								count ++;
								outflowCount.getAndIncrement();
							}
						} catch (InterruptedException ie) {
							logger.log(Level.WARNING, "A batching task was interrupted", ie);
						}
					}
					
					if (batch != null) {
						try {
							int[] results = db.batchExecute(batch, true);
							int inserts = 0;
							if (results != null) {
								for (int r : results) {
									inserts += r;
								}
							}
							if (inserts != results.length) {
								logger.log(Level.WARNING, "Some submitted data failed to insert: given {0} saved {1}",
										new Object[]{results.length, inserts});
							}
						} catch (Exception e) {
							logger.log(Level.SEVERE, "Critical failure while saving a batch!", e);
						}
					}
					
					inflowCount.addAndGet(-count);
					outflowCount.addAndGet(-count);
				}
			});
		} catch (RejectedExecutionException ree) {
			logger.log(Level.WARNING, "Tried to scheduled a new batch worker, rejected: ", ree);
		}
	}
	
	public void stage(DataSampleKey key, DataAggregate aggregate) {
		if (!active) return;
		
		// now unwrap
		if (aggregate.sum != null) {
			this.batchQueue.offer(new BatchLine(key, aggregate.getTimestamp(), null, aggregate.sum));
			this.inflowCount.getAndIncrement();
		}
		for (Entry<String, Double> entry : aggregate.namedSums.entrySet()) {
			this.batchQueue.offer(new BatchLine(key, aggregate.getTimestamp(), entry.getKey(), entry.getValue()));
			this.inflowCount.getAndIncrement();
		}
		if (this.inflowCount.get() - this.outflowCount.get() >= maxBatchSize) {
			if (this.workerCount.get() < this.maxExecutors) {
				this.generateWorker();
			} else {
				logger.log(Level.WARNING, "Inflow count far in advance of outflow count, but no room for more outflow workers. Check your config!");
			}
		}
	}
	
	/**
	 * Lightweight unwrap for aggregator
	 */
	static class BatchLine {
		DataSampleKey key;
		long timestamp;
		String valueString;
		Double valueNumber;
		public BatchLine(DataSampleKey key, long timestamp, String valueString, Double valueNumber) {
			this.key = key;
			this.timestamp = timestamp;
			this.valueString = valueString;
			this.valueNumber = valueNumber;
		}
	}
}
