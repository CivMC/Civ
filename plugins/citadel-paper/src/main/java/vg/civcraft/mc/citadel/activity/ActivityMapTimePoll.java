package vg.civcraft.mc.citadel.activity;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class ActivityMapTimePoll {
	private static final long POLL_INTERVAL_MS = 1000L;

	private final ConcurrentLinkedQueue<Long> nanoTimes = new ConcurrentLinkedQueue<>();
	private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);
	private final Object syncRoot = new Object();

	private long regionLoadCount;
	private long regionLoadSumNano;
	private long regionLoadMinTimeNano;
	private long regionLoadMaxTimeNano;

	private void poll() {
		long currentRegionLoadCount = 0;
		long currentRegionLoadSumNano = 0;
		long currentRegionLoadMinTimeNano = Long.MAX_VALUE;
		long currentRegionLoadMaxTimeNano = Long.MIN_VALUE;

		Long current;
		while ((current = nanoTimes.poll()) != null) {
			if (currentRegionLoadCount > 0) {
				if (current < currentRegionLoadMinTimeNano) {
					currentRegionLoadMinTimeNano = current;
				}
				if (current > currentRegionLoadMaxTimeNano) {
					currentRegionLoadMaxTimeNano = current;
				}
			} else {
				currentRegionLoadMinTimeNano = current;
				currentRegionLoadMaxTimeNano = current;
			}

			currentRegionLoadCount++;
			currentRegionLoadSumNano += current;
		}

		if (currentRegionLoadCount == 0) {
			return;
		}

		synchronized (syncRoot) {
			if (regionLoadCount > 0) {
				if (currentRegionLoadMinTimeNano < regionLoadMinTimeNano) {
					regionLoadMinTimeNano = currentRegionLoadMinTimeNano;
				}
				if (currentRegionLoadMaxTimeNano > regionLoadMaxTimeNano) {
					regionLoadMaxTimeNano = currentRegionLoadMaxTimeNano;
				}
			} else {
				regionLoadMinTimeNano = currentRegionLoadMinTimeNano;
				regionLoadMaxTimeNano = currentRegionLoadMaxTimeNano;
			}

			regionLoadCount += currentRegionLoadCount;
			regionLoadSumNano += currentRegionLoadSumNano;
		}
	}

	void startPolling() {
		scheduler.scheduleWithFixedDelay(() -> {
			poll();
		}, POLL_INTERVAL_MS, POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
	}

	void stopPolling() {
		this.scheduler.shutdown();

		try {
			if (!this.scheduler.awaitTermination(5, TimeUnit.SECONDS))
				this.scheduler.shutdownNow();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}

	void pushTimeNano(long nano) {
		nanoTimes.add(nano);
	}

	void getStat(ActivityMapStat stat) {
		poll();

		synchronized (syncRoot) {
			stat.regionLoadCount = regionLoadCount;
			stat.regionLoadSumNano = regionLoadSumNano;
			stat.regionLoadMinTimeNano = regionLoadMinTimeNano;
			stat.regionLoadMaxTimeNano = regionLoadMaxTimeNano;
		}
	}
}
