package vg.civcraft.mc.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GroupDeleteEvent extends Event implements Cancellable{
	private static final HandlerList handlers = new HandlerList();
	
	private String groupName;
	private boolean cancelled;
	
	public GroupDeleteEvent(String groupName){
		this.groupName = groupName;
	}
	
	public void setGroupName(String groupName){
		this.groupName = groupName;
	}
	
	public String getGroupName(){
		return groupName;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean value) {
		cancelled = value;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

}
