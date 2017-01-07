/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import org.bukkit.Location;

import com.aleksey.castlegates.CastleGates;
import com.aleksey.castlegates.manager.GearManager;
import com.aleksey.castlegates.types.BlockCoord;
import com.aleksey.castlegates.types.Gearblock;
import com.aleksey.castlegates.types.TimerBatch;

public class TimerWorker extends Thread implements Runnable {
	private GearManager gearManager;
	private List<TimerBatch> batches;

	private long lastExecute = System.currentTimeMillis();
    private AtomicBoolean kill = new AtomicBoolean(false);

    public TimerWorker(GearManager gearManager) {
    	this.gearManager = gearManager;
    	this.batches = new ArrayList<TimerBatch>();
	}
	
    public void startThread() {
        setName("CastleGates TimerWorker Thread");
        setPriority(Thread.MIN_PRIORITY);
        start();
        
        CastleGates.getPluginLogger().log(Level.INFO, "TimerWorker thread started");
    }

    public void terminateThread() {
        this.kill.set(true);
    }
		
    public void run() {
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
                	if(this.batches.size() > 0) {
                		for(TimerBatch batch : this.batches) {
              				localBatchMap.put(batch.getGearblock(), batch);
                		}
                		
	                	this.batches.clear();
                	}
                }
                
                long currentTimeMillis = System.currentTimeMillis();
                
                localBatches.addAll(localBatchMap.values());
                
                for(TimerBatch batch : localBatches) {
                	if(batch.getRunTimeMillis() <= currentTimeMillis) {
                		runBatch(batch);
                		localBatchMap.remove(batch.getGearblock());
                	}
                }
                
                localBatches.clear();
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
    }
    
    private void runBatch(final TimerBatch batch) {
    	final GearManager gearManager = this.gearManager; 
    	
		CastleGates.runTask(new Runnable() {
            public void run() {
            	if(gearManager.processTimerBatch(batch)) {
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