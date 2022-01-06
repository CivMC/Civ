package vg.civcraft.mc.namelayer.events;

import java.util.UUID;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GroupRemoveInvitation extends Event implements Cancellable{
	private static final HandlerList handlers = new HandlerList();
	
	private String groupName;
	private UUID invitedPlayer;
	private boolean cancelled = false;
	
	public GroupRemoveInvitation(String groupName, UUID invitedPlayer){
		this.groupName = groupName;
		this.invitedPlayer = invitedPlayer;
	}
	
	/**
	 * @return The group name.
	 */
	public String getGroupName(){
		return groupName;
	}
	
	/**
	 * @return The invited player's uuid..
	 */
	public UUID getInvitedPlayer(){
		return invitedPlayer;
	}
	
	public boolean isCancelled(){
		return cancelled;
	}
	
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
	    return handlers;
	}

}
