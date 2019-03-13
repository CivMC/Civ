package vg.civcraft.mc.citadel.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.namelayer.group.Group;

/**
 * Called when the group a reinforcement is on is changed
 *
 */
public class ReinforcementGroupChangeEvent extends ReinforcementEvent {
	
	private static final HandlerList handlers = new HandlerList();
	
	private Group newGroup;

	public ReinforcementGroupChangeEvent(Player who, Reinforcement reinforcement, Group newGroup) {
		super(who, reinforcement);
		this.newGroup = newGroup;
	}

	public Group getNewGroup() {
		return newGroup;
	}
	

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
