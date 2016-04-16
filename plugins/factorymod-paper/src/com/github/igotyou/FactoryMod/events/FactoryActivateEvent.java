package com.github.igotyou.FactoryMod.events;

import org.bukkit.entity.Player;

import vg.civcraft.mc.civmodcore.interfaces.CustomEvent;

import com.github.igotyou.FactoryMod.factories.Factory;

/**
 * Event called when any type of factory is being activated. Cancelling this
 * event will prevent the factory from starting up, no additional message will
 * be sent to the player informing him about the cancelling, this will be left
 * up to the listener cancelling the activation
 *
 */
public class FactoryActivateEvent extends CustomEvent {
	private Factory fac;
	private Player activator;

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
	 * @return The player activating the factory or null if it was not activated
	 *         by a player
	 */
	public Player getActivator() {
		return activator;
	}
}
