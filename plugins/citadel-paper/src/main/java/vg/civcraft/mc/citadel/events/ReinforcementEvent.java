package vg.civcraft.mc.citadel.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.player.PlayerEvent;
import vg.civcraft.mc.citadel.model.Reinforcement;

public abstract class ReinforcementEvent extends PlayerEvent implements Cancellable {

	protected Reinforcement reinforcement;
	protected boolean isCancelled;

	public ReinforcementEvent(Player who, Reinforcement reinforcement) {
		super(who);
		this.reinforcement = reinforcement;
		isCancelled = false;
	}

	/**
	 * @return Reinforcement involved in this event
	 */
	public Reinforcement getReinforcement() {
		return reinforcement;
	}

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean value) {
		isCancelled = value;
	}
}
