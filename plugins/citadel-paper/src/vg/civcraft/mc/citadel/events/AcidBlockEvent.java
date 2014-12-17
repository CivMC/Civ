package vg.civcraft.mc.citadel.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;

public class AcidBlockEvent extends Event implements Cancellable{
	private static final HandlerList handlers = new HandlerList();
	
	private Player p;
	private PlayerReinforcement acidBlock;
	private PlayerReinforcement destroyedBlock;
	public AcidBlockEvent(Player p, PlayerReinforcement acidBlock,
			PlayerReinforcement destroyedBlock){
		this.p = p;
		this.acidBlock = acidBlock;
		this.destroyedBlock = destroyedBlock;
	}
	/**
	 * Gets the PlayerReinforcement on the acid block.
	 * @return Returns the PlayerReinforcement.
	 */
	public PlayerReinforcement getAcidBlockReinforcement(){
		return acidBlock;
	}
	/**
	 * Gets the PlayerReinforcement on the block above the Acid Block.
	 * @return Returns the PlayerReinforcement for the block above the Acid Block.
	 */
	public PlayerReinforcement getDestoryedBlockReinforcement(){
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