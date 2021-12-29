package vg.civcraft.mc.citadel.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import vg.civcraft.mc.citadel.model.Reinforcement;

/**
 * Called when a reinforcement is repaired in patch mode
 *
 */
public class ReinforcementRepairEvent extends ReinforcementEvent {
	
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public ReinforcementRepairEvent(Player who, Reinforcement reinforcement) {
		super(who, reinforcement);
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

}
