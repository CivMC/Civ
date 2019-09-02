package vg.civcraft.mc.citadel.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import vg.civcraft.mc.citadel.model.Reinforcement;

/**
 * Called when a reinforcement is forcefully destroyed, meaning it is removed
 * and was not bypassed
 *
 */
public class ReinforcementDestructionEvent extends Event implements Cancellable {
	
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	private boolean isCancelled;
	private Reinforcement reinforcement;
	private float finalDamage;

	public ReinforcementDestructionEvent(Reinforcement reinforcement, float finalDamage) {
		this.finalDamage = finalDamage;
		this.reinforcement = reinforcement;
	}
	
	/**
	 * @return Reinforcement destroyed
	 */
	public Reinforcement getReinforcement() {
		return reinforcement;
	}
	
	/**
	 * @return How much damage was dealt in the killing blow to the reinforcement
	 */
	public float getFinalDamage() {
		return finalDamage;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
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
