package com.untamedears.realisticbiomes.events;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.world.StructureGrowEvent;

public class RealisticBiomesStructureGrowEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private StructureGrowEvent wrapped;

	public RealisticBiomesStructureGrowEvent(Location location, TreeType species, boolean bonemeal, Player player,
			List<BlockState> blocks) {
		super(false);
		wrapped = new StructureGrowEvent(location, species, bonemeal, player, blocks);
	}

	public StructureGrowEvent getEvent() {
		return wrapped;
	}

	@Override
	public HandlerList getHandlers() {
		return RealisticBiomesStructureGrowEvent.handlers;
	}
}
