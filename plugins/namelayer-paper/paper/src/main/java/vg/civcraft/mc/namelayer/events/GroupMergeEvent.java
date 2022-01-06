package vg.civcraft.mc.namelayer.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import vg.civcraft.mc.namelayer.group.Group;

public class GroupMergeEvent extends Event implements Cancellable{
	private static final HandlerList handlers = new HandlerList();
	
	private boolean isCancelled = false;
	private Group beingMerged; // the group that will join into another
	private Group mergingInto; // the group that is receiving the other
	private boolean finished;
	
	public GroupMergeEvent(Group group, Group toBeMerged, boolean finished){
		mergingInto = group;
		beingMerged = toBeMerged;
		this.finished = finished;
	}
	/**
	 * @return Returns the group to be merged.
	 */
	public Group getToBeMerged(){
		return beingMerged;
	}
	/**
	 * @return Returns the group that will be left after the merging.
	 */
	public Group getMergingInto(){
		return mergingInto;
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
	
	public void setHasFinished(boolean value){
		finished = value;
	}
	
	public boolean hasFinished(){
		return finished;
	}

}
