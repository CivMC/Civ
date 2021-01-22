package com.untamedears.jukealert.model.actions.impl;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerAction;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;

public class LeaveFieldAction extends LoggablePlayerAction {

	public static final String ID = "LEAVE";

	public LeaveFieldAction(long time, Snitch snitch, UUID player) {
		super(time, snitch, player);
	}

	@Override
	public IClickable getGUIRepresentation() {
		ItemStack is = getSkullFor(getPlayer());
		super.enrichGUIItem(is);
		return new DecorationStack(is);
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
