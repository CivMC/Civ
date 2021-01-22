package vg.civcraft.mc.namelayer.events;

import java.util.UUID;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;

public class GroupAddInvitation extends Event implements Cancellable{
	private static final HandlerList handlers = new HandlerList();
	
	private String groupName;
	private PlayerType type;
	private UUID invitedPlayer;
	private UUID inviter; 
	private boolean cancelled = false;
	
	public GroupAddInvitation(String groupName, PlayerType type, UUID invitedPlayer, UUID inviter){
		this.groupName = groupName;
		this.type = type;
		this.invitedPlayer = invitedPlayer;
		this.inviter = inviter;
	}
	
	/**
	 * @return The group name.
	 */
	public String getGroupName(){
		return groupName;
	}
	
	/**
	 * @return The player type in the group the player was invited to.
	 */
	public PlayerType getPlayerType(){
		return type;
	}

	/**
	 * @return The inviter's uuid..
	 */
	public UUID getInviter(){
		return inviter;
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
