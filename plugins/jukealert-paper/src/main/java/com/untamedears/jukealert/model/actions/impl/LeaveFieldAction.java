package com.untamedears.jukealert.model.actions.impl;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerAction;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;

import java.util.UUID;

public class LeaveFieldAction extends LoggablePlayerAction {

	public static final String ID = "LEAVE";

	public LeaveFieldAction(long time, Snitch snitch, UUID player) {
		super(time, snitch, player);
	}

	@Override
	public IClickable getGUIRepresentation() {
		return getEnrichedClickableSkullFor(getPlayer());
	}

	@Override
	public String getIdentifier() {
		return ID;
	}

	@Override
	protected String getChatRepresentationIdentifier() {
		return "Leave";
	}

}
