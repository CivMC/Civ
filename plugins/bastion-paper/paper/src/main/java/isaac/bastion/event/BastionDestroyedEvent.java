package isaac.bastion.event;

import isaac.bastion.BastionBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Event that gets fired when a bastion is broken
 * @author biggestnerd
 */
public class BastionDestroyedEvent extends PlayerEvent {
	
	private final BastionBlock bastion;
	
	//Handler list for spigot events
	private static final HandlerList handlers = new HandlerList();
	
	/**
	 * Created a new BastionDestroyedEvent instance
	 * @param bastion The destroyed bastion
	 * @param player The destroying player
	 */
	public BastionDestroyedEvent(BastionBlock bastion, Player player) {
		super(player);
		this.bastion = bastion;
	}
	
	/**
	 * Gets the destroyed bastion
	 * @return The destroyed bastion
	 */
	public BastionBlock getBastion() {
		return bastion;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
