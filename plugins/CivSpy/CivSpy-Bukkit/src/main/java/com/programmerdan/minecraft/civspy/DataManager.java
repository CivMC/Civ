package com.programmerdan.minecraft.civspy;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedTransferQueue;

/**
 * Represents a self-monitoring manager that accepts data into a queue and asynchronously pulls data
 *   off and handles aggregation; then passes off to the batcher for database insertion
 */
public class DataManager {
	private final DataBatcher batcher;
	private final Logger logger;

	/**
	 * Threadpool to handle scheduled tasks like flowMonitor and aggregateHandler.
	 */
	private final ScheduledExecutorService scheduler;
	/**
	 * This task runs periodically and does the following:
	 * <ul><li>Zeros out the next window buckets
	 *     <li>Increments window index
	 *     <li>Adds up all other window flow records
	 *     <li>Prints out alerts of flow issues if necessary
	 *     <li>Enqueues flowrate data as periodic data
	 * </ul>
	 */
	private ScheduledFuture<?> flowMonitor;

	/**
	 * Based on configuration of aggregation window, this handler does the work of submitting
	 * aggregate batches to the batching handler for insertion into the database.
	 */
	private ScheduledFuture<?> aggregateHandler;

	/**
	 * A fixed size threadpool of greedy queue consumers that read off the data queue.
	 */
	private final ExecutorService dequeueWorkers;

	/*
	 * These are all flow watching variables. The basic idea is simple. You record flowcount into a "window" of a bucket
	 * then periodically move the "now" bucket, and sum up all the buckets that are left. Use roundrobin so no
	 * new data allocation ever, just clearing and filling.
	 * Lets you do instant, short term, or longer term flow rate monitoring.
	 */
	private long[] instantOutflow;
	private long[] instantInflow;
	private long lastFlowUpdate = 0l;
	private int whichFlowWindow = 0;
	private double avgOutflow;
	private double avgInflow;
	private double flowRatio;

	/**
	 * Defines how many "windows" to capture flow rate in. These are filled round-robin and used to monitor flowrate.
	 * Actual use is flowCaptureWindows - 1; the "current" flow window is ignored while recomputing flow.
	 */
	private int flowCaptureWindows = 61;
	/**
	 * Defines how long inbetween window movement in milliseconds; or, how long to capture inflow/outflow before 
	 * updating the flow ratios
	 */
	private long flowCapturePeriod = 1000;
	/**
	 * What avg inflow/outflow ratio is considered "bad enough" that if exceeded a warning message should be generated?
	 * Recommended: > 1.1
	 * As this indicates that inflow exceeds outflow over the capture window.
	 */
	private double flowRatioWarn = 1.1d;
	/**
	 * What avg inflow/outflow ratio is considered "very bad" that possibly other action will be taken (dropping incoming data, etc.)
	 * Recommended: > 2.0
	 * As this indicates that on average inflow exceeds outflow by _double_ meaning the queue is growing very rapidly.
	 */
	private double flowRatioSevere = 2.0d;

	private final LinkedTransferQueue<DataSample> sampleQueue;
	/**
	 * This controls the aggregation of data points that are eligible for aggregation. From this single
	 * configuration value comes a host of outcomes related to aggregation.
	 */ 
	private final long aggregationPeriod;
	/**
	 * Collection periods are not instantly transferred to the database once the period has "ended" -- a delay is maintained, 
	 * controlled by this delay count. If you notice a lot of lost records due to closed out periods, expand this delay count.
	 * Be mindful of memory implications.
	private final int periodDelayCount;

	/**
	 * How many aggregation windows are, in general, kept "in advance" of data flowing in?
	 * Setting this higher can adjust for issues where it takes too long to allocate a new window and data is getting lost.
	 * Recommend at least 1/4 - 1/2 of delay count.
	 */
	private final int periodFutureCount;

	/**
	 * Derived from future and delay + 1; the actual size of aggregation window for easy modulo looping
	 */
	private final int aggregationCycleSize;

	/**
	 * Where the magic happens. Based on Key, a DataAggregate object is kept, that actually does the work of adding up stuff.
	 * Note this is also windowed; in a round-robin fashion Maps are cycled onto and off of this array, oldest leaves and
	 * is batched to the database, newest arrives and begins accepting new data.
	 * Note this will be sized periodDelayCount + periodFutureCount + 1, so that the outgoing data can "breath" for a moment while asynchronously
	 * offloaded while new data can instantly begin collecting in a new aggregator.
	 */
	private ConcurrentHashMap<DataSampleKey, DataAggregate>[] aggregation;

	private long[] aggregationWindowStart;
	private long[] aggregationWindowEnd;

	/**
	 * This index forms the "base" of aggregation. Initially a bunch of windows are set up into the future; as time progresses
	 * we begin to reach the "end" of this advance, and start removing old windows to make room for future data.
	 * This tracks the "base" of that advanced, and indexes into the array are computed against this.
	 */
	private int oldestAggregatorIndex;
	/**
	 * This is the index currently being saved out to database.
	 */
	private int offloadAggregatorIndex;

	/**
	 * For the lifetime of execution, keeps track of # of total misses; that is, data pulled off the queue that can't fit
	 * in any aggregator currently maintained. This number should always be zero. If you start noticing it increasing,
	 * this will correlate with inflow exceeding outflow. Either increase your period count (how many periods you track 
	 * concurrently) or alter your sampling/event tracking rates to ease congestion.
	 */
	private long missCounter = 0l;


