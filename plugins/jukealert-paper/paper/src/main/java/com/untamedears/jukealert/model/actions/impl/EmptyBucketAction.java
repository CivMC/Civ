package com.untamedears.jukealert.model.actions.impl;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggableBlockAction;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;

public class EmptyBucketAction extends LoggableBlockAction {
	
	public static final String ID = "EMPTY_BUCKET";

	public EmptyBucketAction(long time, Snitch snitch, UUID player, Location location, Material material) {
		super(time, snitch, player, location, material);
	}

	@Override
	protected String getChatRepresentationIdentifier() {
		return "Emptied bucket";
	}


	@Override
	public String getIdentifier() {
		return ID;
	}

}
