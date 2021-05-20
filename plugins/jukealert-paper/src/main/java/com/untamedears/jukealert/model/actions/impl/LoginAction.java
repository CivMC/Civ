package com.untamedears.jukealert.model.actions.impl;

import com.untamedears.jukealert.events.PlayerLoginSnitchEvent;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerAction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;

import java.util.UUID;

public class LoginAction extends LoggablePlayerAction {

	public static final String ID = "LOGIN";

	public LoginAction(long time, Snitch snitch, UUID player) {
		super(time, snitch, player);
	}

	@Override
	public String getIdentifier() {
		return ID;
	}

	@Override
	public IClickable getGUIRepresentation() {
		return getEnrichedClickableSkullFor(getPlayer());
	}

	@Override
	public void accept(Snitch s) {
		Bukkit.getPluginManager().callEvent(new PlayerLoginSnitchEvent(snitch, Bukkit.getPlayer(player)));
	}

	@Override
	protected String getChatRepresentationIdentifier() {
		return ChatColor.BOLD + "Login";
	}
}
