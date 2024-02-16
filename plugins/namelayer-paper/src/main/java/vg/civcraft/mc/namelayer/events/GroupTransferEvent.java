package vg.civcraft.mc.namelayer.events;

import java.util.UUID;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import vg.civcraft.mc.namelayer.group.Group;

public class GroupTransferEvent extends Event implements Cancellable{
	private static final HandlerList handlers = new HandlerList();
	
	private Group group;
	private UUID toUser;
	private boolean isCancelled = false;
	public GroupTransferEvent(Group group, UUID player){
		this.group = group;
		toUser = player;
	}
	/**
	 * @return Returns the Group that is being transfered.
	 */
	public Group getGroup(){
		return group;
	}
	/**
	 * @return Returns the new Owner.
	 */
	public UUID getNewOwner(){
		return toUser;
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
