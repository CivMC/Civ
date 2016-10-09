package vg.civcraft.mc.citadel.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;

/**
 * Event that is called when an acid block action is performed
 *
 */
public class AcidBlockEvent extends PlayerEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private final PlayerReinforcement acidBlock;
	private final PlayerReinforcement destroyedBlock;

	/**
	 * Creates a new AcidBlockEvent instance
	 * @param player The player performing the acid action
	 * @param acidBlock The acid block instance
	 * @param destroyedBlock The destroyed block instance
	 */
	public AcidBlockEvent(final Player player, final PlayerReinforcement acidBlock,
			PlayerReinforcement destroyedBlock) {
		super(player);

		this.acidBlock = acidBlock;
		this.destroyedBlock = destroyedBlock;
	}

	/**
	 * Gets the PlayerReinforcement on the acid block.
	 * @return Returns the PlayerReinforcement.
	 */
	public PlayerReinforcement getAcidBlockReinforcement() {
		return acidBlock;
	}

	/**
	 * Gets the PlayerReinforcement on the block above the Acid Block.
	 * @return Returns the PlayerReinforcement for the block above the Acid Block.
	 */
	public PlayerReinforcement getDestroyedBlockReinforcement() {
		return destroyedBlock;
	}
	
	/**
	 * @Deprecated
	 * @return Returns the PlayerReinforcement for the block above the Acid Block.
	 * @deprecated Use getDestroyedBlockReinforcement instead.
	 */
	@Deprecated
	public PlayerReinforcement getDestoryedBlockReinforcement() {
		return destroyedBlock;
	}

	private boolean isCancelled = false;
	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean value) {
		isCancelled = value;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}