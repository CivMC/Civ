package com.untamedears.JukeAlert.model.actions;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.namelayer.NameAPI;

public abstract class PlayerAction extends LoggedSnitchAction {

	private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

	private final UUID player;

	public PlayerAction(long time, UUID player) {
		super(time);
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

	protected void enrichGUIItem(ItemStack item) {
		ISUtils.addLore(item, String.format("%sPlayer: %s", ChatColor.GOLD, getPlayerName()),
				String.format("%sTime: %s", ChatColor.LIGHT_PURPLE,
						timeFormatter.format(LocalDateTime.ofEpochSecond(time / 1000, 0, ZoneOffset.UTC))));
	}

	public String getPlayerName() {
		return NameAPI.getCurrentName(player);
	}

}
