package vg.civcraft.mc.namelayer.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.entity.Player;;


public class PromotePlayerEvent extends Event implements Cancellable{
	private static final HandlerList handlers = new HandlerList();
	
	private boolean finished;
	private boolean cancelled;
	private Player p;

	public PromotePlayerEvent(Player p){
		this.p = p;
	}
	
	public Player getPlayer(){
		return p;
	}
	
	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean setvalue) {
		cancelled = setvalue;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
	public void setHasFinished(boolean value){
		finished = value;
	}
	
	public boolean hasFinished(){
		return finished;
	}

}
