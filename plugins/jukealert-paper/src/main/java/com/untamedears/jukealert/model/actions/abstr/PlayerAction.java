package com.untamedears.jukealert.model.actions.abstr;

import com.untamedears.jukealert.model.Snitch;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.namelayer.NameAPI;

public abstract class PlayerAction extends SnitchAction {
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

	protected String getFormattedTime(
		final @NotNull DateTimeFormatter formatter
	) {
		return formatter.format(Instant.ofEpochMilli(this.time).atOffset(ZoneOffset.UTC));
	}

	public String getPlayerName() {
		return NameAPI.getCurrentName(player);
	}

}
