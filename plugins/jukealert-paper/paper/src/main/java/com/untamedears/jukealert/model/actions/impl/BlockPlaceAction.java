package com.untamedears.jukealert.model.actions.impl;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggableBlockAction;
import com.untamedears.jukealert.util.JAUtility;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;

public class BlockPlaceAction extends LoggableBlockAction {

	public static final String ID = "BLOCK_PLACE";

	public BlockPlaceAction(long time, Snitch snitch, UUID player, Location location, String materialString) {
		this(time, snitch, player, location, JAUtility.parseMaterial(materialString));
	}

	public BlockPlaceAction(long time, Snitch snitch, UUID player, Location location, Material material) {
		super(time, snitch, player, location, material);
	}

	@Override
	public String getIdentifier() {
		return ID;
	}

	@Override
	protected String getChatRepresentationIdentifier() {
		return "Place";
	}

}
