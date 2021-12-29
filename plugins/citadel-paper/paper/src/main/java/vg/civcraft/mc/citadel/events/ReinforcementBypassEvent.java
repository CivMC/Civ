package vg.civcraft.mc.citadel.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import vg.civcraft.mc.citadel.model.Reinforcement;

/**
 * Called when a player bypasses a reinforcement they have access to
 *
 */
public class ReinforcementBypassEvent extends ReinforcementEvent {

	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public ReinforcementBypassEvent(Player who, Reinforcement reinforcement) {
		super(who, reinforcement);
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

}
