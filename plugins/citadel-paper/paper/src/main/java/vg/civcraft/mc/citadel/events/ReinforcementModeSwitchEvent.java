package vg.civcraft.mc.citadel.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import vg.civcraft.mc.citadel.playerstate.AbstractPlayerState;

/**
 * Called when a player changes his reinforcement mode. Not called when the
 * player attempt to switch, but stays in the same mode
 *
 */
public class ReinforcementModeSwitchEvent extends PlayerEvent implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private AbstractPlayerState oldState;
	private AbstractPlayerState newState;
	private boolean isCancelled;

	public ReinforcementModeSwitchEvent(Player who, AbstractPlayerState oldState, AbstractPlayerState newState) {
		super(who);
		this.oldState = oldState;
		this.newState = newState;
		isCancelled = false;
	}

	/**
	 * @return Reinforcement mode before the change
	 */
	public AbstractPlayerState getOldMode() {
		return oldState;
	}

	/**
	 * @return Reinforcement mode after the change
	 */
	public AbstractPlayerState getNewMode() {
		return newState;
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
}