	/**
	 * Sets up a data manager; defining who to forward aggregate and sampled data to, a logger to log to, and aggregation
	 * configurations including period of aggregation, and how many "windows" to keep and store samples in.
	 * 
	 * @param batcher the DataBatcher that handles DB interfacing
	 * @param logger the logger to send message to
	 * @param aggregationPeriod the length of time to aggregate, say, 1 second, 30 seconds, etc. expressed as milliseconds.
	 * @param periodDelayCount is the number of "prior" aggregation periods to hold on to before committing. Leave larger if
	 *   you expect high incoming event counts, to help even out variations in throughput
	 * @param periodFutureCount is the number of "future" aggregation periods to setup in advance. It's good to set this to 
	 *   about 1/4 to 1/2 of perioddelay for similar reasons.
	 */
	public DataManager(final DataBatcher batcher, final Logger logger, final long aggregationPeriod, final int periodDelayCount,
			final int periodFutureCount) {
		this.batcher = batcher;
		this.logger = logger;

		// Prepare the incoming queue.
		sampleQueue = new LinkedTransferQueue<DataSample>();

		// TODO: Configure flow capture and such
		this.instantOutflow = new long[flowCaptureWindows];
		this.instantInflow = new long[flowCaptureWindows];

		// Set up aggregation.
		this.aggregationPeriod = aggregationPeriod;
		this.periodDelayCount = periodDelayCount;
		this.periodFutureCount = periodFutureCount;

		// Here we set up windows. Everything is geared towards pre-compute; we pre-compute our window bounds and
		// our storages, so that once we start reading off the queue we can rapid fire with no management;
		// management and cycling the windows is someone else's job, handled round robin so nothing changes in terms of
		// array ordering, just eventually things start getting dropped if they have somehow hung around too long.
		this.aggregateCycleSize = this.periodDelayCount + this.periodFutureCount + 1;
		this.aggregation = new ConcurrentHashMap<DataSampleKey, DataAggregate>[this.aggregateCycleSize];
		this.aggregationWindowStart = new long[this.aggregateCycleSize];
		this.aggregationWindowEnd = new long[this.aggregateCycleSize];
		
		this.oldestAggregatorIndex = 1;
		this.offloadAggregatorIndex = 0;

		// We backdate our windows.
		long window = System.currentTimeMillis() - (this.aggregationPeriod * this.periodDelayCount);
		for (int idx = this.oldestAggregatorIndex; idx < this.aggregateCycleSize; id++) {
			this.aggregation[idx] = new ConcurrentHashMap<DataSampleKey, DataAggregate>();
			this.aggregationWindowStart[idx] = window;
			window += this.aggregationPeriod;
			this.aggregationWindowEnd[idx] = window;
		}

		// Now create the executor and schedule repeating tasks.
		// Executor before all. TODO: Configurable dequeue Worker pool size.
		this.scheduler = Executors.newScheduledThreadPool(2);
		this.dequeueWorkers = Executors.newFixedThreadPool(8);

		// First, the queue reading task.

		// Second, the queue throughput watchdog task.

		// Third, the aggregator window cycle task.
	}

	/**
	 * This is the only method accessible to the outside world, besides a shutdown hook method.
	 * It adds data to be processed. It increments the inflow counter.
	 */
	public void enqueue(DataSample data) {
		sampleQueue.offer(data);
		instantInflow[whichFlowWindow]++;
	}

	/**
	 * Undoes what the constructor starts. 
	 * This method stops the various executors and forces an immediate flush of _all_ aggregation buckets with data.
	 */
	public void shutdown() {
		// TODO
	}

	static class RepeatingQueueMinder implements Runnable {
		private final WeakReference<ExecutorService> scheduler;

		RepeatingQueueMinder(ExecutorService scheduler) {
			this.scheduler = new WeakReference<ExecutorService>(scheduler);
		}

		public void run() {
			/* TODO: 
			 * surround by interrupted.
			 * Blocks at queue.get()
			 * Once the queue returns something, do the work of finding the bucket and inserting it.
			 * If insertion fails, appropriately increment the counters
			 * In any case, update outflow.
			 */
			DataSample sample = null;
			try {
				sample = sampleQueue.take();
			} catch (InterruptedException ie) {
				logger.log(Level.WARNING, "While waiting on a queue, interrupted:", ie);
			}

			if (sample != null) {
				if (sample.forAggregate()) {
					// Find which bucket.
					// get the appropriate aggregate or create it
					// update it
					
					// THIS NEEDS TO BE ATOMIC SOMEHOW
					int offset = (int) ((sample.getTimestamp() - aggregationWindowStart[oldestAggregatorIndex]) / aggregationPeriod;
					if (offset < 0 || offset >= aggregateCycleSize) {
						// out of tracking period entirely.
						missCounter++;
					} else {
						int index = offset % aggregateCycleSize; // Round Robin around and around
						if (sample.isBetween(aggregationWindowStart[index], aggregationWindowEnd[index])) {
							// Sanity check.
							DataAggregate aggregate = aggregation[index].get(sample.getKey());
							if (aggregate == null) {
								aggregate = new DataAggregate(aggregationWindowStart[index]);
								aggregation[index].put(sample.getKey(), aggregate);
							}
							aggregate.include(sample);
						} else {
							logger.log(Level.WARNING, "Computed window index doesn't match when attempting to get aggregator");
							missCounter++;
						}
					}
				} else {
					// Create an aggregate to conform to batching expectations
					DataAggregate simple = new DataAggregate(sample.getTimestamp());
					simple.include(sample);
					batcher.stage(sample.getKey(), simple);
				}
			}

			if (this.scheduler.get() != null) {
				try {
					this.scheduler.get().execute(this);
				} catch (RejectedExecutionException ree) {
					logger.log(Level.WARNING, "Unable to reschedule {0} minder.", this.toString());
					logger.log(Level.WARNING, "Failure based on this exception:", ree);
				}
			} else {
				logger.log(Level.INFO, "Gracefully not rescheduling {0} as scheduler has been finalized.", this.toString());
			}
		}
	}

}
