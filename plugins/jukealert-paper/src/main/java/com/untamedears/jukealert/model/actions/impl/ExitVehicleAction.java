package com.untamedears.jukealert.model.actions.impl;

import java.util.UUID;

import org.bukkit.Location;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerVictimAction;

import net.md_5.bungee.api.chat.TextComponent;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;

public class ExitVehicleAction extends LoggablePlayerVictimAction {

	public static final String ID = "EXIT_VEHICLE";
	
	public ExitVehicleAction(long time, Snitch snitch, UUID player, Location location, String victim) {
		super(time, snitch, player, location, victim);
	}

	@Override
	public IClickable getGUIRepresentation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TextComponent getChatRepresentation(Location reference) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIdentifier() {
		return ID;
	}

}