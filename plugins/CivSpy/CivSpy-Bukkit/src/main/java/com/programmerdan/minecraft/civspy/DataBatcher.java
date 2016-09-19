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
	
	private final AtomicLong periodInflowCount;
	private final AtomicLong periodOutflowCount;
	private final AtomicLong periodWorkerCount;
	
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
	 */
	private final long maxBatchSize; // = 100l;
	/**
	 * Maximum number of milliseconds to wait on more elements to join an open batch.
	 */
	private final long maxBatchWait; // = 1000l;
	/**
	 * Effectively, max simultaneous connections. A single batch is run against a single connection.
	 */
	private final int maxExecutors; // = 5;
	
	/**
	 * Creates a new Data Batcher.
	 * 
	 * @param db The Database to send data to. Wraps a connection pool.
	 * @param logger The Logger instance to use for logging.
	 * @param maxBatchSize The maximum number of elements to commit together as a batch
	 * @param maxBatchWait The maximum amount of time to wait for that max elements to show up
	 * @param maxBatchers The maximum number of workers constructing batches simultaneously
	 */
	public DataBatcher(final Database db, final Logger logger, final Long maxBatchSize,
			final Long maxBatchWait, final Integer maxBatchers) {
		this.db = db;
		this.logger = logger;
		this.inflowCount = new AtomicLong(0l);
		this.outflowCount = new AtomicLong(0l);
		this.workerCount = new AtomicInteger(0);
		this.periodInflowCount = new AtomicLong(0l);
		this.periodOutflowCount = new AtomicLong(0l);
		this.periodWorkerCount = new AtomicLong(0l);

		this.maxBatchSize = (maxBatchSize == null ? 100l : maxBatchSize);
		this.maxBatchWait = (maxBatchWait == null ? 1000l : maxBatchWait);
		this.maxExecutors = (maxBatchers == null ? 5 : maxBatchers);
		
		this.batchQueue = new LinkedTransferQueue<BatchLine>();
		
		this.scheduler = Executors.newScheduledThreadPool(1);
		
		this.startWatcher();
		
		this.batchExecutor = Executors.newFixedThreadPool(this.maxExecutors);
	}
	
	/**
	 * Force an orderly shutdown of the batching process. Waits until the queue is done
	 * or 2 minutes have elapsed. Once dequeue has occurred, waits up to 2 minutes
	 * for the consequential commits to complete. 
	 */
	public void shutdown() {
		active = false;
		int delay = 0;
		while (!this.batchQueue.isEmpty() && delay < 120) {
			if (this.workerCount.get() < this.maxExecutors) {
				this.generateWorker();
				this.logger.log(Level.INFO, "Starting a new worker to help with offloading");
			}
			try {
				Thread.sleep(1000l);
			} catch(Exception e) {}
			delay ++;
			if (delay % 30 == 0) {
				this.logger.log(Level.INFO, "Waiting on batch queue workers to finish up, {0} seconds so far", delay);
			}
		}
		if (delay >= 120) {
			this.logger.log(Level.WARNING, "Giving up on waiting. DATA LOSS MAY OCCUR.");
		}
		batchQueue.clear();
		this.scheduler.shutdown();
		this.batchExecutor.shutdown();
		
		try {
			if (!this.batchExecutor.awaitTermination(120l, TimeUnit.SECONDS)) {
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
			private long executions = 0;
			@Override
			public void run() {
				executions ++;
				if (workerCount.get() < 1) {
					generateWorker();
				}
				
				if (executions % 20 == 0) {
					logger.log(Level.INFO, "Since last report, {0} Batch Unloaders ran. Total: {1} records received, {2} records written.",
							new Object[] {periodWorkerCount.getAndSet(0l), periodInflowCount.getAndSet(0l), periodOutflowCount.getAndSet(0l)});
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
								periodOutflowCount.getAndIncrement();
							}
						} catch (InterruptedException ie) {
							logger.log(Level.WARNING, "A batching task was interrupted", ie);
							break;
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
					workerCount.decrementAndGet();
				}
			});
			workerCount.incrementAndGet();
			periodWorkerCount.incrementAndGet();
		} catch (RejectedExecutionException ree) {
			logger.log(Level.WARNING, "Tried to scheduled a new batch worker, rejected: ", ree);
		}
	}
	
	/**
	 * {@link DataManager} calls this to queue aggregates up for batch commit.
	 * 
	 * @param key The {@link DataSampleKey} to index this aggregate against.
	 * @param aggregate A {@link DataAggregate} which holds either one or more aggregations of data over a time period.
	 */
	public void stage(DataSampleKey key, DataAggregate aggregate) {
		if (!active) return;
		
		// now unwrap
		if (aggregate.sum != null) {
			this.batchQueue.offer(new BatchLine(key, aggregate.getTimestamp(), null, aggregate.sum));
			this.inflowCount.getAndIncrement();
			this.periodInflowCount.getAndIncrement();
		}
		for (Entry<String, Double> entry : aggregate.namedSums.entrySet()) {
			this.batchQueue.offer(new BatchLine(key, aggregate.getTimestamp(), entry.getKey(), entry.getValue()));
			this.inflowCount.getAndIncrement();
			this.periodInflowCount.getAndIncrement();
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
