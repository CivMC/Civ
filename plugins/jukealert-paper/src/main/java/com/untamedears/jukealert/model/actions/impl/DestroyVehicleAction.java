package com.untamedears.jukealert.model.actions.impl;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerVictimAction;

import net.md_5.bungee.api.chat.TextComponent;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;

public class DestroyVehicleAction extends LoggablePlayerVictimAction {
	
	public static String ID = "DESTROY_VEHICLE";

	public DestroyVehicleAction(long time, Snitch snitch, UUID player, Location location, String victim) {
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
	
	/**
	 * @return Material of the vehicle destroyed
	 */
	public Material getVehicle() {
		return Material.valueOf(victim);
	}

	@Override
	public String getIdentifier() {
		return ID;
	}

}
