/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.utils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import org.bukkit.Location;

import com.aleksey.castlegates.CastleGates;
import com.aleksey.castlegates.engine.bridge.BridgeManager;
import com.aleksey.castlegates.types.BlockCoord;
import com.aleksey.castlegates.types.Gearblock;
import com.aleksey.castlegates.types.TimerBatch;

public class TimerWorker extends Thread implements Runnable {
	private BridgeManager bridgeManager;
	private List<TimerBatch> batches;

	private long lastExecute = System.currentTimeMillis();
    private AtomicBoolean kill = new AtomicBoolean(false);
    private AtomicBoolean run = new AtomicBoolean(false);

    public TimerWorker(BridgeManager bridgeManager) {
    	this.bridgeManager = bridgeManager;
    	this.batches = new ArrayList<TimerBatch>();
	}

    public void startThread() {
        this.kill.set(false);

        setName("CastleGates TimerWorker Thread");
        setPriority(Thread.MIN_PRIORITY);
        start();

        CastleGates.getPluginLogger().log(Level.INFO, "TimerWorker thread started");
    }

    public void terminateThread() {
        this.kill.set(true);

        while (this.run.get()) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            for (TimerBatch batch : this.batches) {
                this.bridgeManager.processTimerBatch(batch);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.batches.clear();

        CastleGates.getPluginLogger().log(Level.INFO, "TimerWorker thread stopped");
    }

    public void run() {
        this.run.set(true);

        try {
            Map<Gearblock, TimerBatch> localBatchMap = new WeakHashMap<Gearblock, TimerBatch>();
            List<TimerBatch> localBatches = new ArrayList<TimerBatch>();

            while (!this.isInterrupted() && !this.kill.get()) {
                try {
                    long timeWait = this.lastExecute + CastleGates.getConfigManager().getTimerWorkerRate() - System.currentTimeMillis();
                    this.lastExecute = System.currentTimeMillis();
                    if (timeWait > 0) {
                        Thread.sleep(timeWait);
                    }

                    synchronized (this.batches) {
                        if (this.batches.size() > 0) {
                            for (TimerBatch batch : this.batches) {
                                localBatchMap.put(batch.getGearblock(), batch);
                            }

                            this.batches.clear();
                        }
                    }

                    long currentTimeMillis = System.currentTimeMillis();

                    localBatches.addAll(localBatchMap.values());

                    for (TimerBatch batch : localBatches) {
                        if (batch.getRunTimeMillis() <= currentTimeMillis) {
                            runBatch(batch);
                            localBatchMap.remove(batch.getGearblock());
                        }
                    }

                    localBatches.clear();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } finally {
            this.run.set(false);
        }
    }

    private void runBatch(final TimerBatch batch) {
    	final BridgeManager bridgeManager = this.bridgeManager;

		CastleGates.runTask(new Runnable() {
            public void run() {
            if(!batch.isInvalid() && bridgeManager.processTimerBatch(batch)) {
                BlockCoord blockCoord = batch.getGearblock().getCoord();
                Location location = new Location(batch.getWorld(), blockCoord.getX(), blockCoord.getY(), blockCoord.getZ());

                PowerResultHelper.playSound(location, batch.getProcessStatus());
            }
            }
        });
    }

    public void addBatch(TimerBatch batch) {
		synchronized(this.batches) {
			this.batches.add(batch);
		}
	}
}
