package com.untamedears.jukealert.model.actions.impl;

import com.untamedears.jukealert.events.PlayerHitSnitchEvent;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerAction;
import java.util.UUID;
import org.bukkit.Bukkit;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;

public class EnterFieldAction extends LoggablePlayerAction {

	public static final String ID = "ENTRY";

	public EnterFieldAction(long time, Snitch snitch, UUID player) {
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
		Bukkit.getPluginManager().callEvent(new PlayerHitSnitchEvent(snitch, Bukkit.getPlayer(player)));
	}

	@Override
	public String getChatRepresentationIdentifier() {
		return "Enter";
	}
}
