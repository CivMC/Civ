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
	private BridgeManager _bridgeManager;
	private List<TimerBatch> _batches;

	private long _lastExecute = System.currentTimeMillis();
    private AtomicBoolean _kill = new AtomicBoolean(false);
    private AtomicBoolean _run = new AtomicBoolean(false);

    public TimerWorker(BridgeManager bridgeManager) {
    	_bridgeManager = bridgeManager;
    	_batches = new ArrayList<TimerBatch>();
	}

    public void startThread() {
        _kill.set(false);

        setName("CastleGates TimerWorker Thread");
        setPriority(Thread.MIN_PRIORITY);
        start();

        CastleGates.getPluginLogger().log(Level.INFO, "TimerWorker thread started");
    }

    public void terminateThread() {
        _kill.set(true);

        while (_run.get()) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            for (TimerBatch batch : _batches) {
                _bridgeManager.processTimerBatch(batch);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        _batches.clear();

        CastleGates.getPluginLogger().log(Level.INFO, "TimerWorker thread stopped");
    }

    public void run() {
        _run.set(true);

        try {
            Map<Gearblock, TimerBatch> localBatchMap = new HashMap<Gearblock, TimerBatch>();
            List<TimerBatch> localBatches = new ArrayList<TimerBatch>();

            while (!isInterrupted() && !_kill.get()) {
                try {
                    long timeWait = _lastExecute + CastleGates.getConfigManager().getTimerWorkerRate() - System.currentTimeMillis();
                    _lastExecute = System.currentTimeMillis();
                    if (timeWait > 0) {
                        Thread.sleep(timeWait);
                    }

                    synchronized (_batches) {
                        if (_batches.size() > 0) {
                            for (TimerBatch batch : _batches) {
                                localBatchMap.put(batch.getGearblock(), batch);
                            }

                            _batches.clear();
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
            _run.set(false);
        }
    }

    private void runBatch(final TimerBatch batch) {
    	final BridgeManager bridgeManager = _bridgeManager;

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
		synchronized(_batches) {
			_batches.add(batch);
		}
	}
}
