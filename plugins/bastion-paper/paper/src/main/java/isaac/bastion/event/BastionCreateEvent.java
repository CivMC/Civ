package isaac.bastion.event;

import isaac.bastion.BastionBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Event that gets fired when a bastion is created
 * @author Gordon
 */
public class BastionCreateEvent extends PlayerEvent implements Cancellable {
	
	private final BastionBlock bastion;
	
	private boolean cancelled;
	
	// Handler list for spigot events
	private static final HandlerList handlers = new HandlerList();
	
	/**
	 * Creates a new BastionCreateEvent instance
	 * @param bastion The bastion instance
	 * @param player The player creating the bastion
	 */
	public BastionCreateEvent(final BastionBlock bastion, final Player player) {
		super(player);
		this.bastion = bastion;
	}
	
	/**
	 * Gets the created bastion
	 * @return The created bastion
	 */
	public BastionBlock getBastion() {
		return bastion;
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
