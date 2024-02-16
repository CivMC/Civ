package vg.civcraft.mc.citadel.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import vg.civcraft.mc.citadel.model.Reinforcement;

/**
 * Called when a reinforcement is destroyed and the block reinforced is going to drop
 *
 */
public class ReinforcedBlockBreak extends ReinforcementEvent {
	
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	private BlockBreakEvent blockEvent;

	public ReinforcedBlockBreak(Player who, Reinforcement reinforcement, BlockBreakEvent event) {
		super(who, reinforcement);
		this.blockEvent = event;
	}
	
	/**
	 * @return The BlockBreakEvent which caused the destrution of the reinforcement
	 */
	public BlockBreakEvent getWrappedBlockBreakEvent() {
		return blockEvent;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

}
