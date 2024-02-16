package vg.civcraft.mc.citadel.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import vg.civcraft.mc.citadel.model.Reinforcement;

/**
 * Called when a reinforcement is damaged by a player, but not when it is damaged by other entities or natural causes
 *
 */
public class ReinforcementDamageEvent extends ReinforcementEvent {

	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private float damageDone;

	public ReinforcementDamageEvent(Player player, Reinforcement rein, float damageDone) {
		super(player, rein);
		this.damageDone = damageDone;
	}

	/**
	 * Gets the total damage done by this damaging event
	 * 
	 * @return Total damage
	 */
	public float getDamageDone() {
		return damageDone;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	/**
	 * Sets the total damage done
	 * 
	 * @param damageDone Damage to do
	 */
	public void setDamageDone(float damageDone) {
		this.damageDone = damageDone;
	}
}
