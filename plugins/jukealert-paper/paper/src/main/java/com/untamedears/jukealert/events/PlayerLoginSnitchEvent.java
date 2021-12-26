package com.untamedears.jukealert.events;

import com.untamedears.jukealert.model.Snitch;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Called when a player logs in next to a snitch and triggers a notification
 *
 */
public class PlayerLoginSnitchEvent extends PlayerEvent {

	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private Snitch snitch;

	public PlayerLoginSnitchEvent(Snitch snitch, Player player) {
		super(player);
		this.snitch = snitch;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public Snitch getSnitch() {
		return snitch;
	}

}
