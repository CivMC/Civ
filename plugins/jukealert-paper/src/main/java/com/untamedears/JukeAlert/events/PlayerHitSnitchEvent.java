package com.untamedears.JukeAlert.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import com.untamedears.JukeAlert.model.Snitch;

public class PlayerHitSnitchEvent extends PlayerEvent {

	private static final HandlerList handlers = new HandlerList();

	private Snitch snitch;

	public PlayerHitSnitchEvent(Snitch snitch, Player player) {
		super(player);
		this.snitch = snitch;
	}

	public Snitch getSnitch() {
		return snitch;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
