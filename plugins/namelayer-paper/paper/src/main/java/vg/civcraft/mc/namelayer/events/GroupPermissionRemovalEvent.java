package vg.civcraft.mc.namelayer.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class GroupPermissionRemovalEvent extends Event implements Cancellable{
	
	private static final HandlerList handlers = new HandlerList();
	
	private boolean isCancelled = false;
	private Group group;
	private PlayerType playerType;
	private PermissionType permission;
	
	public GroupPermissionRemovalEvent(Group group, PlayerType playerType, PermissionType permission){
		this.group = group;
		this.playerType = playerType;
		this.permission = permission;
	}
	
	/**
	 * @return Group for which permission was removed
	 */
	public Group getGroup(){
		return group;
	}
	
	/**
	 * @return Player type from which the permission was taken
	 */
	public PlayerType getPlayerType() {
		return playerType;
	}
	
	/**
	 * @return Permission taken
	 */
	public PermissionType getPermission() {
		return permission;
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
