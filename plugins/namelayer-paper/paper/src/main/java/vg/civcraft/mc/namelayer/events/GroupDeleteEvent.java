package vg.civcraft.mc.namelayer.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import vg.civcraft.mc.namelayer.group.Group;

public class GroupDeleteEvent extends Event implements Cancellable{
	private static final HandlerList handlers = new HandlerList();
	
	private Group group;
	private boolean cancelled;
	private boolean finished;
	
	public GroupDeleteEvent(Group group, boolean finished){
		this.group = group;
		this.finished = finished;
	}
	/**
	 * Sets the group to be deleted.
	 * @param group- The group to be deleted.
	 */
	public void setGroup(Group group){
		this.group = group;
	}
	/**
	 * Gets the group that is deleted.
	 * @return Returns the group that is to be deleted.
	 */
	public Group getGroup(){
		return group;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean value) {
		cancelled = value;
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
