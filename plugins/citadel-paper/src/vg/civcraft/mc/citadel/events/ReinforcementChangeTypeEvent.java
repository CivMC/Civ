package vg.civcraft.mc.citadel.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;

/**
 * Called when a reinforcements type is changed
 *
 */
public class ReinforcementChangeTypeEvent extends ReinforcementEvent {

	private static final HandlerList handlers = new HandlerList();

	private ReinforcementType newType;

	public ReinforcementChangeTypeEvent(Player p, Reinforcement rein, ReinforcementType newType) {
		super(p, rein);
		this.newType = newType;
	}

	/**
	 * 
	 * @return Future reinforcement type
	 */
	public ReinforcementType getNewType() {
		return newType;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
