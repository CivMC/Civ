package vg.civcraft.mc.namelayer.events;

import java.util.logging.Level;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.group.Group;

public class PromotePlayerEvent extends Event implements Cancellable{
	private static final HandlerList handlers = new HandlerList();
	
	private boolean finished;
	private boolean cancelled;
	private Player p;
	private Group g;
	private PlayerType c;
	private PlayerType f;

	public PromotePlayerEvent(Player p, Group g, PlayerType currentType, PlayerType futureType){
		this.p = p;
		this.g = g;
		this.c = currentType;
		this.f = futureType;
		NameLayerPlugin.log(Level.WARNING, "Promote Player Event Occured");
	}
	
	public Player getPlayer(){
		return p;
	}
	
	public Group getGroup(){
		return g;
	}
	
	public PlayerType getCurrentPlayerType(){
		return c;
	}
	
	public PlayerType getFuturePlayerType(){
		return f;
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
