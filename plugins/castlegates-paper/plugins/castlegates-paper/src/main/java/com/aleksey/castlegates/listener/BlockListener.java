/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;

import com.aleksey.castlegates.CastleGates;

public class BlockListener implements Listener {
	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onBlockBreak(BlockBreakEvent event) {
		CastleGates.getManager().handleBlockBreak(event);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=true)
    public void onBlockRedstone(BlockRedstoneEvent event) {
		CastleGates.getManager().handleBlockRedstone(event);
	}

	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onBlockPhysics(BlockPhysicsEvent event) {
		CastleGates.getManager().handleBlockPhysics(event);
	}

	@EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
	public void onBlockPistonExtend(BlockPistonExtendEvent event) {
		CastleGates.getManager().handlePistonEvent(event.getBlocks());
	}

	@EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
	public void onBlockPistonRetract(BlockPistonRetractEvent event) {
		CastleGates.getManager().handlePistonEvent(event.getBlocks());
	}
}
