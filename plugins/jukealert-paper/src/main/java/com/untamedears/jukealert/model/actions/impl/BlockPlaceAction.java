package com.untamedears.jukealert.model.actions.impl;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggableBlockAction;
import com.untamedears.jukealert.util.JAUtility;

import net.md_5.bungee.api.chat.TextComponent;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.namelayer.NameAPI;

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
	public IClickable getGUIRepresentation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TextComponent getChatRepresentation(Location relative) {
		return new TextComponent(String.format("%sPlace  %s%s  %s%s %s%s", ChatColor.GOLD, ChatColor.GREEN,
				NameAPI.getCurrentName(getPlayer()),ChatColor.AQUA, material.toString(), ChatColor.YELLOW,
				JAUtility.formatLocation(location, false)));
	}
}
