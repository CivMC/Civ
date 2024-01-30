package com.programmerdan.minecraft.civspy.listeners.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.aleksey.castlegates.events.CastleGatesDrawGateEvent;
import com.aleksey.castlegates.events.CastleGatesUndrawGateEvent;
import com.programmerdan.minecraft.civspy.DataManager;
import com.programmerdan.minecraft.civspy.DataSample;
import com.programmerdan.minecraft.civspy.PointDataSample;
import com.programmerdan.minecraft.civspy.listeners.ServerDataListener;

/**
 * Contributes <code>castlegates.draw</code> stats when a gate is withdrawn and <code>castlegates.undraw</code> when 
 * a gate is extended. For now, basic utilization tracking. Maybe later on, better tracking.
 * 
 * @author ProgrammerDan
 *
 */
public class CastleGatesListener extends ServerDataListener {

	public CastleGatesListener(DataManager target, Logger logger, String server) {
		super(target, logger, server);
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onGateRetractEvent(CastleGatesDrawGateEvent event) {
		try {
			if ( event.getImpacted() != null && !event.getImpacted().isEmpty()) { 
				Chunk chunk = event.getImpacted().get(0).getChunk();
				
				DataSample rgate = new PointDataSample("castlegates.draw", this.getServer(),
						chunk.getWorld().getName(), null,
						chunk.getX(), chunk.getZ(), 1);
				this.record(rgate);
			} else {
				DataSample rgate = new PointDataSample("castlegates.draw", this.getServer(),
						null, null, null, null, 1);
				this.record(rgate);
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to spy an castlegates draw event", e);
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onGateExpandEvent(CastleGatesUndrawGateEvent event) {
		try {
			if ( event.getImpacted() != null && !event.getImpacted().isEmpty()) { 
				Chunk chunk = event.getImpacted().get(0).getChunk();
				
				DataSample rgate = new PointDataSample("castlegates.undraw", this.getServer(),
						chunk.getWorld().getName(), null,
						chunk.getX(), chunk.getZ(), 1);
				this.record(rgate);
			} else {
				DataSample rgate = new PointDataSample("castlegates.undraw", this.getServer(),
						null, null, null, null, 1);
				this.record(rgate);
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to spy an castlegates undraw event", e);
		}
	}
	
	@Override
	public void shutdown() {
		// NO-OP
	}

}
