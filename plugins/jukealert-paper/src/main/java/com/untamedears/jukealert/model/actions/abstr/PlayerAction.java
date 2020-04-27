package com.untamedears.jukealert.model.actions.abstr;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.untamedears.jukealert.model.Snitch;

import vg.civcraft.mc.namelayer.NameAPI;

public abstract class PlayerAction extends SnitchAction {

	private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

	protected final UUID player;

	public PlayerAction(long time, Snitch snitch, UUID player) {
		super(time, snitch);
		this.player = player;
	}

	/**
	 * @return Player who commited the action
	 */
	public UUID getPlayer() {
		return player;
	}

	@Override
	public boolean hasPlayer() {
		return true;
	}

	protected String getFormattedTime() {
		return timeFormatter.format(LocalDateTime.ofEpochSecond(time / 1000, 0, ZoneOffset.UTC));
	}

	public String getPlayerName() {
		return NameAPI.getCurrentName(player);
	}

}
