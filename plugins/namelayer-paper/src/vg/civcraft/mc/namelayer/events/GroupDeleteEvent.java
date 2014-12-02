package vg.civcraft.mc.namelayer.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import vg.civcraft.mc.namelayer.group.Group;

public class GroupDeleteEvent extends Event implements Cancellable{
	private static final HandlerList handlers = new HandlerList();
	
	private Group group;
	private boolean cancelled;
	
	public GroupDeleteEvent(Group group){
		this.group = group;
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

	public HandlerList getHandlers() {
		return handlers;
	}
}
