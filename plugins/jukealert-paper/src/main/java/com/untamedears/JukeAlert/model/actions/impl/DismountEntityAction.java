package com.untamedears.JukeAlert.model.actions.impl;

import java.util.UUID;

import org.bukkit.Location;

import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.model.actions.abstr.LoggablePlayerVictimAction;

import net.md_5.bungee.api.chat.TextComponent;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;

public class DismountEntityAction extends LoggablePlayerVictimAction {
	
	public static final String ID = "DISMOUNT_ENTITY";

	public DismountEntityAction(long time, Snitch snitch, UUID player, Location location, String victim) {
		super(time, snitch, player, location, victim);
	}

	@Override
	public IClickable getGUIRepresentation() {
		return null;
	}

	@Override
	public TextComponent getChatRepresentation(Location reference) {
		return null;
	}

	@Override
	public String getIdentifier() {
		return null;
	}

}
