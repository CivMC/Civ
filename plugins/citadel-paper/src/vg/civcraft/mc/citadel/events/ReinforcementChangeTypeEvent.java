package vg.civcraft.mc.citadel.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;

public class ReinforcementChangeTypeEvent extends Event implements Cancellable{	
	private static final HandlerList handlers = new HandlerList();
	
	private Reinforcement rein;
	private ReinforcementType newType;
	private Player player;
	
	private boolean isCancelled = false;
	
	public ReinforcementChangeTypeEvent(Reinforcement rein, ReinforcementType newType, Player p){
		this.rein = rein;
		this.newType = newType;
		this.player = p;
	}
	/**
	 * Gets the Reinforcement that was just modified.
	 * @return Returns the Reinforcement.
	 */
	public Reinforcement getReinforcement(){
		return rein;
	}
	/**
	 * 
	 * @return Future reinforcement type
	 */
	public ReinforcementType getNewType(){
		return newType;
	}
	/**
	 * @return Returns the Player that modified the Reinforcement.
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
