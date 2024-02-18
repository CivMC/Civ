package com.untamedears.jukealert.model.actions.impl;

import com.untamedears.jukealert.events.PlayerLogoutSnitchEvent;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerAction;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;

public class LogoutAction extends LoggablePlayerAction {

	public static final String ID = "LOGOUT";

	public LogoutAction(long time, Snitch snitch, UUID player) {
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
		Bukkit.getPluginManager().callEvent(new PlayerLogoutSnitchEvent(snitch, Bukkit.getPlayer(player)));
	}

	@Override
	public String getChatRepresentationIdentifier() {
		return ChatColor.BOLD + "Logout";
	}
}
