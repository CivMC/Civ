package vg.civcraft.mc.citadel.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import vg.civcraft.mc.citadel.reinforcement.Reinforcement;

public class ReinforcementDamageEvent  extends Event implements Cancellable{
	private static final HandlerList handlers = new HandlerList();
	
	private boolean isCancelled = false;
	private Reinforcement rein;
	private Player player;
	private Block block;
	public ReinforcementDamageEvent(Reinforcement rein, Player player, Block block){
		this.rein = rein;
		this.player = player;
		this.block = block;
	}
	/**
	 * @return Returns the Reinforcement.
	 */
	public Reinforcement getReinforcement(){
		return rein;
	}
	/**
	 * @return Returns the block.
	 */
	public Block getBlock(){
		return block;
	}
	/**
	 * @return Returns the Player who damaged the Reinforcement.
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
