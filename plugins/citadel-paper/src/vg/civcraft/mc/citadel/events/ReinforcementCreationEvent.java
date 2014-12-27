package vg.civcraft.mc.citadel.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import vg.civcraft.mc.citadel.reinforcement.Reinforcement;

public class ReinforcementCreationEvent extends Event implements Cancellable{	
	private static final HandlerList handlers = new HandlerList();
	
	private Reinforcement rein;
	private Block bl;
	private Player player;
	
	private boolean isCancelled = false;
	
	public ReinforcementCreationEvent(Reinforcement rein, Block block, Player p){
		this.rein = rein;
		bl = block;
		player = p;
	}
	/**
	 * Gets the Reinforcement that was just created.
	 * @return Returns the Reinforcement.
	 */
	public Reinforcement getReinforcement(){
		return rein;
	}
	/**
	 * Gets the Block that the Reinforcement was created on.
	 * @return Returns the Block associated with the reinforcement.
	 */
	public Block getBlock(){
		return bl;
	}
	/**
	 * @return Returns the Player that created the Reinforcement.
	 */
	public Player getPlayer(){
		return player;
	}

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
