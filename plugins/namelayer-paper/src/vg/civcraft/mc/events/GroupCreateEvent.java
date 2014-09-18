package vg.civcraft.mc.events;

import java.util.UUID;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import vg.civcraft.mc.group.GroupType;

// Use this to create the group into the database
// Afterwards grab the group from the groupmanager
public class GroupCreateEvent extends Event implements Cancellable{
	private static final HandlerList handlers = new HandlerList();
	
	private String groupName;
	private UUID uuid;
	private boolean cancelled = false;
	private String password;
	private GroupType type;
	
	public GroupCreateEvent(String groupName, UUID uuid, String password, GroupType type){
		this.groupName = groupName;
		this.uuid = uuid;
		this.password = password;
		this.type = type;
	}
	
	public void setGroupName(String groupName){
		this.groupName = groupName;
	}
	
	public void setOwner(UUID owner){
		uuid = owner;
	}
	
	public String getGroupName(){
		return groupName;
	}
	
	public UUID getOwner(){
		return uuid;
	}
	
	public boolean isCancelled(){
		return cancelled;
	}

	public HandlerList getHandlers() {
		// TODO Auto-generated method stub
		return handlers;
	}

	public void setCancelled(boolean cancel) {
		cancelled = cancel;
	}
	
	public void setPassword(String password){
		this.password = password;
	}
	
	public String getPassword(){
		return password;
	}
	
	public void setType(GroupType type){
		this.type = type;
	}
	
	public GroupType getType(){
		return type;
	}
}
