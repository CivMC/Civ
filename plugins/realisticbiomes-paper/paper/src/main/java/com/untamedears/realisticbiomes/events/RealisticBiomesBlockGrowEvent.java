package com.untamedears.realisticbiomes.events;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockGrowEvent;

public final class RealisticBiomesBlockGrowEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private BlockGrowEvent wrapped;

	public RealisticBiomesBlockGrowEvent(Block block, BlockState newState) {
		super(false);
		wrapped = new BlockGrowEvent(block, newState);
	}

	public BlockGrowEvent getEvent() {
		return wrapped;
	}

	@Override
	public HandlerList getHandlers() {
		return RealisticBiomesBlockGrowEvent.handlers;
	}
}
