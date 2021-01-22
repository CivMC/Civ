package isaac.bastion.event;

import isaac.bastion.BastionBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Event that gets fired when a bastion is damaged
 * @author Gordon
 */
public class BastionDamageEvent extends PlayerEvent implements Cancellable {
	public enum Cause { BLOCK_PLACED, PEARL, ELYTRA }
	
	private final BastionBlock bastion;
	private final Cause cause;
	private final double damage;
	
	private boolean cancelled;
	
	// Handler list for spigot events
	private static final HandlerList handlers = new HandlerList();
	
	/**
	 * Creates a new BastionDamageEvent instance
	 * @param bastion The damaged bastion instance
	 * @param player The damaging player
	 * @param cause The damage cause
	 */
	public BastionDamageEvent(final BastionBlock bastion, final Player player, final Cause cause, final double damage) {
		super(player);
		this.bastion = bastion;
		this.cause = cause;
		this.damage = damage;
	}
	
	/**
	 * Gets the damaged bastion
	 * @return The damaged bastion
	 */
	public BastionBlock getBastion() {
		return bastion;
	}
	
	/**
	 * Gets the damage cause
	 * @return The damage cause
	 */
	public Cause getCause() {
		return cause;
	}
	
	/**
	* Gets the amount of damage to the bastion
	* @return The amount of damage
	*/
	public double getDamage() {
		return damage;
	}
	
	/**
	 * Gets whether the event is cancelled
	 * @return true if the event is cancelled
	 */
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	/**
	 * Sets whether the event is cancelled
	 * @param cancelled whether the event is cancelled
	 */
	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
	@Override
	public HandlerList getHandlers() {
	    return handlers;
	}
	
	public static HandlerList getHandlerList() {
	    return handlers;
	}
}
