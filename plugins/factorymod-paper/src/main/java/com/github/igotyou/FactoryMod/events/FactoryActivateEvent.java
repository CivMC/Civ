package com.github.igotyou.FactoryMod.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.github.igotyou.FactoryMod.factories.Factory;

/**
 * Event called when any type of factory is being activated. Cancelling this
 * event will prevent the factory from starting up, no additional message will
 * be sent to the player informing him about the cancelling, this will be left
 * up to the listener cancelling the activation
 *
 */
public class FactoryActivateEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private Factory fac;
	private Player activator;
	
	private boolean cancelled;

	public FactoryActivateEvent(Factory f, Player activator) {
		this.fac = f;
		this.activator = activator;
	}

	/**
	 * @return The factory being activated
	 */
	public Factory getFactory() {
		return fac;
	}

	/**
	 * @return The player activating the factory or null if it was not activated by
	 *         a player
	 */
	public Player getActivator() {
		return activator;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;		
	}
}
