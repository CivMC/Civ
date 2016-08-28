package com.programmerdan.minecraft.civspy.listeners;

import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

import com.programmerdan.minecraft.civspy.DataManager;
import com.programmerdan.minecraft.civspy.DataSample;
import com.programmerdan.minecraft.civspy.PointDataSample;

public final class BreakListener extends ServerDataListener {

	public BreakListener(DataManager target, Logger logger, String server) {
		super(target, logger, server);
	}
	
	@Override
	public void shutdown() {
		// NO-OP
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void BreakListen(BlockBreakEvent event) {
		Player p = event.getPlayer();
		if (p == null) return;
		UUID id = p.getUniqueId();
		Block broken = event.getBlock();
		Chunk chunk = broken.getChunk();
		
		StringBuilder blockName = new StringBuilder(broken.getType().toString());
		blockName.append(":").append(broken.getData());

		DataSample blockBreak = new PointDataSample("player.blockbreak", this.getServer(),
				chunk.getWorld().getName(), id, chunk.getX(), chunk.getZ(), blockName.toString());
			this.record(blockBreak);
	}

}
