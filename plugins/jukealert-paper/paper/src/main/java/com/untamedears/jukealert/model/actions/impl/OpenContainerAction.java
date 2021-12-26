package com.untamedears.jukealert.model.actions.impl;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggableBlockAction;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;

public class OpenContainerAction extends LoggableBlockAction {
	
	public static final String ID = "OPEN_CONTAINER";

	public OpenContainerAction(long time, Snitch snitch, UUID player, Location location, Material material) {
		super(time, snitch, player, location, material);
	}

	@Override
	protected String getChatRepresentationIdentifier() {
		return "Opened";
	}

	@Override
	public String getIdentifier() {
		return ID;
	}

}
