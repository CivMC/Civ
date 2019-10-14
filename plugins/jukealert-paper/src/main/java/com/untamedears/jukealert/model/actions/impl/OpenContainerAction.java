package com.untamedears.jukealert.model.actions.impl;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggableBlockAction;

import net.md_5.bungee.api.chat.TextComponent;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;

public class OpenContainerAction extends LoggableBlockAction {
	
	public static final String ID = "OPEN_CONTAINER";

	public OpenContainerAction(long time, Snitch snitch, UUID player, Location location, Material material) {
		super(time, snitch, player, location, material);
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
