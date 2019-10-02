package com.untamedears.JukeAlert.model.actions.impl;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;

import com.untamedears.JukeAlert.model.actions.LoggableBlockAction;
import com.untamedears.JukeAlert.util.JAUtility;

import net.md_5.bungee.api.chat.TextComponent;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;

public class BlockPlaceAction extends LoggableBlockAction {
	
	public static final String ID = "BLOCK_PLACE";
	
	public BlockPlaceAction(long time, UUID player, Location location, String materialString) {
		this(time, player, location, JAUtility.parseMaterial(materialString));
	}

	public BlockPlaceAction(long time, UUID player, Location location, Material material) {
		super(time, player, location, material);
	}

	@Override
	public String getIdentifier() {
		return ID;
	}

	@Override
	public IClickable getGUIRepresentation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TextComponent getChatRepresentation() {
		// TODO Auto-generated method stub
		return null;
	}
}
