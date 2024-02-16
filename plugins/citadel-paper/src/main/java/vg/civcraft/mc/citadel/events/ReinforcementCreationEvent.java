package vg.civcraft.mc.citadel.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import vg.civcraft.mc.citadel.model.Reinforcement;

/**
 * Called when a new reinforcement is created
 *
 */
public class ReinforcementCreationEvent extends ReinforcementEvent {

	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public ReinforcementCreationEvent(Player p, Reinforcement rein) {
		super(p, rein);
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
