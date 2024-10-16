package vg.civcraft.mc.citadel.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import vg.civcraft.mc.citadel.model.Reinforcement;

/**
 * Called when a block is destroyed by an acid block
 *
 */
public class ReinforcementAcidBlockedEvent extends ReinforcementEvent {
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private Reinforcement acidBlock;

	public ReinforcementAcidBlockedEvent(Player player, Reinforcement acidBlock, Reinforcement destroyedBlock) {
		super(player, destroyedBlock);
		this.acidBlock = acidBlock;
	}

	/**
	 * Gets the reinforcement of the acid block.
	 * 
	 * @return Acid block reinforcement
	 */
	public Reinforcement getAcidBlockReinforcement() {
		return acidBlock;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
