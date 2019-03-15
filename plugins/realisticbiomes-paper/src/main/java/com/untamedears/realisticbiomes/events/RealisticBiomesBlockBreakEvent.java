package com.untamedears.realisticbiomes.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;

public class RealisticBiomesBlockBreakEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	
	private BlockBreakEvent wrapped;
	
	public RealisticBiomesBlockBreakEvent(Block theBlock, Player player) {
		super(false);
		wrapped = new BlockBreakEvent(theBlock, player);
	}
	
	@Override
	public HandlerList getHandlers() {
		return RealisticBiomesBlockBreakEvent.handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	public BlockBreakEvent getEvent() {
		return wrapped;
	}

}
